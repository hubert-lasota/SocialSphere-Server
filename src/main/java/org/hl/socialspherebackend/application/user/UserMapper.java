package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.user.request.UserProfileConfigRequest;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
import org.hl.socialspherebackend.api.dto.user.response.*;
import org.hl.socialspherebackend.api.entity.user.*;
import org.hl.socialspherebackend.application.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserMapper {

    private static final Logger log = LoggerFactory.getLogger(UserMapper.class);

    private UserMapper() { }


    public static UserResponse fromEntityToResponse(User user, RelationshipStatus status) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                status,
                user.isOnline(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastOnlineAt()
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
        UserHeaderResponse sender = fromEntityToUserHeaderResponse(userFriendRequest.getSender(), RelationshipStatus.FRIEND);
        return new UserFriendRequestResponse(
                userFriendRequest.getId(),
                sender,
                userFriendRequest.getReceiver().getId(),
                userFriendRequest.getStatus(),
                userFriendRequest.getSentAt()
        );
    }

    public static UserWrapperResponse fromEntityToUserWrapperResponse(User user, RelationshipStatus status) {
        UserResponse userResponse = UserMapper.fromEntityToResponse(user, status);
        UserProfileResponse userProfileResponse = UserMapper.fromEntityToResponse(user.getUserProfile());
        UserProfileConfigResponse userProfileConfigResponse = UserMapper.fromEntityToResponse(user.getUserProfileConfig());

        return new UserWrapperResponse(userResponse, userProfileResponse, userProfileConfigResponse);
    }

    public static UserHeaderResponse fromEntityToUserHeaderResponse(User entity, RelationshipStatus status) {
        UserProfile profileResponse = entity.getUserProfile();
        String firstName = profileResponse.getFirstName();
        String lastName = profileResponse.getLastName();
        byte[] profilePicture = null;
        if(profileResponse.getProfilePicture() != null) {
            profilePicture = FileUtils.decompressFile(profileResponse.getProfilePicture().getImage());
        }

        return new UserHeaderResponse(entity.getId(), firstName, lastName, profilePicture, status);
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
        UserProfilePicture userProfilePicture = userProfile.getProfilePicture();
        if(userProfilePicture == null) {
            log.debug("UserProfile {} has no picture", userProfile);
            return null;
        }

        byte[] profilePicture = userProfile.getProfilePicture().getImage();
        return FileUtils.decompressFile(profilePicture);
    }

}
