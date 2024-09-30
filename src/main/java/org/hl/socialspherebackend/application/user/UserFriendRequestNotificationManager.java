package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.user.response.UserFriendRequestResponse;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.application.common.Observer;
import org.hl.socialspherebackend.application.util.AuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserFriendRequestNotificationManager implements Observer<UserFriendRequestResponse>, UserFriendRequestNotificationSubscriber {

    private static final Logger log = LoggerFactory.getLogger(UserFriendRequestNotificationManager.class);

    private final ConcurrentMap<Long, SseEmitter> clients = new ConcurrentHashMap<>();

    @Override
    public void update(UserFriendRequestResponse subject) {
        if(subject == null) {
            log.debug("Subject is null");
            return;
        }
        sendNotification(subject);
    }

    @Override
    public SseEmitter subscribe() {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            log.debug("Could not find current user in security context");
            return null;
        }
        User currentUser = currentUserOpt.get();
        Long userId = currentUser.getId();

        SseEmitter sseEmitter = new SseEmitter();
        sseEmitter.onCompletion(() -> clients.remove(userId));
        sseEmitter.onTimeout(() -> clients.remove(userId));

        clients.put(userId, sseEmitter);
        return sseEmitter;
    }


    @Async
    void sendNotification(UserFriendRequestResponse notification) {
        Long userId = notification.receiverId();
        SseEmitter sseEmitter = clients.get(userId);
        if(sseEmitter != null) {
            try {
                sseEmitter.send(notification);
            } catch (IOException exc) {
                log.debug("Error occurred while sending notification. Client is removed from list", exc);
                clients.remove(userId);
            }
        } else {
            log.trace("userId={} is not in client list. The notification will be stored in database", userId);
        }
    }

}
