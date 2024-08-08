package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.user.response.UserFriendRequestResponse;

public interface UserFriendRequestNotificationSender {

    void send(UserFriendRequestResponse notification);

}
