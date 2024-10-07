package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.post.response.PostUpdateDetails;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.application.common.Observer;
import org.hl.socialspherebackend.application.util.AuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PostNotificationManager implements Observer<PostUpdateDetails>, PostNotificationSubscriber {

    private static final Logger log = LoggerFactory.getLogger(PostNotificationManager.class);

    private final ConcurrentMap<Long, SseEmitter> clients = new ConcurrentHashMap<>();

    private final PostNotificationFacade postNotificationFacade;

    public PostNotificationManager(PostNotificationFacade postNotificationFacade) {
        this.postNotificationFacade = postNotificationFacade;
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

    @Override
    public void update(PostUpdateDetails subject) {
        if(subject == null) {
            log.debug("Subject is null");
            return;
        }

        postNotificationFacade.savePostUpdateNotification(subject);
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            log.debug("Could not find current user in security context");
            return;
        }

        Long currentUserId = currentUserOpt.get().getId();
        if(currentUserId.equals(subject.updatedBy().userId())) {
            log.debug("Won't send notification because current user is receiver");
            return;
        }

        if(sendNotification(subject)) {
           log.debug("Successfully sent notification: {}", subject);
       } else {
           log.debug("Could not send notification: {}", subject);
       }
    }

    private boolean sendNotification(PostUpdateDetails subject) {
        Long userId = subject.updatedPost().userId();
        SseEmitter sseEmitter = clients.get(userId);
        if(sseEmitter != null) {
            try {
                sseEmitter.send(subject);
                return true;
            } catch (IOException exc) {
                log.debug("Error occurred while sending notification. Client is removed from list", exc);
                clients.remove(userId);
                return false;
            }
        } else {
            log.trace("userId={} is not in client list. The notification will be stored in database", userId);
            return false;
        }
    }

}
