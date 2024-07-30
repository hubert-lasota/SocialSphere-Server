package org.hl.socialspherebackend.api.dto.notification.response;

import org.hl.socialspherebackend.api.entity.notification.UserFriendRequestStatus;

public record UserFriendRequestResponse(Long senderId, Long receiverId, UserFriendRequestStatus status) {
}
