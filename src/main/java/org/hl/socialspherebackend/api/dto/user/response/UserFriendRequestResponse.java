package org.hl.socialspherebackend.api.dto.user.response;

import org.hl.socialspherebackend.api.entity.user.UserFriendRequestStatus;

public record UserFriendRequestResponse(Long senderId, Long receiverId, UserFriendRequestStatus status) {
}
