package org.hl.socialspherebackend.api.dto.user.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserFriendListResult {

    @JsonProperty
    private final UserFriendListResponse friends;

    @JsonProperty
    private final Code code;

    @JsonProperty
    private final String message;

    public enum Code {
        FOUND, NOT_FOUND, USER_NOT_FOUND, USER_HAVE_NO_FRIENDS
    }

    private UserFriendListResult(UserFriendListResponse friends, Code code, String message) {
        this.friends = friends;
        this.code = code;
        this.message = message;
    }


    public static UserFriendListResult success(UserFriendListResponse response, Code code) {
        return new UserFriendListResult(response, code, null);
    }

    public static UserFriendListResult failure(Code code, String message) {
        return new UserFriendListResult(null, code, message);
    }

    public boolean isSuccess() {
        return friends != null;
    }

    @JsonIgnore
    public boolean isFailure() {
        return !isSuccess();
    }


    @Override
    public String toString() {
        return "UserFriendListResult{" +
                "friends=" + friends +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

}
