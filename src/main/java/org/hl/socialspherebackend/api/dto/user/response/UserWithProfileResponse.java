package org.hl.socialspherebackend.api.dto.user.response;

public record UserWithProfileResponse(UserResponse user, UserProfileResponse userProfile) {
}
