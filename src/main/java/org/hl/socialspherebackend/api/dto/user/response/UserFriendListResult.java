package org.hl.socialspherebackend.api.dto.user.response;

public class UserFriendListResult {

    private final UserFriendListResponse friends;
    private final Code code;
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

    public boolean isFailure() {
        return !isSuccess();
    }

}
