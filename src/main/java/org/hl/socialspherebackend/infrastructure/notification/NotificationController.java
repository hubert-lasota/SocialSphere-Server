package org.hl.socialspherebackend.infrastructure.notification;

import org.hl.socialspherebackend.application.notification.PostNotificationSubscriber;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping(value = "/api/v1/notification")
public class NotificationController {

    private final PostNotificationSubscriber postNotificationSubscriber;

    public NotificationController(PostNotificationSubscriber postNotificationSubscriber) {
        this.postNotificationSubscriber = postNotificationSubscriber;
    }

    @GetMapping(value = "/post/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribePosts(@RequestParam Long userId) {
        return postNotificationSubscriber.subscribe(userId);
    }

}
