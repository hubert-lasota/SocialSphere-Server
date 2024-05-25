package org.hl.socialspherebackend.application.user.mapper;

import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
import org.hl.socialspherebackend.api.dto.user.response.UserProfileResponse;
import org.hl.socialspherebackend.api.entity.user.UserProfile;

public class UserProfileMapper {

    private UserProfileMapper() { }

    public static UserProfileResponse fromEntityToResponse(UserProfile userProfile) {
        return new UserProfileResponse(
                userProfile.getFirstName(),
                userProfile.getLastName(),
                userProfile.getCity(),
                userProfile.getCountry()
        );
    }

    public static UserProfile fromRequestToEntity(UserProfileRequest request) {
        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName(request.firstName());
        userProfile.setLastName(request.lastName());
        userProfile.setCity(request.city());
        userProfile.setCountry(request.country());
        return userProfile;
    }

}
