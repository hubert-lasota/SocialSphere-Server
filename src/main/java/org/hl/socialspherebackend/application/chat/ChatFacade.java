package org.hl.socialspherebackend.application.chat;

import org.hl.socialspherebackend.api.dto.chat.request.ChatMessageRequest;
import org.hl.socialspherebackend.api.dto.chat.response.ChatErrorCode;
import org.hl.socialspherebackend.api.dto.chat.response.ChatMessageResponse;
import org.hl.socialspherebackend.api.dto.chat.response.ChatResponse;
import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.entity.chat.Chat;
import org.hl.socialspherebackend.api.entity.chat.ChatMessage;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.application.validator.RequestValidator;
import org.hl.socialspherebackend.infrastructure.chat.ChatRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class ChatFacade {

    private final Logger log = LoggerFactory.getLogger(ChatFacade.class);

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final RequestValidator<ChatMessageRequest, ChatValidateResult> chatMessageValidator;
    private final Clock clock;

    public ChatFacade(ChatRepository chatRepository, UserRepository userRepository, RequestValidator<ChatMessageRequest, ChatValidateResult> chatMessageValidator, Clock clock) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.chatMessageValidator = chatMessageValidator;
        this.clock = clock;
    }

    public DataResult<ChatMessageResponse, ChatErrorCode> sendMessage(ChatMessageRequest chatMessageRequest) {
        ChatValidateResult validateResult = chatMessageValidator.validate(chatMessageRequest);
        if(!validateResult.isValid()) {
            return DataResult.failure(validateResult.code(), validateResult.message());
        }

        Optional<User> senderOpt = userRepository.findById(chatMessageRequest.senderId());
        Optional<User> receiverOpt = userRepository.findById(chatMessageRequest.receiverId());
        if(senderOpt.isEmpty()) {
            return DataResult.failure(ChatErrorCode.SENDER_NOT_FOUND,
                    "sender with id %d not found".formatted(chatMessageRequest.senderId()));
        }
        if(receiverOpt.isEmpty()) {
            return DataResult.failure(ChatErrorCode.RECEIVER_NOT_FOUND,
                    "receiver with id %d not found".formatted(chatMessageRequest.receiverId()));
        }

        User sender = senderOpt.get();
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
                    "Error occurred in database. Sender %s and receiver %s have more than one chat".formatted(chatMessageRequest.senderId(), chatMessageRequest.receiverId()));
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


    public DataResult<Set<ChatResponse>, ChatErrorCode> findUserChats(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return DataResult.failure(ChatErrorCode.USER_NOT_FOUND,
                    "User with id %d not found in database!".formatted(userId));
        }

        List<Chat> chats = chatRepository.findChatsByUserId(userId);
        if(chats.isEmpty()) {
            return DataResult.failure(ChatErrorCode.USER_HAS_NO_CHATS,
                    "User with id %d has no chats".formatted(userId));
        }


        Set<ChatResponse> responseSet = chats.stream()
                .map(ch -> {
                    User anotherUserInChat = chatRepository.findSecondUserInChat(ch.getId(), userId);
                    ChatMessage lastMessage = chatRepository.findLastChatMessageByChatId(ch.getId()).get();
                    return ChatMapper.fromEntitiesToResponse(ch, lastMessage, anotherUserInChat);
                })
                .collect(toSet());

        return DataResult.success(responseSet);
    }

    public DataResult<Set<ChatMessageResponse>, ChatErrorCode> findChatMessages(Long chatId) {
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

}
