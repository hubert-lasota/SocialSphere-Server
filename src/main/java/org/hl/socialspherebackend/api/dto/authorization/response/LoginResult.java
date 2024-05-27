package org.hl.socialspherebackend.api.dto.authorization.response;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResult {

    @JsonProperty
    private LoginResponse login;

    @JsonProperty
    private Code code;

    @JsonProperty
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

    @JsonIgnore
    public boolean isFailure() {
        return !isSuccess();
    }


    @Override
    public String toString() {
        return "LoginResult{" +
                "login=" + login +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

}
