package org.hl.socialspherebackend.application.chat;

import org.hl.socialspherebackend.api.dto.chat.response.ChatMessageResponse;
import org.hl.socialspherebackend.api.dto.chat.response.ChatResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserHeaderResponse;
import org.hl.socialspherebackend.api.entity.chat.Chat;
import org.hl.socialspherebackend.api.entity.chat.ChatMessage;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.application.user.UserMapper;

import java.time.Instant;

class ChatMapper {

    private ChatMapper() { }

    static ChatMessageResponse fromEntityToResponse(ChatMessage chatMessage) {
        Chat chat = chatMessage.getChat();
        Long chatId = chat.getId();
        User user = chatMessage.getSender();
        UserHeaderResponse sender = UserMapper.fromEntityToUserHeaderResponse(user, null);
        Long messageId = chatMessage.getId();
        String content = chatMessage.getContent();
        Instant sentAt = chatMessage.getCreatedAt();

        return new ChatMessageResponse(messageId, chatId, sender, content, sentAt);
    }

    static ChatResponse fromEntitiesToResponse(Chat chat, User user, boolean hasNotSeenMessages) {
        ChatMessageResponse messageResponse = null;
        ChatMessage lastMessage = chat.getLastMessage();
        if (lastMessage != null) {
            messageResponse = fromEntityToResponse(lastMessage);
        }

        Long chatId = chat.getId();
        Instant createdAt = chat.getCreatedAt();
        UserHeaderResponse createdBy = UserMapper.fromEntityToUserHeaderResponse(chat.getCreatedBy(), null);
        UserHeaderResponse userResponse = UserMapper.fromEntityToUserHeaderResponse(user, null);
        return new ChatResponse(chatId, createdBy, createdAt, hasNotSeenMessages, messageResponse, userResponse);
    }
}
