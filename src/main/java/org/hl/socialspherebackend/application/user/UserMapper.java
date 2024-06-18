package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.user.request.UserProfileConfigRequest;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
import org.hl.socialspherebackend.api.dto.user.response.UserFriendRequestResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserProfileConfigResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserProfileResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserResponse;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.api.entity.user.UserFriendRequest;
import org.hl.socialspherebackend.api.entity.user.UserProfile;
import org.hl.socialspherebackend.api.entity.user.UserProfileConfig;

public class UserMapper {

    private UserMapper() { }


    public static UserResponse fromUserEntityToResponse(User entity) {
        return new UserResponse(
                entity.getId(),
                entity.getUsername()
        );
    }

    public static UserProfileResponse fromUserProfileEntityToResponse(UserProfile userProfile, byte[] image) {
        return new UserProfileResponse(
                userProfile.getFirstName(),
                userProfile.getLastName(),
                userProfile.getCity(),
                userProfile.getCountry(),
                image
        );
    }

    public static UserProfile fromRequestToUserProfileEntity(UserProfileRequest request, User user) {
        return new UserProfile(
                request.firstName(),
                request.lastName(),
                request.city(),
                request.country(),
                user
        );
    }

    public static UserProfileConfigResponse fromUserProfileConfigEntityToResponse(UserProfileConfig userProfileConfig) {
        return new UserProfileConfigResponse(userProfileConfig.getUserPrivacyLevel());
    }

    public static UserProfileConfig fromRequestToUserProfileConfigEntity(UserProfileConfigRequest request, User user) {
      return new UserProfileConfig(
              request.userProfilePrivacyLevel(),
              user
      );
    }

    public static UserFriendRequestResponse fromUserFriendRequestEntityToResponse(UserFriendRequest entity) {
        return new UserFriendRequestResponse(
                entity.getSender().getId(),
                entity.getSender().getId(),
                entity.getStatus()
        );
    }

}
