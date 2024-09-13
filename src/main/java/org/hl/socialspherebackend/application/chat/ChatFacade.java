package org.hl.socialspherebackend.application.chat;

import org.hl.socialspherebackend.api.dto.chat.request.ChatMessageRequest;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class ChatFacade {

    private final Logger log = LoggerFactory.getLogger(ChatFacade.class);

    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final RequestValidatorChain requestValidator;
    private final Clock clock;

    public ChatFacade(ChatRepository chatRepository, ChatMessageRepository chatMessageRepository, UserRepository userRepository, RequestValidatorChain requestValidator, Clock clock) {
        this.chatRepository = chatRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.requestValidator = requestValidator;
        this.clock = clock;
    }


    public DataResult<ChatMessageResponse> sendMessage(ChatMessageRequest chatMessageRequest, Principal principal) {
        RequestValidateResult validateResult = requestValidator.validate(chatMessageRequest);
        if(!validateResult.valid()) {
            return DataResult.failure(validateResult.errorCode(), validateResult.errorMessage());
        }

        User sender = getUserFromPrincipal(principal);
        if(sender == null) {
            return DataResult.failure(ChatErrorCode.SENDER_NOT_FOUND,
                    "Could not find sender in security context");
        }

        Optional<User> receiverOpt = userRepository.findById(chatMessageRequest.receiverId());
        if(receiverOpt.isEmpty()) {
            return DataResult.failure(ChatErrorCode.RECEIVER_NOT_FOUND,
                    "receiver with id %d not found".formatted(chatMessageRequest.receiverId()));
        }

        User receiver = receiverOpt.get();

        List<Chat> senderChats = chatRepository.findChatsByUserId(sender.getId());
        Chat chat;
        Instant now = Instant.now(clock);

        List<Chat> usersChat = senderChats.stream()
                .filter(ch -> checkIfUsersHaveValidChat(sender, receiver, ch))
                .toList();

        if(usersChat.isEmpty()) {
            chat = new Chat(now);
        } else if (usersChat.size() > 1) {
            return DataResult.failure(ChatErrorCode.SERVER_ERROR,
                    "Error occurred in database. Sender and receiver(id=%d) have more than one chat"
                            .formatted(chatMessageRequest.receiverId()));
        } else {
            chat = usersChat.get(0);
        }

        chat.addUser(sender);
        chat.addUser(receiver);

        ChatMessage message = new ChatMessage(chatMessageRequest.content(), now, chat, sender);
        chat.addChatMessage(message);
        chatRepository.save(chat);

        ChatMessageResponse response = ChatMapper.fromEntityToResponse(message);
        return DataResult.success(response);
    }

    private boolean checkIfUsersHaveValidChat(User sender, User receiver, Chat chat) {
        Set<User> users = chat.getUsers();

        return users.stream()
                .allMatch(u -> u.equals(sender) || u.equals(receiver));
    }


    public DataResult<Set<ChatResponse>> findCurrentUserChats() {
        DataResult<List<Chat>> result = findCurrentUserChatEntities();
        if(result.isFailure()) {
            return DataResult.failure(result.getErrorCode(), result.getErrorMessage());
        }
        List<Chat> chats = result.getData();
        User user = AuthUtils.getCurrentUser().orElseThrow();

        Set<ChatResponse> responseSet = chats.stream()
                .map(ch -> {
                    User anotherUserInChat = userRepository.findSecondUserInChat(ch.getId(), user.getId()).get();
                    ChatMessage lastMessage = chatMessageRepository.findLastChatMessageByChatId(ch.getId()).get();
                    return ChatMapper.fromEntitiesToResponse(ch, lastMessage, anotherUserInChat);
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
                .map(ChatMapper::fromEntityToResponse)
                .collect(toSet());

        return DataResult.success(response);
    }

    public DataResult<List<Long>> findChatsIdWithNewMessage() {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if(userOpt.isEmpty()) {
            return DataResult.failure(ChatErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }

       List<Long> chatIds = chatRepository.findChatsIdWithNewMessage(userOpt.get().getId());
        if(chatIds.isEmpty()) {
            return DataResult.failure(ChatErrorCode.NO_NEW_MESSAGE, "Current user has no new messages!");
        }
        return DataResult.success(chatIds);
    }

    private DataResult<List<Chat>> findCurrentUserChatEntities() {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if(userOpt.isEmpty()) {
            return DataResult.failure(ChatErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }
        User user = userOpt.get();
        List<Chat> chats = chatRepository.findChatsByUserId(user.getId());
        if(chats.isEmpty()) {
            return DataResult.failure(ChatErrorCode.USER_HAS_NO_CHATS,"Current user has no chats!");
        }
        return DataResult.success(chats);
    }

    private User getUserFromPrincipal(Principal principal) {
        if(principal == null) return null;

        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        return (User) auth.getPrincipal();
    }
}
