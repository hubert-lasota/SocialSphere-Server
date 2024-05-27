package org.hl.socialspherebackend.api.dto.user.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserResult {

    @JsonProperty
    private final UserResponse user;

    @JsonProperty
    private final UserProfileResponse userProfileResponse;

    @JsonProperty
    private final UserProfileConfigResponse userProfileConfigResponse;

    @JsonProperty
    private final Code code;

    @JsonProperty
    private final String message;

    public enum Code { NOT_FOUND, FOUND }


    private UserResult(UserResponse user,
                       UserProfileResponse userProfileResponse,
                       UserProfileConfigResponse userProfileConfigResponse,
                       Code code,
                       String message) {
        this.user = user;
        this.userProfileResponse = userProfileResponse;
        this.userProfileConfigResponse = userProfileConfigResponse;
        this.code = code;
        this.message = message;
    }

    public static UserResult success(UserResponse userResponse,
                                     UserProfileResponse userProfileResponse,
                                     UserProfileConfigResponse userProfileConfigResponse,
                                     Code code) {
        return new UserResult(userResponse, userProfileResponse, userProfileConfigResponse, code, null);
    }

    public static UserResult failure(Code code, String message) {
        return new UserResult(null, null, null, code, message);
    }


    public boolean isSuccess() {
        return user != null;
    }

    @JsonIgnore
    public boolean isFailure() {
        return !isSuccess();
    }

    @Override
    public String toString() {
        return "UserResult{" +
                "user=" + user +
                ", userProfileResponse=" + userProfileResponse +
                ", userProfileConfigResponse=" + userProfileConfigResponse +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

}