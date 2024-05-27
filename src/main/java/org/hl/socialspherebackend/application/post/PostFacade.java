package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.post.request.PostCommentRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostLikeRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostRequest;
import org.hl.socialspherebackend.api.dto.post.response.*;
import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.post.PostComment;
import org.hl.socialspherebackend.api.entity.post.PostImage;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.api.entity.user.UserProfileConfig;
import org.hl.socialspherebackend.api.entity.user.UserProfilePrivacyLevel;
import org.hl.socialspherebackend.application.post.mapper.PostCommentMapper;
import org.hl.socialspherebackend.application.post.mapper.PostMapper;
import org.hl.socialspherebackend.application.user.UserFacade;
import org.hl.socialspherebackend.infrastructure.post.repository.PostCommentRepository;
import org.hl.socialspherebackend.infrastructure.post.repository.PostImageRepository;
import org.hl.socialspherebackend.infrastructure.post.repository.PostRepository;
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
    private final UserFacade userFacade;


    public PostFacade(PostRepository postRepository,
                      PostCommentRepository postCommentRepository,
                      PostImageRepository postImageRepository,
                      UserFacade userFacade) {

        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.postImageRepository = postImageRepository;
        this.userFacade = userFacade;
    }



    public PostResult createPost(PostRequest postRequest) {
        Optional<User> userOpt = userFacade.findUserEntityById(postRequest.userId());
        if(userOpt.isEmpty()) {
            return PostResult.failure(PostResult.Code.CANNOT_CREATE,
                    "Could not find user with id = %d in database!".formatted(postRequest.userId()));
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
        Optional<User> authorOpt = userFacade.findUserEntityById(request.authorId());
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
        Optional<User> userOpt = userFacade.findUserEntityById(request.userId());
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
        Optional<User> userOpt = userFacade.findUserEntityById(request.userId());
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

    public Page<PostResponse> findRecentPostsAvailableForUser(Long userId, int page, int size) {
        if(!userFacade.existsUserById(userId)) {
            log.debug("Could not find user with id = {}", userId);
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
        Page<Post> posts = postRepository.findRecentPostsAvailableForUser(pageable, userId);

        return posts.map(PostMapper::fromEntityToResponse);
    }

    public Page<PostResponse> checkUserPosts(Long userId, Long userToCheckId, int page, int size) {
        if(!userFacade.existsUserById(userId)) {
            log.debug("Could not find user with id = {}", userId);
            return Page.empty();
        }

        if(!userFacade.existsUserById(userToCheckId)) {
            log.debug("Could not find user to check with id = {}", userToCheckId);
            return Page.empty();
        }

        Optional<UserProfileConfig> userToCheckConfigOpt = userFacade.findUserProfileConfigEntityByUserId(userToCheckId);
        if(userToCheckConfigOpt.isEmpty()) {
            log.debug("User with id = {} does not have configuration", userToCheckId);
            return Page.empty();
        }
        UserProfileConfig userToCheckConfig = userToCheckConfigOpt.get();

        if(userToCheckConfig.getUserPrivacyLevel().equals(UserProfilePrivacyLevel.PRIVATE)) {
            log.debug("User to check have private profile");
            return Page.empty();
        }

        if(!userFacade.areUsersFriends(userId, userToCheckId) &&
                (!userToCheckConfig.getUserPrivacyLevel().equals(UserProfilePrivacyLevel.PUBLIC))) {
            log.debug("User profile is not available for current user");
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
        Page<Post> posts = postRepository.findPostsByUserId(pageable, userToCheckId);
        return posts.map(PostMapper::fromEntityToResponse);
    }


}
