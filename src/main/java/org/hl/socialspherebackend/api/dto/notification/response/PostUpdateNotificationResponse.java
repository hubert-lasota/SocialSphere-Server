package org.hl.socialspherebackend.api.dto.notification.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record PostUpdateNotificationResponse(Long id, @JsonUnwrapped PostUpdateDetails postUpdateDetails, boolean checked) {
}
