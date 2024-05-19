package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.post.request.PostCommentRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostLikeRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostRequest;
import org.hl.socialspherebackend.api.dto.post.response.*;
import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.post.PostComment;
import org.hl.socialspherebackend.api.entity.post.PostImage;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.api.entity.user.UserConfig;
import org.hl.socialspherebackend.api.entity.user.UserPrivacyLevel;
import org.hl.socialspherebackend.infrastructure.post.PostCommentRepository;
import org.hl.socialspherebackend.infrastructure.post.PostImageRepository;
import org.hl.socialspherebackend.infrastructure.post.PostRepository;
import org.hl.socialspherebackend.infrastructure.user.UserConfigRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

public class PostFacade {

    private static final Logger log = LoggerFactory.getLogger(PostFacade.class);

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostImageRepository postImageRepository;
    private final UserRepository userRepository;
    private final UserConfigRepository userConfigRepository;

    public PostFacade(PostRepository postRepository,
                      PostCommentRepository postCommentRepository,
                      PostImageRepository postImageRepository,
                      UserRepository userRepository,
                      UserConfigRepository userConfigRepository) {

        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.postImageRepository = postImageRepository;
        this.userRepository = userRepository;
        this.userConfigRepository = userConfigRepository;
    }



    public PostResult createPost(PostRequest postRequest, Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return PostResult.failure(PostResult.Code.CANNOT_CREATE,
                    "Could not find user with id = %d in database!".formatted(userId));
        }

        User user = userOpt.get();
        Post post = PostMapper.fromRequestToEntity(postRequest);
        post.setUser(user);
        post.setLikeCount(0L);
        post.setCommentCount(0L);
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());
        Set<PostImage> postImages = PostMapper.fromRequestToEntities(postRequest.images());
        postImages.forEach(img -> {
            post.getImages().add(img);
            img.setPost(post);
            postImageRepository.save(img);
        });

        postRepository.save(post);
        PostResponse response = PostMapper.fromEntityToResponse(post);
        return PostResult.success(response, PostResult.Code.CREATED);
    }




    public PostCommentResult addCommentToPost(PostCommentRequest request) {
        Optional<User> authorOpt = userRepository.findById(request.authorId());
        Optional<Post> postOpt = postRepository.findById(request.postId());

        if(authorOpt.isEmpty()) {
            return PostCommentResult.failure(PostCommentResult.Code.CANNOT_CREATE,
                    "Could not find author with id = %d in database!".formatted(request.authorId()));
        }

        if(postOpt.isEmpty()) {
            return PostCommentResult.failure(PostCommentResult.Code.CANNOT_CREATE,
                    "Could not find post with id = %d in database!".formatted(request.postId()));
        }

        Post post = postOpt.get();
        Long incrementPostCommentCount = post.getLikeCount() + 1L;
        post.setCommentCount(incrementPostCommentCount);

        PostComment postComment =
                PostCommentMapper.fromRequestToEntity(postOpt.get(), authorOpt.get(), request.content());
        postComment.setCreatedAt(Instant.now());
        postComment.setUpdatedAt(Instant.now());
        postCommentRepository.save(postComment);
        PostCommentResponse response = PostCommentMapper.fromEntityToResponse(postComment);
        return PostCommentResult.success(response);
    }

    public PostLikeResult addLikeToPost(PostLikeRequest request) {
        Optional<User> userOpt = userRepository.findById(request.userId());
        Optional<Post> postOpt = postRepository.findById(request.postId());

        if(userOpt.isEmpty()) {
            return PostLikeResult.failure(PostLikeResult.Code.USER_NOT_FOUND,
                    "Could not find author with id = %d in database!".formatted(request.userId()));
        }

        if(postOpt.isEmpty()) {
            return PostLikeResult.failure(PostLikeResult.Code.POST_NOT_FOUND,
                    "Could not find post with id = %d in database!".formatted(request.postId()));
        }

        Post post = postOpt.get();
        User user = userOpt.get();

        if(postRepository.existsPostLikedBy(post.getId(), user.getId())) {
            return PostLikeResult.failure(PostLikeResult.Code.USER_ALREADY_LIKES_POST,
                    "User with id = %d already likes post with id = %d".formatted(user.getId(), post.getId()));
        }

        Long postLikeIncrement = post.getLikeCount() + 1;
        post.setLikeCount(postLikeIncrement);
        postRepository.save(post);

        postRepository.savePostLikedBy(post.getId(), user.getId());
        return PostLikeResult.success(post.getId(), user.getId());
    }


    public PostLikeResult removeLikeToPost(PostLikeRequest request) {
        Optional<User> userOpt = userRepository.findById(request.userId());
        Optional<Post> postOpt = postRepository.findById(request.postId());

        if(userOpt.isEmpty()) {
            return PostLikeResult.failure(PostLikeResult.Code.USER_NOT_FOUND,
                    "Could not find author with id = %d in database!".formatted(request.userId()));
        }

        if(postOpt.isEmpty()) {
            return PostLikeResult.failure(PostLikeResult.Code.POST_NOT_FOUND,
                    "Could not find post with id = %d in database!".formatted(request.postId()));
        }

        Post post = postOpt.get();
        User user = userOpt.get();

        if(postRepository.existsPostLikedBy(post.getId(), user.getId())) {
            return PostLikeResult.failure(PostLikeResult.Code.USER_ALREADY_LIKES_POST,
                    "User with id = %d already likes post with id = %d".formatted(user.getId(), post.getId()));
        }

        Long postLikeIncrement = post.getLikeCount() - 1;
        post.setLikeCount(postLikeIncrement);
        postRepository.save(post);

        postRepository.deletePostLikedBy(post.getId(), user.getId());
        return PostLikeResult.success(post.getId(), user.getId());
    }

    public Page<PostResponse> findRecentPostAvailableForUser(Long userId, int page, int size) {
        if(!userRepository.existsById(userId)) {
            log.debug("Could not find user with id = {}", userId);
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
        Page<Post> posts = postRepository.findRecentPostsAvailableForUser(pageable, userId);

        return posts.map(PostMapper::fromEntityToResponse);
    }

    public Page<PostResponse> checkUserPosts(Long userId, Long userToCheckId, int page, int size) {
        if(!userRepository.existsById(userId)) {
            log.debug("Could not find user with id = {}", userId);
            return Page.empty();
        }

        if(!userRepository.existsById(userToCheckId)) {
            log.debug("Could not find user to check with id = {}", userToCheckId);
            return Page.empty();
        }

        Optional<UserConfig> userToCheckConfigOpt = userConfigRepository.findByUserId(userToCheckId);
        if(userToCheckConfigOpt.isEmpty()) {
            log.debug("User with id = {} does not have configuration", userToCheckId);
            return Page.empty();
        }
        UserConfig userToCheckConfig = userToCheckConfigOpt.get();

        if(userToCheckConfig.getUserPrivacyLevel().equals(UserPrivacyLevel.PRIVATE)) {
            log.debug("User to check have private profile");
            return Page.empty();
        }

        if(!userRepository.areUsersFriends(userId, userToCheckId) &&
                (!userToCheckConfig.getUserPrivacyLevel().equals(UserPrivacyLevel.PUBLIC))) {
            log.debug("User profile is not available for current user");
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
        Page<Post> posts = postRepository.findPostsByUserId(pageable, userToCheckId);
        return posts.map(PostMapper::fromEntityToResponse);
    }


}
