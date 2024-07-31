package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.user.response.UserErrorCode;

record UserValidateResult(boolean isValid, UserErrorCode code, String message) {
}
