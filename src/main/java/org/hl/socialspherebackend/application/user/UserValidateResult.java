package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.user.response.UserErrorCode;

public record UserValidateResult(boolean isValid, UserErrorCode code, String message) {
}
