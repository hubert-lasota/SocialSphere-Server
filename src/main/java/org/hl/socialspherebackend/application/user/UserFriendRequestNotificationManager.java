package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.user.response.UserFriendRequestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserFriendRequestNotificationManager implements UserFriendRequestNotificationSender, UserFriendRequestNotificationSubscriber {

    private static final Logger log = LoggerFactory.getLogger(UserFriendRequestNotificationManager.class);

    private final ConcurrentMap<Long, SseEmitter> clients = new ConcurrentHashMap<>();

    @Override
    public void send(UserFriendRequestResponse notification) {
        if(notification == null) {
            log.debug("Notification is null");
            return;
        }
        sendNotification(notification);
    }

    @Override
    public SseEmitter subscribe(Long userId) {
        SseEmitter sseEmitter = new SseEmitter();
        clients.put(userId, sseEmitter);

        sseEmitter.onCompletion(() -> clients.remove(userId));
        sseEmitter.onTimeout(() -> clients.remove(userId));

        return sseEmitter;
    }

    private void sendNotification(UserFriendRequestResponse notification) {
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
