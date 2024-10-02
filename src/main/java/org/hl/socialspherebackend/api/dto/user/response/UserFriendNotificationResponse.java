package org.hl.socialspherebackend.api.dto.user.response;

import org.hl.socialspherebackend.api.entity.user.UserFriendRequestStatus;

import java.time.Instant;

public record UserFriendNotificationResponse(Long id,
                                             UserHeaderResponse sender,
                                             UserFriendRequestStatus status,
                                             Instant sentAt) {
}
