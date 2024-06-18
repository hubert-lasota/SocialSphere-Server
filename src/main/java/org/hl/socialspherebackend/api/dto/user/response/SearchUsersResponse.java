package org.hl.socialspherebackend.api.dto.user.response;

public record SearchUsersResponse(Long userId, String firstName, String lastName, byte[] profilePicture) {
}
