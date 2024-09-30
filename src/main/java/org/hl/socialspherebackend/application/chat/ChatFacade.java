package org.hl.socialspherebackend.application.chat;

import org.hl.socialspherebackend.api.dto.chat.request.ChatMessageRequest;
import org.hl.socialspherebackend.api.dto.chat.request.ChatRequest;
import org.hl.socialspherebackend.api.dto.chat.response.ChatErrorCode;
import org.hl.socialspherebackend.api.dto.chat.response.ChatMessageResponse;
import org.hl.socialspherebackend.api.dto.chat.response.ChatResponse;
import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.entity.chat.Chat;
import org.hl.socialspherebackend.api.entity.chat.ChatMessage;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.application.util.AuthUtils;
import org.hl.socialspherebackend.application.validator.RequestValidateResult;
import org.hl.socialspherebackend.application.validator.RequestValidatorChain;
import org.hl.socialspherebackend.infrastructure.chat.ChatMessageRepository;
import org.hl.socialspherebackend.infrastructure.chat.ChatRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class ChatFacade {

    private final Logger log = LoggerFactory.getLogger(ChatFacade.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final RequestValidatorChain requestValidator;
    private final Clock clock;

    public ChatFacade(SimpMessagingTemplate messagingTemplate,
                      ChatRepository chatRepository,
                      ChatMessageRepository chatMessageRepository,
                      UserRepository userRepository,
                      RequestValidatorChain requestValidator,
                      Clock clock) {
        this.messagingTemplate = messagingTemplate;
        this.chatRepository = chatRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.requestValidator = requestValidator;
        this.clock = clock;
    }


    public DataResult<ChatResponse> createChat(ChatRequest request) {
        Long receiverId = request.receiverId();
        Optional<User> receiverOpt = userRepository.findById(receiverId);
        if (receiverOpt.isEmpty()) {
            return DataResult.failure(ChatErrorCode.RECEIVER_NOT_FOUND,
                    "receiver with id %d not found".formatted(receiverId));
        }
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(ChatErrorCode.USER_NOT_FOUND,
                    "Could not find current user!");
        }

        User currentUser = currentUserOpt.get();
        User receiver = receiverOpt.get();

        List<Chat> chats = chatRepository.findChatsByUserId(currentUser.getId());

        boolean doUsersHaveChat = chats.stream()
                .anyMatch(ch -> ch.getUsers().stream().anyMatch(u -> u.equals(receiver)));

        if (doUsersHaveChat) {
            return DataResult.failure(ChatErrorCode.USERS_ALREADY_HAVE_CHAT,
                    "Current user already has chat with receiver(id=%d)".formatted(receiverId));
        }

        Instant now = Instant.now(clock);
        Chat chat = new Chat(currentUser, now);
        chat.addUser(currentUser);
        chat.addUser(receiver);
        chatRepository.save(chat);

        ChatResponse response = ChatMapper.fromEntitiesToResponse(chat, receiver, false);
        return DataResult.success(response);

    }

    public void sendMessage(ChatMessageRequest request, Principal principal) {
        RequestValidateResult validateResult = requestValidator.validate(request);
        if(!validateResult.valid()) {
            log.debug("ChatMessage request is not valid: Error message: {}", validateResult.errorMessage());
            return;
        }

        User sender = getUserFromPrincipal(principal);
        if(sender == null) {
            log.debug("Could not find current user in security context!");
            return;
        }

        Optional<Chat> chatOpt = chatRepository.findById(request.chatId());
        if(chatOpt.isEmpty()) {
            log.debug("Could not find chat with id = %d", request.chatId());
            return;
        }

        Chat chat = chatOpt.get();
        Instant now = Instant.now(clock);
        ChatMessage chatMessage = new ChatMessage(request.content(), now, chat, sender);

        chatMessageRepository.save(chatMessage);
        ChatMessageResponse response = ChatMapper.fromEntityToResponse(chatMessage);

        Long receiverId = findSecondUserInChat(chat, sender).getId();
        messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/messages", response);
        messagingTemplate.convertAndSendToUser(sender.getId().toString(), "/queue/my_messages", response);
    }

    public DataResult<?> setSeenAllMessagesInChat(Long chatId) {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if(userOpt.isEmpty()) {
            return DataResult.failure(ChatErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }

        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if(chatOpt.isEmpty()) {
            return DataResult.failure(ChatErrorCode.CHAT_NOT_FOUND,
                    "Could not find chat wid id=%d".formatted(chatId));
        }

        User currentUser = userOpt.get();
        Chat chat = chatOpt.get();
        final Boolean[] didUpdate = { false };
        chat.getChatMessages()
                .forEach(mess -> {
                    if(!mess.getSender().equals(currentUser)) {
                        mess.setSeen(true);
                        didUpdate[0] = true;
                    }
                });

        // prevent unnecessary db request
        if(didUpdate[0]) chatRepository.save(chat);
        return DataResult.success(null);
    }

    public DataResult<Set<ChatResponse>> findCurrentUserChats() {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if(userOpt.isEmpty()) {
            return DataResult.failure(ChatErrorCode.USER_NOT_FOUND,
                    "Could not find current user!");
        }
        User user = userOpt.get();
        List<Chat> chats = chatRepository.findChatsByUserId(user.getId());
        if(chats.isEmpty()) {
            return DataResult.failure(ChatErrorCode.USER_HAS_NO_CHATS, "Current user has no chats!");
        }

        Set<ChatResponse> responseSet = chats.stream()
                .map(ch -> {
                    User secondUser = findSecondUserInChat(ch, user);
                    return ChatMapper.fromEntitiesToResponse(ch, secondUser, checkIfUserHasNotSeenMessages(ch, user));
                })
                .collect(toSet());

        return DataResult.success(responseSet);
    }


    public DataResult<Set<ChatMessageResponse>> findChatMessages(Long chatId) {
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if(chatOpt.isEmpty()) {
            return DataResult.failure(ChatErrorCode.CHAT_NOT_FOUND,
                    "Chat with id = %d not found in database!".formatted(chatId));
        }


        Chat chat = chatOpt.get();
        Set<ChatMessage> messages = chat.getChatMessages();
        if(messages.isEmpty()) {
            return DataResult.failure(ChatErrorCode.CHAT_MESSAGES_NOT_FOUND,
                    "Chat with id = %d does not have messages!".formatted(chatId));
        }

        Set<ChatMessageResponse> response = messages.stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(ChatMapper::fromEntityToResponse)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return DataResult.success(response);
    }

    public DataResult<List<ChatResponse>> findCurrentUserChatsWithNewMessage() {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if(userOpt.isEmpty()) {
            return DataResult.failure(ChatErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }

       List<Chat> chats = chatRepository.findChatsWithNewMessages(userOpt.get().getId());
        if(chats.isEmpty()) {
            return DataResult.failure(ChatErrorCode.NO_NEW_MESSAGE, "Current user has no new messages!");
        }

        User user = userOpt.get();
        List<ChatResponse> response = chats.stream()
                .map((ch) -> {
                    User secondUser = findSecondUserInChat(ch, user);
                    boolean hasNotSeenMessages = checkIfUserHasNotSeenMessages(ch, secondUser);
                    return ChatMapper.fromEntitiesToResponse(ch, secondUser, hasNotSeenMessages);
                })
                .toList();
        return DataResult.success(response);
    }


    private User findSecondUserInChat(Chat chat, User firstUser) {
        return chat.getUsers()
                .stream()
                .filter(u -> !u.equals(firstUser))
                .findFirst().orElseThrow();
    }

    private User getUserFromPrincipal(Principal principal) {
        var auth = (UsernamePasswordAuthenticationToken) principal;
        return (User) auth.getPrincipal();
    }

    private boolean checkIfUserHasNotSeenMessages(Chat chat, User user) {
        return chat.getChatMessages()
                .stream()
                .anyMatch(m -> !m.isSeen() && !m.getSender().equals(user));
    }

}
