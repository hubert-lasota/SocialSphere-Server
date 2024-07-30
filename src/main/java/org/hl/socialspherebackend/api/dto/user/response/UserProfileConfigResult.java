package org.hl.socialspherebackend.api.dto.user.response;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserProfileConfigResult {

    @JsonProperty
    private final UserProfileConfigResponse userProfileConfig;

    @JsonProperty
    private final UserErrorCode code;

    @JsonProperty
    private final String message;

    private UserProfileConfigResult(UserProfileConfigResponse response, UserErrorCode code, String message) {
        this.userProfileConfig = response;
        this.code = code;
        this.message = message;
    }


    public static UserProfileConfigResult success(UserProfileConfigResponse response) {
        return new UserProfileConfigResult(response, null, null);
    }

    public static UserProfileConfigResult failure(UserErrorCode code, String message) {
        return new UserProfileConfigResult(null, code, message);
    }

    public boolean isSuccess() {
        return userProfileConfig != null;
    }

    @JsonIgnore
    public boolean isFailure() {
        return !isSuccess();
    }

    @Override
    public String toString() {
        return "UserProfileConfigResult{" +
                "userProfileConfig=" + userProfileConfig +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

}
