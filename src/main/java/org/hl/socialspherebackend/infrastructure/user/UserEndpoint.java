package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.api.dto.user.request.UserProfileConfigRequest;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
import org.hl.socialspherebackend.api.dto.user.response.*;
import org.hl.socialspherebackend.application.user.UserFacade;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/user")
public class UserEndpoint {

    private final UserFacade userFacade;

    public UserEndpoint(UserFacade userFacade) {
        this.userFacade = userFacade;
    }


    @PostMapping("/profile")
    public ResponseEntity<UserProfileResult> createUserProfile(
            @RequestParam Long userId,
            @RequestParam("profilePicture") MultipartFile profilePicture,
            @RequestBody UserProfileRequest request
    ) {
        UserProfileResult userProfileResult = userFacade.createUserProfile(userId, request, profilePicture);

        return userProfileResult.isSuccess() ?
                ResponseEntity.ok(userProfileResult) :
                ResponseEntity.badRequest().body(userProfileResult);
    }

    @PostMapping("/profile/config")
    public ResponseEntity<UserProfileConfigResult> createUserProfileConfig(
            @RequestParam Long userId,
            @RequestBody UserProfileConfigRequest request
    ) {
        UserProfileConfigResult userProfileConfigResult = userFacade.createUserProfileConfig(userId, request);

        return userProfileConfigResult.isSuccess() ?
                ResponseEntity.ok(userProfileConfigResult) :
                ResponseEntity.badRequest().body(userProfileConfigResult);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResult> findUserById(@PathVariable Long id, @RequestParam(required = false) Long currentUserId) {
        UserResult userResult = userFacade.findUserById(id, currentUserId);

        return userResult.isSuccess() ?
                ResponseEntity.ok(userResult) :
                ResponseEntity.badRequest().body(userResult);
    }


    @GetMapping("/profile")
    public ResponseEntity<UserProfileResult> findUserProfileByUserId(@RequestParam Long userId) {
        UserProfileResult userProfileResult = userFacade.findUserProfileByUserId(userId);

        return userProfileResult.isSuccess() ?
                ResponseEntity.ok(userProfileResult) :
                ResponseEntity.badRequest().body(userProfileResult);
    }

    @GetMapping("/profile/picture")
    public ResponseEntity<byte[]> findProfilePictureByUserId(@RequestParam Long userId) {
        byte[] response = userFacade.findUserProfilePictureByUserId(userId);

        return response != null ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().build();

    }

    @GetMapping("/profile/config")
    public ResponseEntity<UserProfileConfigResult> findUserProfileConfigByUserId(@RequestParam Long userId) {
         UserProfileConfigResult userProfileConfigResult = userFacade.findUserProfileConfigByUserId(userId);

        return userProfileConfigResult.isSuccess() ?
                ResponseEntity.ok(userProfileConfigResult) :
                ResponseEntity.badRequest().body(userProfileConfigResult);
    }

    @GetMapping("/friend")
    public ResponseEntity<?> findUserFriends(
            @RequestParam(required = false, defaultValue = "-1") Long currentUserId,
            @RequestParam Long userId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {

        if(page == null || size == null) {
            UserFriendListResult userFriendListResult = userFacade.findUserFriends(userId);

            return userFriendListResult.isSuccess() ?
                    ResponseEntity.ok(userFriendListResult) :
                    ResponseEntity.badRequest().body(userFriendListResult);
        }

        Page<UserFriendResponse> userFriendPage;
        if (currentUserId.equals(-1L)) {
            userFriendPage = userFacade.findUserFriends(userId, page, size);
        } else {
            userFriendPage = userFacade.findCheckedUserFriends(currentUserId, userId, page, size);
        }

        return userFriendPage.isEmpty() ?
                new ResponseEntity<>(userFriendPage, HttpStatus.NO_CONTENT) :
                new ResponseEntity<>(userFriendPage, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<SearchUsersResult> searchFriends(
            @RequestParam Long userId,
            @RequestParam String containsString,
            @RequestParam Integer maxSize
    ) {
        SearchUsersResult searchUsersResult = userFacade.findUsers(userId, containsString, maxSize);

        return searchUsersResult.isSuccess() ?
                ResponseEntity.ok(searchUsersResult) :
                ResponseEntity.badRequest().body(searchUsersResult);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResult> updateUserProfile(
            @RequestParam Long userId,
            @RequestPart("profilePicture") MultipartFile profilePicture,
            @RequestPart("request") UserProfileRequest request
    ) {
        UserProfileResult userProfileResult = userFacade.updateUserProfile(userId, request, profilePicture);

        return userProfileResult.isSuccess() ?
                ResponseEntity.ok(userProfileResult) :
                ResponseEntity.badRequest().body(userProfileResult);
    }


    @PutMapping("/profile/config")
    public ResponseEntity<UserProfileConfigResult> updateUserConfig(
            @RequestParam Long userId,
            @RequestBody UserProfileConfigRequest request
    ) {
        UserProfileConfigResult userProfileConfigResult = userFacade.updateUserProfileConfig(userId, request);

        return userProfileConfigResult.isSuccess() ?
                ResponseEntity.ok(userProfileConfigResult) :
                ResponseEntity.badRequest().body(userProfileConfigResult);
    }

    @DeleteMapping("/friend/remove")
    public ResponseEntity<?> removeFriendFromFriendList(@RequestParam Long userId, @RequestParam Long friendId) {
        boolean isSuccess = userFacade.removeFromFriendList(userId, friendId);

        return isSuccess ? ResponseEntity.ok(null)
                : ResponseEntity.badRequest().body(null);
    }

}
