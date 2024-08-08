package org.hl.socialspherebackend.api.dto.chat.response;

import org.hl.socialspherebackend.api.dto.user.response.UserWrapperResponse;

import java.time.Instant;

public record ChatResponse(Long id,
                           Instant createdAt,
                           ChatMessageResponse lastMessage,
                           UserWrapperResponse user) {
}
