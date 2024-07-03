package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.user.request.UserProfileConfigRequest;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
import org.hl.socialspherebackend.api.dto.user.response.*;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.api.entity.user.UserFriendRequest;
import org.hl.socialspherebackend.api.entity.user.UserProfile;
import org.hl.socialspherebackend.api.entity.user.UserProfileConfig;
import org.hl.socialspherebackend.application.util.FileUtils;

class UserMapper {

    private UserMapper() { }


    public static UserResponse fromUserEntityToResponse(User entity) {
        return new UserResponse(
                entity.getId(),
                entity.getUsername()
        );
    }

    public static SearchUsersResponse fromUserEntityToSearchUsersResponse(User entity) {
        UserProfile profileResponse = entity.getUserProfile();
        String firstName = profileResponse.getFirstName();
        String lastName = profileResponse.getLastName();
        byte[] profilePicture = null;
        if(profileResponse.getProfilePicture() != null) {
            profilePicture = FileUtils.decompressFile(profileResponse.getProfilePicture().getImage());
        }

        return new SearchUsersResponse(entity.getId(), firstName, lastName, profilePicture);
    }

    public static UserFriendResponse fromUserEntityToUserFriendResponse(User entity) {
        UserResponse userResponse = UserMapper.fromUserEntityToResponse(entity);
        byte[] profilePicture = FileUtils.decompressFile(entity.getUserProfile().getProfilePicture().getImage());
        UserProfileResponse userProfile = UserMapper.fromUserProfileEntityToResponse(entity.getUserProfile(), profilePicture);
        UserProfileConfigResponse userProfileConfig = UserMapper.fromUserProfileConfigEntityToResponse(entity.getUserProfileConfig());
        return new UserFriendResponse(userResponse, userProfile, userProfileConfig);
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
