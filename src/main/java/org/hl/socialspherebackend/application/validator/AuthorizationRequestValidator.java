package org.hl.socialspherebackend.application.validator;

import org.hl.socialspherebackend.api.dto.authorization.request.LoginRequest;

public class AuthorizationRequestValidator extends RequestValidatorChain {

    public AuthorizationRequestValidator(RequestValidatorChain next) {
        super(next);
    }


    @Override
    protected RequestValidateResult doValidate(Object request) {
        LoginRequest r = (LoginRequest) request;
        String username = r.username();
        String password = r.password();

        RequestValidateResult usernameResult = validateUsername(username);
        if(!usernameResult.valid()) {
            return usernameResult;
        }

        RequestValidateResult passwordResult = validatePassword(password);
        if(!passwordResult.valid()) {
            return passwordResult;
        }

        return new RequestValidateResult(true, null, null);
    }

    private RequestValidateResult validateUsername(String username) {
        if(username == null) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_NULL,
                    "Username is null");
        }

        if(username.isBlank() && !acceptBlankText) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_BLANK,
                    "Username is blank");
        }

        if(containsWhitespace(username) && !acceptWhitespace) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_CONTAINS_WHITESPACE,
                    "Username \"%s\" contains whitespace".formatted(username));
        }

        if(username.length() > textMaxSize) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_TOO_LONG,
                    "Username length is too long. Max length is %d".formatted(textMaxSize));
        }

        if(username.length() < textMinSize) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_TOO_SHORT,
                    "Username length is too short. Min length is %d".formatted(textMinSize));
        }

        return new RequestValidateResult(true, null, null);
    }

    private RequestValidateResult validatePassword(String password) {
        if(password == null) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_NULL,
                    "Password is null");
        }

        if(password.isBlank() && !acceptBlankText) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_BLANK,
                    "Password is blank");
        }

        if(containsWhitespace(password) && !acceptWhitespace) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_CONTAINS_WHITESPACE,
                    "Password \"%s\" contains whitespace".formatted(password));
        }

        if(password.length() > textMaxSize) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_TOO_LONG,
                    "Password length is too long. Max length is %d".formatted(textMaxSize));
        }

        if(password.length() < textMinSize) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_TOO_SHORT,
                    "Password length is too short. Min length is %d".formatted(textMinSize));
        }

        return new RequestValidateResult(true, null, null);
    }

    @Override
    protected boolean isRequestValidInstance(Object request) {
        return request instanceof LoginRequest;
    }

}
