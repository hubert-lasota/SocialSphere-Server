package org.hl.socialspherebackend.api.dto.chat.request;

public record ChatMessageRequest(Long senderId, Long receiverId, String content) {
}
