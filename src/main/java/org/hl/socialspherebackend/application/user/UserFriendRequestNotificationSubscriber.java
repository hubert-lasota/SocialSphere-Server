package org.hl.socialspherebackend.application.user;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface UserFriendRequestNotificationSubscriber {

    SseEmitter subscribe();

}
