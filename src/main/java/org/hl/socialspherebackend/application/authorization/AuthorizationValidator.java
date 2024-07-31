package org.hl.socialspherebackend.application.authorization;

import org.hl.socialspherebackend.api.dto.authorization.request.LoginRequest;
import org.hl.socialspherebackend.api.dto.authorization.response.AuthorizationErrorCode;
import org.hl.socialspherebackend.application.validator.Validator;

public class AuthorizationValidator extends Validator<LoginRequest, AuthorizationValidateResult> {

    public AuthorizationValidator() {
        super();
    }

    @Override
    public AuthorizationValidateResult validate(LoginRequest objectToValidate) {
        if(objectToValidate == null) {
            return new AuthorizationValidateResult(false, AuthorizationErrorCode.LOGIN_REQUEST_IS_NULL,
                    "Login request is null");
        }

        String username = objectToValidate.username();
        String password = objectToValidate.password();

        if(username == null) {
            return new AuthorizationValidateResult(false, AuthorizationErrorCode.USERNAME_IS_NULL,
                    "Username is null");
        }

        if(username.isBlank() && !acceptBlankText) {
            return new AuthorizationValidateResult(false, AuthorizationErrorCode.USERNAME_IS_BLANK,
                    "Username is blank");
        }

        if(containsWhitespace(username) && !acceptWhitespace) {
            return new AuthorizationValidateResult(false, AuthorizationErrorCode.USERNAME_CONTAINS_WHITESPACE,
                    "Username \"%s\" contains whitespace".formatted(username));
        }

        if(username.length() > textMaxSize) {
            return new AuthorizationValidateResult(false, AuthorizationErrorCode.USERNAME_LENGTH_IS_TOO_LONG,
                    "Username length is too long. Max length is %d".formatted(textMaxSize));
        }

        if(username.length() < textMinSize) {
            return new AuthorizationValidateResult(false, AuthorizationErrorCode.USERNAME_LENGTH_IS_TOO_SHORT,
                    "Username length is too short. Min length is %d".formatted(textMinSize));
        }


        if(password == null) {
            return new AuthorizationValidateResult(false, AuthorizationErrorCode.PASSWORD_IS_NULL,
                    "Password is null");
        }

        if(password.isBlank() && !acceptBlankText) {
            return new AuthorizationValidateResult(false, AuthorizationErrorCode.PASSWORD_IS_BLANK,
                    "Password is blank");
        }

        if(containsWhitespace(username) && !acceptWhitespace) {
            return new AuthorizationValidateResult(false, AuthorizationErrorCode.PASSWORD_CONTAINS_WHITESPACE,
                    "Password \"%s\" contains whitespace".formatted(username));
        }

        if(password.length() > textMaxSize) {
            return new AuthorizationValidateResult(false, AuthorizationErrorCode.PASSWORD_LENGTH_IS_TOO_LONG,
                    "Password length is too long. Max length is %d".formatted(textMaxSize));
        }

        if(password.length() < textMinSize) {
            return new AuthorizationValidateResult(false, AuthorizationErrorCode.PASSWORD_LENGTH_IS_TOO_SHORT,
                    "Password length is too short. Min length is %d".formatted(textMinSize));
        }

        return new AuthorizationValidateResult(true, null, null);
    }

}
