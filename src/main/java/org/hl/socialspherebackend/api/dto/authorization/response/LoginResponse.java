package org.hl.socialspherebackend.api.dto.authorization.response;

public record LoginResponse(Long userId, String username, String jwt) {
}
