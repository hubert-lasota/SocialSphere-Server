package org.hl.socialspherebackend.infrastructure.post;

import org.hl.socialspherebackend.api.dto.post.request.PostCommentRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostLikeRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostRequest;
import org.hl.socialspherebackend.api.dto.post.response.*;
import org.hl.socialspherebackend.application.post.PostFacade;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/post")
public class PostEndpoint {

    private final PostFacade postFacade;

    public PostEndpoint(PostFacade postFacade) {
        this.postFacade = postFacade;
    }


    @PostMapping
    public ResponseEntity<PostResult> createPost(@RequestBody PostRequest request) {
        PostResult postResult = postFacade.createPost(request);

        return postResult.isSuccess() ?
                ResponseEntity.ok(postResult) :
                ResponseEntity.badRequest().body(postResult);
    }

    @PostMapping(value = "/comment")
    public ResponseEntity<PostCommentResult> createPostComment(@RequestBody PostCommentRequest request) {
        PostCommentResult postCommentResult = postFacade.addCommentToPost(request);

        return postCommentResult.isSuccess() ?
                ResponseEntity.ok(postCommentResult) :
                ResponseEntity.badRequest().body(postCommentResult);
    }


    @PostMapping(value = "/like/add")
    public ResponseEntity<PostLikeResult> addLikeToPost(@RequestBody PostLikeRequest request) {
        PostLikeResult postLikeResult = postFacade.addLikeToPost(request);

        return postLikeResult.isSuccess() ?
                ResponseEntity.ok(postLikeResult) :
                ResponseEntity.badRequest().body(postLikeResult);
    }


    @GetMapping
    public ResponseEntity<Page<PostResponse>> findUserPosts(
            @RequestParam Long currentUserId,
            @RequestParam(required = false, defaultValue = "-1") Long userToCheckId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        Page<PostResponse> response = userToCheckId.equals(-1L) ?
                postFacade.findCurrentUserPosts(currentUserId, page, size) :
                postFacade.findUserPosts(currentUserId, userToCheckId, page, size);

        return response.isEmpty() ?
                new ResponseEntity<>(response, HttpStatus.NO_CONTENT) :
                new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @GetMapping("/recent")
    public ResponseEntity<Page<PostResponse>> findRecentPostsAvailableForUser(
            @RequestParam Long userId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        Page<PostResponse> pageOfPostResponse = postFacade.findRecentPostsAvailableForUser(userId, page, size);

        return pageOfPostResponse.isEmpty() ?
                new ResponseEntity<>(pageOfPostResponse, HttpStatus.NO_CONTENT) :
                new ResponseEntity<>(pageOfPostResponse, HttpStatus.OK);
    }

    @GetMapping(value = "/comment")
    public ResponseEntity<Page<PostCommentResponse>> findPostComments(
            @RequestParam Long postId,
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        Page<PostCommentResponse> response = postFacade.findPostComments(postId, page, size);

        return response.isEmpty() ?
                new ResponseEntity<>(response, HttpStatus.NO_CONTENT) :
                new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping(value = "/like/remove")
    public ResponseEntity<PostLikeResult> removeLikeFromPost(@RequestParam Long postId, @RequestParam Long userId) {
        PostLikeResult postLikeResult = postFacade.removeLikeFromPost(postId, userId);

        return postLikeResult.isSuccess() ?
                ResponseEntity.ok(postLikeResult) :
                ResponseEntity.badRequest().body(postLikeResult);
    }

}
