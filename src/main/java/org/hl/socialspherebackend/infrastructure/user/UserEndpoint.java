package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.dto.user.request.*;
import org.hl.socialspherebackend.application.user.UserFacade;
import org.hl.socialspherebackend.application.user.UserFriendRequestNotificationSubscriber;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserEndpoint {

    private final UserFacade userFacade;
    private final UserFriendRequestNotificationSubscriber notificationSubscriber;

    public UserEndpoint(UserFacade userFacade, UserFriendRequestNotificationSubscriber notificationSubscriber) {
        this.userFacade = userFacade;
        this.notificationSubscriber = notificationSubscriber;
    }


    @GetMapping(value = "/friend/notification/subscribe")
    public SseEmitter subscribeFriendRequests() {
        return notificationSubscriber.subscribe();
    }

    @GetMapping(value = "/friend/notification")
    public ResponseEntity<?> findCurrentUserFriendRequests() {
        DataResult<?> result = userFacade.findCurrentUserFriendNotifications();

        return new ResponseEntity<>(result, result.getHttpStatus());
    }


    @PostMapping(value = "/friend/send")
    public ResponseEntity<?> sendFriendRequest(@RequestBody UserFriendRequestDto userFriendRequestDto) {
        DataResult<?> result = userFacade.sendFriendRequest(userFriendRequestDto);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @PostMapping(value = "/profile")
    public ResponseEntity<?> createUserProfile(
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart("request") UserProfileRequest request
    ) {
        DataResult<?> result = userFacade.createUserProfile(request, profilePicture);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @PostMapping(value = "/profile/config")
    public ResponseEntity<?> createUserProfileConfig(@RequestBody UserProfileConfigRequest request) {
        DataResult<?> result = userFacade.createUserProfileConfig(request);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }


    @GetMapping
    public ResponseEntity<?> findCurrentUser(
            @RequestParam(value = "header", required = false, defaultValue = "false") boolean headerResponse
    ) {
        DataResult<?> result = headerResponse ? userFacade.findCurrentUserHeader() : userFacade.findCurrentUser();

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> findUser(@PathVariable Long id) {
        DataResult<?> result = userFacade.findUserById(id);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }


    @GetMapping(value = "/profile")
    public ResponseEntity<?> findUserProfile(@RequestParam(required = false) Long userId) {
        DataResult<?> result;
        if(userId != null) {
            result = userFacade.findUserProfileByUserId(userId);
        } else {
            result = userFacade.findCurrentUserProfile();
        }

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @GetMapping(value = "/profile/picture")
    public ResponseEntity<?> findCurrentUserProfilePicture() {
        DataResult<?> result = userFacade.findCurrentUserProfilePicture();

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @GetMapping(value = "/profile/config")
    public ResponseEntity<?> findUserProfileConfig(@RequestParam(required = false) Long userId) {
        DataResult<?> result;
        if(userId != null) {
            result = userFacade.findUserProfileConfigByUserId(userId);
        } else {
            result = userFacade.findCurrentUserProfileConfig();
        }
        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @GetMapping(value = "/friend")
    public ResponseEntity<?> findUserFriends(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "false") boolean noSharedChat,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        DataResult<?> result;
         if(noSharedChat) {
             result = userFacade.findCurrentUserFriendsWithNoSharedChat();
        } else if(page == null || size == null) {
            result = userFacade.findCurrentUserFriends();
        } else if (userId == null) {
            result = userFacade.findCurrentUserFriends(page, size);
        } else {
            result = userFacade.findUserFriends(userId, page, size);
        }

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @GetMapping(value = "/friend/search")
    public ResponseEntity<?> searchFriends(@RequestParam Map<String, String> params,
                                           @RequestParam SearchFriendsRelationshipStatus relationshipStatus,
                                           @RequestParam int page,
                                           @RequestParam int size
    ) {
        SearchFriendsRequest request = new SearchFriendsRequest(params.get("firstNamePattern"),
                params.get("lastNamePattern"),
                params.get("cityPattern"),
                params.get("countryPattern"),
                relationshipStatus);

        DataResult<?> result = userFacade.searchFriends(request, page, size);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @GetMapping(value = "/friend/request/toCurrentUser")
    public ResponseEntity<?> findUserFriendRequestForCurrentUser(@RequestParam Long userId) {
        DataResult<?> result = userFacade.findUserFriendRequestForCurrentUser(userId);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @GetMapping(value = "/friend/isCurrentUserWaiting")
    public ResponseEntity<?> isCurrentUserWaitingForFriendResponse(@RequestParam Long userId) {
        DataResult<?> result = userFacade.isCurrentUserWaitingForFriendResponse(userId);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @GetMapping(value = "/friend/isUserWaiting")
    public ResponseEntity<?> isUserWaitingForCurrentUserFriendResponse(@RequestParam Long userId) {
        DataResult<?> result = userFacade.isUserWaitingForCurrentUserFriendResponse(userId);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @GetMapping(value = "/isAllowed")
    public ResponseEntity<?> isCurrentUserHasPermissionToCheckProfile(@RequestParam Long userId) {
        DataResult<?> result = userFacade.isCurrentUserHasPermissionToCheckProfile(userId);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchFriends(
            @RequestParam String pattern,
            @RequestParam Integer size
    ) {
        DataResult<?> result = userFacade.findUsers(pattern, size);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart("request") UserProfileRequest request) {
        DataResult<?> result = userFacade.updateCurrentUserProfile(request, profilePicture);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }


    @PutMapping("/profile/config")
    public ResponseEntity<?> updateUserConfig(@RequestBody UserProfileConfigRequest request) {
        DataResult<?> result = userFacade.updateCurrentUserProfileConfig(request);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @PatchMapping("/friend/accept")
    public ResponseEntity<?> acceptFriendRequest(@RequestParam Long friendRequestId) {
        DataResult<?> result = userFacade.acceptFriendRequest(friendRequestId);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @PatchMapping("/friend/reject")
    public ResponseEntity<?> rejectFriendRequest(@RequestParam Long friendRequestId) {
        DataResult<?> result = userFacade.rejectFriendRequest(friendRequestId);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @DeleteMapping("/friend/remove")
    public ResponseEntity<?> removeFriendFromFriendList(@RequestParam Long friendId) {
        DataResult<?> result = userFacade.removeFromFriendList(friendId);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

}
