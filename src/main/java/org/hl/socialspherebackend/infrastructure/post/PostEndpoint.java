package org.hl.socialspherebackend.infrastructure.post;

import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.dto.post.request.PostCommentRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostLikeRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostRequest;
import org.hl.socialspherebackend.application.post.PostFacade;
import org.hl.socialspherebackend.application.post.PostNotificationFacade;
import org.hl.socialspherebackend.application.post.PostNotificationSubscriber;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/post")
public class PostEndpoint {

    private final PostFacade postFacade;
    private final PostNotificationFacade postNotificationFacade;
    private final PostNotificationSubscriber postNotificationSubscriber;

    public PostEndpoint(PostFacade postFacade, PostNotificationFacade postNotificationFacade, PostNotificationSubscriber notificationSubscriber) {
        this.postFacade = postFacade;
        this.postNotificationFacade = postNotificationFacade;
        this.postNotificationSubscriber = notificationSubscriber;
    }


    @GetMapping(value = "/notification/subscribe")
    public SseEmitter subscribePosts() {
        return postNotificationSubscriber.subscribe();
    }

    @GetMapping(value = "/notification")
    public ResponseEntity<?> findCurrentUserPostUpdateNotifications() {
        DataResult<?> result = postNotificationFacade.findCurrentUserPostUpdateNotifications();

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestPart("request") PostRequest request,
                                        @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        DataResult<?> result = postFacade.createPost(request, images);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @PostMapping(value = "/comment")
    public ResponseEntity<?>  createPostComment(@RequestBody PostCommentRequest request) {
        DataResult<?> result = postFacade.addCommentToPost(request);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }


    @PostMapping(value = "/like/add")
    public ResponseEntity<?> addLikeToPost(@RequestBody PostLikeRequest request) {
        DataResult<?> result = postFacade.addLikeToPost(request);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }


    @GetMapping
    public ResponseEntity<?> findPosts(
            @RequestParam(required = false) Long userId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        DataResult<?> result = userId == null ?
                postFacade.findCurrentUserPosts(page, size) :
                postFacade.findUserPosts(userId, page, size);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @GetMapping("/recent")
    public ResponseEntity<?> findRecentPostsAvailableForCurrentUser(
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        DataResult<?> result = postFacade.findRecentPostsAvailableForCurrentUser(page, size);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @GetMapping(value = "/comment")
    public ResponseEntity<?> findPostComments(
            @RequestParam Long postId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        DataResult<?> result = postFacade.findPostComments(postId, page, size);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }


    @PutMapping(value = "/{id}")
    public ResponseEntity<?> updatePost(@PathVariable(value = "id") Long postId,
                                        @RequestPart(value = "request") PostRequest request,
                                        @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        DataResult<?> result = postFacade.updatePost(postId, request, images);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @PutMapping(value = "/comment/{id}")
    public ResponseEntity<?> updatePostComment(@PathVariable(value = "id") Long postCommentId, @RequestBody PostCommentRequest request) {
        DataResult<?> result = postFacade.updatePostComment(postCommentId, request);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }


    @DeleteMapping(value = "/like/remove")
    public ResponseEntity<?> removeLikeFromPost(@RequestParam Long postId) {
        DataResult<?> result = postFacade.removeLikeFromPost(postId);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deletePost(@PathVariable(value = "id") Long postId) {
        DataResult<?> result = postFacade.deletePost(postId);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @DeleteMapping(value = "/comment/{id}")
    public ResponseEntity<?> deletePostComment(@PathVariable(value = "id") Long postCommentId) {
        DataResult<?> result = postFacade.deletePostComment(postCommentId);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

}
