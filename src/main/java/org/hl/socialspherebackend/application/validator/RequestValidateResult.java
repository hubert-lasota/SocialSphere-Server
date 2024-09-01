package org.hl.socialspherebackend.application.validator;

public record RequestValidateResult(boolean valid, RequestValidateErrorCode errorCode, String errorMessage) {
}
