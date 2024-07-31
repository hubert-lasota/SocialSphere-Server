package org.hl.socialspherebackend.application.authorization;

import org.hl.socialspherebackend.api.dto.authorization.response.AuthorizationErrorCode;

record AuthorizationValidateResult(boolean isValid, AuthorizationErrorCode code, String message) {
}
