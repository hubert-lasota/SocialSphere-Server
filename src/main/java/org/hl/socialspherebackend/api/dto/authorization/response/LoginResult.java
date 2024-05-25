package org.hl.socialspherebackend.api.dto.authorization.response;


public class LoginResult {

    private LoginResponse login;
    private Code code;
    private String message;

    public enum Code {
        CREATED, USERNAME_EXISTS, USERNAME_DOES_NOT_EXISTS, SUCCESSFULLY_LOGGED_IN, VALID_USER, NOT_VALID_USER
    }

    private LoginResult(LoginResponse response, Code code, String message) {
        this.login = response;
        this.code = code;
        this.message = message;
    }

    public static LoginResult success(LoginResponse response, Code code) {
        return new LoginResult(response, code, null);
    }

    public static LoginResult failure(Code code, String message) {
        return new LoginResult(null, code, message);
    }

    public boolean isSuccess() {
        return login != null;
    }

    public boolean isFailure() {
        return !isSuccess();
    }

}
