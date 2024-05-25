package org.hl.socialspherebackend.application.user.mapper;

import org.hl.socialspherebackend.api.dto.user.response.UserFriendRequestResponse;
import org.hl.socialspherebackend.api.entity.user.UserFriendRequest;

public class UserFriendRequestMapper {

    private UserFriendRequestMapper() { }

    public static UserFriendRequestResponse fromEntityToResponse(UserFriendRequest entity) {
        return new UserFriendRequestResponse(
                entity.getId().getSenderId(),
                entity.getId().getReceiverId(),
                entity.getStatus()
        );
    }
}
