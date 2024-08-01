package org.hl.socialspherebackend.infrastructure.notification;

import org.hl.socialspherebackend.api.dto.notification.request.UserFriendRequestDto;
import org.hl.socialspherebackend.api.dto.notification.response.UserFriendRequestResult;
import org.hl.socialspherebackend.application.notification.PostNotificationSubscriber;
import org.hl.socialspherebackend.application.notification.UserFriendRequestFacade;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping(value = "/api/v1")
public class NotificationController {

    private final PostNotificationSubscriber postNotificationSubscriber;
    private final UserFriendRequestFacade userFriendRequestFacade;

    public NotificationController(PostNotificationSubscriber postNotificationSubscriber,
                                  UserFriendRequestFacade userFriendRequestFacade) {
        this.postNotificationSubscriber = postNotificationSubscriber;
        this.userFriendRequestFacade = userFriendRequestFacade;
    }

    @GetMapping(value = "/post/notification/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribePosts(@RequestParam Long userId) {
        return postNotificationSubscriber.subscribe(userId);
    }

    @PostMapping("/friend/send")
    public ResponseEntity<UserFriendRequestResult> sendFriendRequest(UserFriendRequestDto request) {
        UserFriendRequestResult userFriendRequestResult = userFriendRequestFacade.sendFriendRequest(request);

        return userFriendRequestResult.isSuccess() ?
                ResponseEntity.ok(userFriendRequestResult) :
                ResponseEntity.badRequest().body(userFriendRequestResult);
    }


    @PostMapping("/friend/accept")
    public ResponseEntity<UserFriendRequestResult> acceptFriendRequest(UserFriendRequestDto request) {
        UserFriendRequestResult userFriendRequestResult = userFriendRequestFacade.acceptFriendRequest(request);

        return userFriendRequestResult.isSuccess() ?
                ResponseEntity.ok(userFriendRequestResult) :
                ResponseEntity.badRequest().body(userFriendRequestResult);
    }

    @PostMapping("/friend/reject")
    public ResponseEntity<UserFriendRequestResult> rejectFriendRequest(UserFriendRequestDto request) {

        UserFriendRequestResult userFriendRequestResult = userFriendRequestFacade.rejectFriendRequest(request);

        return userFriendRequestResult.isSuccess() ?
                ResponseEntity.ok(userFriendRequestResult) :
                ResponseEntity.badRequest().body(userFriendRequestResult);
    }

}
