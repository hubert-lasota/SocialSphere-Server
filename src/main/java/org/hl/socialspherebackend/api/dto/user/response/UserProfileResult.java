package org.hl.socialspherebackend.api.dto.user.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserProfileResult {

    @JsonProperty
    private final UserProfileResponse userProfile;

    @JsonProperty
    private final UserErrorCode code;

    @JsonProperty
    private final String message;

    private UserProfileResult(UserProfileResponse userProfile, UserErrorCode code, String message) {
        this.userProfile = userProfile;
        this.code = code;
        this.message = message;
    }


    public static UserProfileResult success(UserProfileResponse userProfileResponse) {
        return new UserProfileResult(userProfileResponse, null, null);
    }

    public static UserProfileResult failure(UserErrorCode code, String message) {
        return new UserProfileResult(null, code, message);
    }

    public boolean isSuccess() {
        return userProfile != null;
    }

    @JsonIgnore
    public boolean isFailure() {
        return !isSuccess();
    }

    @Override
    public String toString() {
        return "UserProfileResult{" +
                "userProfile=" + userProfile +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

}
