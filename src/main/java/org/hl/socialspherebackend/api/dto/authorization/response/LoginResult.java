package org.hl.socialspherebackend.api.dto.authorization.response;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResult {

    @JsonProperty
    private LoginResponse login;

    @JsonProperty
    private AuthorizationErrorCode code;

    @JsonProperty
    private String message;

    private LoginResult(LoginResponse response, AuthorizationErrorCode code, String message) {
        this.login = response;
        this.code = code;
        this.message = message;
    }

    public static LoginResult success(LoginResponse response) {
        return new LoginResult(response, null, null);
    }

    public static LoginResult failure(AuthorizationErrorCode code, String message) {
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
