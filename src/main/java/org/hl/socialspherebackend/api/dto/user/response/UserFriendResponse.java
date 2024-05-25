package org.hl.socialspherebackend.api.dto.user.response;

public record UserFriendResponse(UserResponse user,
                                 UserProfileResponse userProfile,
                                 UserProfileConfigResponse userProfileConfig) {
}
