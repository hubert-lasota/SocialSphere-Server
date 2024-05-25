package org.hl.socialspherebackend.api.dto.user.response;

public class UserResult {

    private final UserResponse user;
    private final UserProfileResponse userProfileResponse;
    private final UserProfileConfigResponse userProfileConfigResponse;
    private final Code code;
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

    public boolean isFailure() {
        return !isSuccess();
    }

}