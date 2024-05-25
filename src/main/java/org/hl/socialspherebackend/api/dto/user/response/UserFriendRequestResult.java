package org.hl.socialspherebackend.api.dto.user.response;

public class UserFriendRequestResult {

    private final UserFriendRequestResponse userFriendRequest;
    private final Code code;
    private final String message;

    public enum Code { SENT, REPLIED, NOT_SENT, SENDER_NOT_FOUND, RECEIVER_NOT_FOUND, FRIEND_REQUEST_NOT_FOUND }

    private UserFriendRequestResult(UserFriendRequestResponse userFriendRequest, Code code, String message) {
        this.userFriendRequest = userFriendRequest;
        this.code = code;
        this.message = message;
    }

    public static UserFriendRequestResult success(UserFriendRequestResponse response, Code code) {
        return new UserFriendRequestResult(response, code, null);
    }

    public static UserFriendRequestResult failure(Code code, String message) {
        return new UserFriendRequestResult(null, code, message);
    }

    public boolean isSuccess() {
        return userFriendRequest != null;
    }

    public boolean isFailure() {
        return !isSuccess();
    }

}
