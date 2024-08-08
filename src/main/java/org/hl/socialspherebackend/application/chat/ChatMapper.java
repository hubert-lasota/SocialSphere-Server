package org.hl.socialspherebackend.application.chat;

import org.hl.socialspherebackend.api.dto.chat.response.ChatMessageResponse;
import org.hl.socialspherebackend.api.dto.chat.response.ChatResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserWrapperResponse;
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
        UserWrapperResponse sender = UserMapper.fromEntityToUserWrapperResponse(user);
        Long messageId = chatMessage.getId();
        String content = chatMessage.getContent();
        Instant sentAt = chatMessage.getCreatedAt();

        return new ChatMessageResponse(chatId, sender, messageId, content, sentAt);
    }

    static ChatResponse fromEntitiesToResponse(Chat chat, ChatMessage lastMessage, User user) {
        ChatMessageResponse messageResponse = null;
        if (lastMessage != null) {
            messageResponse = fromEntityToResponse(lastMessage);
        }

        Long chatId = chat.getId();
        Instant createdAt = chat.getCreatedAt();
        UserWrapperResponse userResponse = UserMapper.fromEntityToUserWrapperResponse(user);
        return new ChatResponse(chatId, createdAt, messageResponse, userResponse);
    }
}
