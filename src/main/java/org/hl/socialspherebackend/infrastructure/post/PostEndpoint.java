package org.hl.socialspherebackend.infrastructure.post;

import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.dto.post.request.PostCommentRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostLikeRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostRequest;
import org.hl.socialspherebackend.application.post.PostFacade;
import org.hl.socialspherebackend.application.post.PostNotificationSubscriber;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/post")
public class PostEndpoint {

    private final PostFacade postFacade;
    private final PostNotificationSubscriber postNotificationSubscriber;

    public PostEndpoint(PostFacade postFacade, PostNotificationSubscriber notificationSubscriber) {
        this.postFacade = postFacade;
        this.postNotificationSubscriber = notificationSubscriber;
    }


    @GetMapping(value = "/notification/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribePosts(@RequestParam Long userId) {
        return postNotificationSubscriber.subscribe(userId);
    }


    @PostMapping
    public ResponseEntity<?> createPost(@RequestPart("request") PostRequest request,
                                                       @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        DataResult<?, ?> result = postFacade.createPost(request, images);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PostMapping(value = "/comment")
    public ResponseEntity<?>  createPostComment(@RequestBody PostCommentRequest request) {
        DataResult<?, ?> result = postFacade.addCommentToPost(request);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }


    @PostMapping(value = "/like/add")
    public ResponseEntity<?> addLikeToPost(@RequestBody PostLikeRequest request) {
        DataResult<?, ?> result = postFacade.addLikeToPost(request);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }


    @GetMapping
    public ResponseEntity<?> findUserPosts(
            @RequestParam Long currentUserId,
            @RequestParam(required = false, defaultValue = "-1") Long userToCheckId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        DataResult<?, ?> result = userToCheckId.equals(-1L) ?
                postFacade.findCurrentUserPosts(currentUserId, page, size) :
                postFacade.findUserPosts(currentUserId, userToCheckId, page, size);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }
    
    @GetMapping("/recent")
    public ResponseEntity<?> findRecentPostsAvailableForUser(
            @RequestParam Long userId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        DataResult<?, ?> result = postFacade.findRecentPostsAvailableForUser(userId, page, size);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @GetMapping(value = "/comment")
    public ResponseEntity<?> findPostComments(
            @RequestParam Long postId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        DataResult<?, ?> result = postFacade.findPostComments(postId, page, size);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }


    @PutMapping(value = "/{id}")
    public ResponseEntity<?> updatePost(@PathVariable(value = "id") Long postId,
                                        @RequestPart(value = "request") PostRequest request,
                                        @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        DataResult<?, ?> result = postFacade.updatePost(postId, request, images);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PutMapping(value = "/comment/{id}")
    public ResponseEntity<?> updatePostComment(@PathVariable(value = "id") Long postCommentId, @RequestBody PostCommentRequest request) {
        DataResult<?, ?> result = postFacade.updatePostComment(postCommentId, request);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }


    @DeleteMapping(value = "/like/remove")
    public ResponseEntity<?> removeLikeFromPost(@RequestParam Long postId, @RequestParam Long userId) {
        DataResult<?, ?> result = postFacade.removeLikeFromPost(postId, userId);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deletePost(@PathVariable(value = "id") Long postId, @RequestParam Long userId) {
        DataResult<?, ?> result = postFacade.deletePost(postId, userId);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @DeleteMapping(value = "/comment/{id}")
    public ResponseEntity<?> deletePostComment(@PathVariable(value = "id") Long postCommentId, @RequestParam Long userId) {
        DataResult<?, ?> result = postFacade.deletePostComment(postCommentId, userId);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

}
