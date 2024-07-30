package org.hl.socialspherebackend.api.dto.user.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserFriendListResult {

    @JsonProperty
    private final UserFriendListResponse friends;

    @JsonProperty
    private final UserErrorCode code;

    @JsonProperty
    private final String message;


    private UserFriendListResult(UserFriendListResponse friends, UserErrorCode code, String message) {
        this.friends = friends;
        this.code = code;
        this.message = message;
    }


    public static UserFriendListResult success(UserFriendListResponse response) {
        return new UserFriendListResult(response, null, null);
    }

    public static UserFriendListResult failure(UserErrorCode code, String message) {
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
