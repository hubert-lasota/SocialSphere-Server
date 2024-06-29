package org.hl.socialspherebackend.api.dto.user.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserProfileResult {

    @JsonProperty
    private final UserProfileResponse userProfile;

    @JsonProperty
    private final Code code;

    @JsonProperty
    private final String message;

    public enum Code {
        CREATED, CANNOT_CREATE,
        USER_PROFILE_NOT_FOUND, USER_NOT_FOUND, FOUND,
        UPDATED
    }

    private UserProfileResult(UserProfileResponse userProfile, Code code, String message) {
        this.userProfile = userProfile;
        this.code = code;
        this.message = message;
    }


    public static UserProfileResult success(UserProfileResponse userProfileResponse, Code code) {
        return new UserProfileResult(userProfileResponse, code, null);
    }

    public static UserProfileResult failure(Code code, String message) {
        return new UserProfileResult(null, code, message);
    }

    public boolean isSuccess() {
        return userProfile != null;
    }

    @JsonIgnore
    public boolean isFailure() {
        return !isSuccess();
    }

    public UserProfileResponse getUserProfileResponse() {
        return userProfile;
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
