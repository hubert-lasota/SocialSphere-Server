package org.hl.socialspherebackend.api.dto.user.response;

public record UserWrapperResponse(UserResponse user,
                                  UserProfileResponse userProfile,
                                  UserProfileConfigResponse userProfileConfig) {
}
