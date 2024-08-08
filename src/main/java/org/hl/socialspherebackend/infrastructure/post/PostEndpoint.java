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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    public ResponseEntity<DataResult<?, ?>> createPost(@RequestBody PostRequest request) {
        DataResult<?, ?> result = postFacade.createPost(request);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @PostMapping(value = "/comment")
    public ResponseEntity<DataResult<?, ?>>  createPostComment(@RequestBody PostCommentRequest request) {
        DataResult<?, ?> result = postFacade.addCommentToPost(request);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }


    @PostMapping(value = "/like/add")
    public ResponseEntity<DataResult<?, ?>> addLikeToPost(@RequestBody PostLikeRequest request) {
        DataResult<?, ?> result = postFacade.addLikeToPost(request);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }


    @GetMapping
    public ResponseEntity<DataResult<?, ?>> findUserPosts(
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
    public ResponseEntity<DataResult<?, ?>> findRecentPostsAvailableForUser(
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
    public ResponseEntity<DataResult<?, ?>> findPostComments(
            @RequestParam Long postId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        DataResult<?, ?> result = postFacade.findPostComments(postId, page, size);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

    @DeleteMapping(value = "/like/remove")
    public ResponseEntity<DataResult<?, ?>> removeLikeFromPost(@RequestParam Long postId, @RequestParam Long userId) {
        DataResult<?, ?> result = postFacade.removeLikeFromPost(postId, userId);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }

}
