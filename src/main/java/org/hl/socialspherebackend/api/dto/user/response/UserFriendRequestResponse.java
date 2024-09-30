package org.hl.socialspherebackend.api.dto.user.response;

import org.hl.socialspherebackend.api.entity.user.UserFriendRequestStatus;

import java.time.Instant;

public record UserFriendRequestResponse(Long id, UserHeaderResponse sender, Long receiverId, UserFriendRequestStatus status, Instant sentAt) {
}
