package org.hl.socialspherebackend.api.dto.user.response;

import java.time.Instant;

public record UserResponse(Long id,
                           String username,
                           RelationshipStatus relationshipStatus,
                           boolean online,
                           Instant createdAt,
                           Instant updatedAt,
                           Instant lastOnlineAt) {
}
