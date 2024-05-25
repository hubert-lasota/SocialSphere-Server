package org.hl.socialspherebackend.api.dto.user.response;



public class UserProfileConfigResult {

    private final UserProfileConfigResponse userProfileConfig;
    private final Code code;
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

    public boolean isFailure() {
        return !isSuccess();
    }

}
