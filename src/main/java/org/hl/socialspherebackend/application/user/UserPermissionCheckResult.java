package org.hl.socialspherebackend.application.user;

public record UserPermissionCheckResult(boolean allowed, String notAllowedErrorMessage) {
}
