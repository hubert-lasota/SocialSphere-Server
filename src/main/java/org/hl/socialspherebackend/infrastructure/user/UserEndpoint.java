package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.dto.user.request.UserFriendRequestDto;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileConfigRequest;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
import org.hl.socialspherebackend.application.user.UserFacade;
import org.hl.socialspherebackend.application.user.UserFriendRequestNotificationSubscriber;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/user")
public class UserEndpoint {

    private final UserFacade userFacade;
    private final UserFriendRequestNotificationSubscriber notificationSubscriber;

    public UserEndpoint(UserFacade userFacade, UserFriendRequestNotificationSubscriber notificationSubscriber) {
        this.userFacade = userFacade;
        this.notificationSubscriber = notificationSubscriber;
    }


    @GetMapping(value = "/friend/notification/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeFriendRequests(@RequestParam Long userId) {
        return notificationSubscriber.subscribe(userId);
    }

    @PostMapping("/friend/send")
    public ResponseEntity<DataResult<?, ?>> sendFriendRequest(UserFriendRequestDto request) {
        DataResult<?, ?> result = userFacade.sendFriendRequest(request);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }


    @PostMapping("/friend/accept")
    public ResponseEntity<DataResult<?, ?>> acceptFriendRequest(UserFriendRequestDto request) {
        DataResult<?, ?> result = userFacade.acceptFriendRequest(request);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/friend/reject")
    public ResponseEntity<DataResult<?, ?>> rejectFriendRequest(UserFriendRequestDto request) {
        DataResult<?, ?> result = userFacade.rejectFriendRequest(request);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/profile")
    public ResponseEntity<DataResult<?, ?>> createUserProfile(
            @RequestParam Long userId,
            @RequestParam("profilePicture") MultipartFile profilePicture,
            @RequestBody UserProfileRequest request
    ) {
        DataResult<?, ?> result = userFacade.createUserProfile(userId, request, profilePicture);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/profile/config")
    public ResponseEntity<DataResult<?, ?>> createUserProfileConfig(
            @RequestParam Long userId,
            @RequestBody UserProfileConfigRequest request
    ) {
        DataResult<?, ?> result = userFacade.createUserProfileConfig(userId, request);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataResult<?, ?>> findUserById(@PathVariable Long id, @RequestParam(required = false) Long currentUserId) {
        DataResult<?, ?> result = userFacade.findUserById(id, currentUserId);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }


    @GetMapping("/profile")
    public ResponseEntity<DataResult<?, ?>> findUserProfileByUserId(@RequestParam Long userId) {
        DataResult<?, ?> result = userFacade.findUserProfileByUserId(userId);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/profile/picture")
    public ResponseEntity<DataResult<?, ?>> findProfilePictureByUserId(@RequestParam Long userId) {
        DataResult<?, ?> result = userFacade.findUserProfilePictureByUserId(userId);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/profile/config")
    public ResponseEntity<DataResult<?, ?>> findUserProfileConfigByUserId(@RequestParam Long userId) {
        DataResult<?, ?> result = userFacade.findUserProfileConfigByUserId(userId);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/friend")
    public ResponseEntity<DataResult<?, ?>> findUserFriends(
            @RequestParam(required = false, defaultValue = "-1") Long currentUserId,
            @RequestParam Long userId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {

        if(page == null || size == null) {
            DataResult<?, ?> userFriendListResult = userFacade.findUserFriends(userId);

            return userFriendListResult.isSuccess() ?
                    ResponseEntity.ok(userFriendListResult) :
                    ResponseEntity.badRequest().body(userFriendListResult);
        }

        DataResult<?, ?> result;
        if (currentUserId.equals(-1L)) {
            result = userFacade.findUserFriends(userId, page, size);
        } else {
            result = userFacade.findCheckedUserFriends(currentUserId, userId, page, size);
        }

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/search")
    public ResponseEntity<DataResult<?, ?>> searchFriends(
            @RequestParam Long userId,
            @RequestParam String containsString,
            @RequestParam Integer maxSize
    ) {
        DataResult<?, ?> result = userFacade.findUsers(userId, containsString, maxSize);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PutMapping("/profile")
    public ResponseEntity<DataResult<?, ?>> updateUserProfile(
            @RequestParam Long userId,
            @RequestPart("profilePicture") MultipartFile profilePicture,
            @RequestPart("request") UserProfileRequest request
    ) {
        DataResult<?, ?> result = userFacade.updateUserProfile(userId, request, profilePicture);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }


    @PutMapping("/profile/config")
    public ResponseEntity<DataResult<?, ?>> updateUserConfig(
            @RequestParam Long userId,
            @RequestBody UserProfileConfigRequest request
    ) {
        DataResult<?, ?> result = userFacade.updateUserProfileConfig(userId, request);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @DeleteMapping("/friend/remove")
    public ResponseEntity<?> removeFriendFromFriendList(@RequestParam Long userId, @RequestParam Long friendId) {
        DataResult<?, ?> result = userFacade.removeFromFriendList(userId, friendId);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

}
