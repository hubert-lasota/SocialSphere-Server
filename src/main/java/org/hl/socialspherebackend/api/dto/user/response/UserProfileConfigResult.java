package org.hl.socialspherebackend.api.dto.user.response;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserProfileConfigResult {

    @JsonProperty
    private final UserProfileConfigResponse userProfileConfig;

    @JsonProperty
    private final Code code;

    @JsonProperty
    private final String message;

    public enum Code {
        CREATED, CANNOT_CREATE,
        USER_NOT_FOUND, USER_PROFILE_CONFIG_NOT_FOUND, FOUND,
        UPDATED
    }

    private UserProfileConfigResult(UserProfileConfigResponse response, Code code, String message) {
        this.userProfileConfig = response;
        this.code = code;
        this.message = message;
    }


    public static UserProfileConfigResult success(UserProfileConfigResponse response, Code code) {
        return new UserProfileConfigResult(response, code, null);
    }

    public static UserProfileConfigResult failure(Code code, String message) {
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
