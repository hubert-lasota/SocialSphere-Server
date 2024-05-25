package org.hl.socialspherebackend.application.user.mapper;

import org.hl.socialspherebackend.api.dto.user.request.UserProfileConfigRequest;
import org.hl.socialspherebackend.api.dto.user.response.UserProfileConfigResponse;
import org.hl.socialspherebackend.api.entity.user.UserProfileConfig;

public class UserProfileConfigMapper {

    private UserProfileConfigMapper() { }

    public static UserProfileConfigResponse fromEntityToResponse(UserProfileConfig userProfileConfig) {
        return new UserProfileConfigResponse(userProfileConfig.getUserPrivacyLevel());
    }

    public static UserProfileConfig fromRequestToEntity(UserProfileConfigRequest request) {
        UserProfileConfig userProfileConfig = new UserProfileConfig();
        userProfileConfig.setUserProfilePrivacyLevel(request.userProfilePrivacyLevel());
        return userProfileConfig;
    }
}
