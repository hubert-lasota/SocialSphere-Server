package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.notification.response.UserFriendRequestResponse;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileConfigRequest;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
import org.hl.socialspherebackend.api.dto.user.response.*;
import org.hl.socialspherebackend.api.entity.notification.UserFriendRequest;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.api.entity.user.UserProfile;
import org.hl.socialspherebackend.api.entity.user.UserProfileConfig;
import org.hl.socialspherebackend.application.util.FileUtils;

public class UserMapper {

    private UserMapper() { }


    public static UserResponse fromEntityToResponse(User user, RelationshipStatus status) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                status
        );
    }

    public static UserProfileResponse fromEntityToResponse(UserProfile userProfile) {
        byte[] decompressedProfilePicture = decompressProfilePicture(userProfile);

        return new UserProfileResponse(
                userProfile.getFirstName(),
                userProfile.getLastName(),
                userProfile.getCity(),
                userProfile.getCountry(),
                decompressedProfilePicture
        );
    }

    public static UserProfileConfigResponse fromEntityToResponse(UserProfileConfig userProfileConfig) {
        return new UserProfileConfigResponse(userProfileConfig.getUserPrivacyLevel());
    }

    public static UserFriendRequestResponse fromEntityToResponse(UserFriendRequest userFriendRequest) {
        return new UserFriendRequestResponse(
                userFriendRequest.getSender().getId(),
                userFriendRequest.getSender().getId(),
                userFriendRequest.getStatus()
        );
    }

    public static SearchUsersResponse fromEntityToSearchUsersResponse(User entity, RelationshipStatus status) {
        UserProfile profileResponse = entity.getUserProfile();
        String firstName = profileResponse.getFirstName();
        String lastName = profileResponse.getLastName();
        byte[] profilePicture = null;
        if(profileResponse.getProfilePicture() != null) {
            profilePicture = FileUtils.decompressFile(profileResponse.getProfilePicture().getImage());
        }

        return new SearchUsersResponse(entity.getId(), firstName, lastName, profilePicture, status);
    }

    public static UserFriendResponse fromEntityToUserFriendResponse(User entity, RelationshipStatus status) {
        UserResponse userResponse = UserMapper.fromEntityToResponse(entity, status);
        UserProfileResponse userProfile = UserMapper.fromEntityToResponse(entity.getUserProfile());
        UserProfileConfigResponse userProfileConfig = UserMapper.fromEntityToResponse(entity.getUserProfileConfig());
        return new UserFriendResponse(userResponse, userProfile, userProfileConfig);
    }


    public static UserProfile fromRequestToEntity(UserProfileRequest userProfileRequest, User user) {
        return new UserProfile(
                userProfileRequest.firstName(),
                userProfileRequest.lastName(),
                userProfileRequest.city(),
                userProfileRequest.country(),
                user
        );
    }

    public static UserProfileConfig fromRequestToEntity(UserProfileConfigRequest userProfileConfigRequest, User user) {
      return new UserProfileConfig(
              userProfileConfigRequest.profilePrivacyLevel(),
              user
      );
    }


    private static byte[] decompressProfilePicture(UserProfile userProfile) {
        byte[] profilePicture = userProfile.getProfilePicture().getImage();
        return FileUtils.decompressFile(profilePicture);
    }

}
