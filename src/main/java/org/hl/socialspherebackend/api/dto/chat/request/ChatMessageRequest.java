package org.hl.socialspherebackend.api.dto.chat.request;

public record ChatMessageRequest(Long receiverId, String content) {
}
