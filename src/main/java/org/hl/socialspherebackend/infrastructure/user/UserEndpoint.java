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
    public ResponseEntity<?> sendFriendRequest(@RequestBody UserFriendRequestDto userFriendRequestDto) {
        DataResult<?> result = userFacade.sendFriendRequest(userFriendRequestDto);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/friend/accept")
    public ResponseEntity<?> acceptFriendRequest(UserFriendRequestDto request) {
        DataResult<?> result = userFacade.acceptFriendRequest(request);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/friend/reject")
    public ResponseEntity<?> rejectFriendRequest(UserFriendRequestDto request) {
        DataResult<?> result = userFacade.rejectFriendRequest(request);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/profile")
    public ResponseEntity<?> createUserProfile(
            @RequestParam("profilePicture") MultipartFile profilePicture,
            @RequestBody UserProfileRequest request
    ) {
        DataResult<?> result = userFacade.createUserProfile(request, profilePicture);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/profile/config")
    public ResponseEntity<?> createUserProfileConfig(@RequestBody UserProfileConfigRequest request) {
        DataResult<?> result = userFacade.createUserProfileConfig(request);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }


    @GetMapping
    public ResponseEntity<?> findCurrentUser() {
        DataResult<?> result = userFacade.findCurrentUser();

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findUser(@PathVariable Long id) {
        DataResult<?> result = userFacade.findUserById(id);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }


    @GetMapping("/profile")
    public ResponseEntity<?> findUserProfile(@RequestParam(required = false) Long userId) {
        DataResult<?> result;
        if(userId != null) {
            result = userFacade.findUserProfileByUserId(userId);
        } else {
            result = userFacade.findCurrentUserProfile();
        }

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/profile/picture")
    public ResponseEntity<?> findProfilePicture() {
        DataResult<?> result = userFacade.findCurrentUserProfilePicture();

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/profile/config")
    public ResponseEntity<?> findUserProfileConfig(@RequestParam(required = false) Long userId) {
        DataResult<?> result;
        if(userId != null) {
            result = userFacade.findUserProfileConfigByUserId(userId);
        } else {
            result = userFacade.findCurrentUserProfileConfig();
        }
        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/friend")
    public ResponseEntity<?> findUserFriends(
            @RequestParam(required = false) Long userId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        DataResult<?> result;
        if(page == null || size == null) {
            result = userFacade.findCurrentUserFriends();
        } else if (userId == null) {
            result = userFacade.findCurrentUserFriends(page, size);
        } else {
            result = userFacade.findUserFriends(userId, page, size);
        }

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchFriends(
            @RequestParam String containsString,
            @RequestParam Integer maxSize
    ) {
        DataResult<?> result = userFacade.findUsers(containsString, maxSize);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestPart("profilePicture") MultipartFile profilePicture,
            @RequestPart("request") UserProfileRequest request) {
        DataResult<?> result = userFacade.updateCurrentUserProfile(request, profilePicture);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }


    @PutMapping("/profile/config")
    public ResponseEntity<?> updateUserConfig(@RequestBody UserProfileConfigRequest request) {
        DataResult<?> result = userFacade.updateCurrentUserProfileConfig(request);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @DeleteMapping("/friend/remove")
    public ResponseEntity<?> removeFriendFromFriendList(@RequestParam Long friendId) {
        DataResult<?> result = userFacade.removeFromFriendList(friendId);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

}
