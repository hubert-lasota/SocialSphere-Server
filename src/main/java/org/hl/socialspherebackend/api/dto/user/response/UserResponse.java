package org.hl.socialspherebackend.api.dto.user.response;

public record UserResponse(Long id, String username, RelationshipStatus relationshipStatus) {
}
