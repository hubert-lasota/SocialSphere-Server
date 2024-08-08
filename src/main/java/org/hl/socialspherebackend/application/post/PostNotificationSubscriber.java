package org.hl.socialspherebackend.application.post;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface PostNotificationSubscriber {

    SseEmitter subscribe(Long userId);

}
