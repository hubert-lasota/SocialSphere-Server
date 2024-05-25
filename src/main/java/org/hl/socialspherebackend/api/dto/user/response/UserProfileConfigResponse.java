package org.hl.socialspherebackend.api.dto.user.response;

import org.hl.socialspherebackend.api.entity.user.UserProfilePrivacyLevel;

public record UserProfileConfigResponse(UserProfilePrivacyLevel profilePrivacyLevel) {
}
