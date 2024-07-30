package org.hl.socialspherebackend.api.dto.notification.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserFriendRequestResult {

    @JsonProperty(value = "response")
    private final UserFriendRequestResponse userFriendRequest;

    @JsonProperty
    private final NotificationErrorCode code;

    @JsonProperty
    private final String message;


    private UserFriendRequestResult(UserFriendRequestResponse userFriendRequest, NotificationErrorCode code, String message) {
        this.userFriendRequest = userFriendRequest;
        this.code = code;
        this.message = message;
    }

    public static UserFriendRequestResult success(UserFriendRequestResponse response) {
        return new UserFriendRequestResult(response, null, null);
    }

    public static UserFriendRequestResult failure(NotificationErrorCode code, String message) {
        return new UserFriendRequestResult(null, code, message);
    }

    public boolean isSuccess() {
        return userFriendRequest != null;
    }

    @JsonIgnore
    public boolean isFailure() {
        return !isSuccess();
    }

    @Override
    public String toString() {
        return "UserFriendRequestResult{" +
                "userFriendRequest=" + userFriendRequest +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

}
