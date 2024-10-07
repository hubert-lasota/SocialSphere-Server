package org.hl.socialspherebackend.api.dto.user.request;

import org.hl.socialspherebackend.api.entity.user.UserProfilePrivacyLevel;

public record UserProfileConfigRequest(UserProfilePrivacyLevel profilePrivacyLevel) {
}
