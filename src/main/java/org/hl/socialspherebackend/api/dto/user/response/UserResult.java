package org.hl.socialspherebackend.api.dto.user.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserResult {

    @JsonProperty
    private final UserResponse user;

    @JsonProperty
    private final UserProfileResponse userProfile;

    @JsonProperty
    private final UserProfileConfigResponse userProfileConfig;

    @JsonProperty
    private final UserErrorCode code;

    @JsonProperty
    private final String message;


    private UserResult(UserResponse user,
                       UserProfileResponse userProfile,
                       UserProfileConfigResponse userProfileConfig,
                       UserErrorCode code,
                       String message) {
        this.user = user;
        this.userProfile = userProfile;
        this.userProfileConfig = userProfileConfig;
        this.code = code;
        this.message = message;
    }

    public static UserResult success(UserResponse userResponse,
                                     UserProfileResponse userProfileResponse,
                                     UserProfileConfigResponse userProfileConfigResponse) {
        return new UserResult(userResponse, userProfileResponse, userProfileConfigResponse, null, null);
    }

    public static UserResult failure(UserErrorCode code, String message) {
        return new UserResult(null, null, null, code, message);
    }


    public boolean isSuccess() {
        return user != null;
    }

    @JsonIgnore
    public boolean isFailure() {
        return !isSuccess();
    }

    @JsonIgnore
    public UserResponse getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "UserResult{" +
                "user=" + user +
                ", userProfileResponse=" + userProfile +
                ", userProfileConfigResponse=" + userProfileConfig +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

}