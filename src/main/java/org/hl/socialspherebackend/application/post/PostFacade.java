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
import org.hl.socialspherebackend.infrastructure.post.PostRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PostFacade {

    private static final Logger log = LoggerFactory.getLogger(PostFacade.class);

    private final UserRepository userRepository;
    private final PostRepository postRepository;


    public PostFacade(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }


    public PostResult createPost(PostRequest request) {
        Optional<User> userOpt = userRepository.findById(request.userId());
        if(userOpt.isEmpty()) {
            return PostResult.failure(PostResult.Code.CANNOT_CREATE,
                    "Could not find user with id = %d in database!".formatted(request.userId()));
        }
        User user = userOpt.get();

        Instant now = Instant.now();
        Post post = new Post(request.content(), 0L, 0L, now, now, user);

        Set<PostImage> postImages = PostMapper.fromRequestToPostImageEntities(request.images());
        postImages.forEach(img -> {
            img.setPost(post);
            post.getImages().add(img);
        });

        user.appendPost(post);
        userRepository.save(user);

        PostResponse response = PostMapper.fromPostEntityToResponse(post);
        return PostResult.success(response, PostResult.Code.CREATED);
    }

    public PostCommentResult addCommentToPost(PostCommentRequest request) {
        Optional<User> authorOpt = userRepository.findById(request.authorId());
        if(authorOpt.isEmpty()) {
            return PostCommentResult.failure(PostCommentResult.Code.CANNOT_CREATE,
                    "Could not find author with id = %d in database!".formatted(request.authorId()));
        }

        User author = authorOpt.get();
        Optional<Post> postOpt = author.getPosts()
                .stream()
                .filter(p -> p.getId().equals(request.postId()))
                .findFirst();

        if(postOpt.isEmpty()) {
            return PostCommentResult.failure(PostCommentResult.Code.CANNOT_CREATE,
                    "Could not find post with id = %d in database!".formatted(request.postId()));
        }

        Post post = postOpt.get();
        Long incrementPostCommentCount = post.getLikeCount() + 1L;
        post.setCommentCount(incrementPostCommentCount);

        Instant now = Instant.now();
        PostComment postComment = new PostComment(author, post, request.content(), now, now);
        post.appendPostComment(postComment);
        userRepository.save(post.getUser());

        PostCommentResponse response = PostMapper.fromPostCommentEntityToResponse(postComment);
        return PostCommentResult.success(response);
    }

    public PostLikeResult addLikeToPost(PostLikeRequest request) {
        Optional<User> likedByOpt = userRepository.findById(request.userId());

        if(likedByOpt.isEmpty()) {
            return PostLikeResult.failure(PostLikeResult.Code.USER_NOT_FOUND,
                    "Could not find author with id = %d in database!".formatted(request.userId()));
        }
        User likedBy = likedByOpt.get();

        Optional<Post> postOpt = postRepository.findById(request.postId());

        if(postOpt.isEmpty()) {
            return PostLikeResult.failure(PostLikeResult.Code.POST_NOT_FOUND,
                    "Could not find post with id = %d in database!".formatted(request.postId()));
        }

        Post post = postOpt.get();

        boolean isLikedByUser = post.getLikedBy()
                .stream()
                .anyMatch(user -> user.equals(likedBy));

        if(isLikedByUser) {
            return PostLikeResult.failure(PostLikeResult.Code.USER_ALREADY_LIKES_POST,
                    "User with id = %d already likes post with id = %d".formatted(likedBy.getId(), post.getId()));
        }

        Long postLikeIncrement = post.getLikeCount() + 1;
        post.setLikeCount(postLikeIncrement);
        post.appendLikedBy(likedBy);

        userRepository.save(post.getUser());

        return PostLikeResult.success(post.getId(), likedBy.getId());
    }


    public PostLikeResult removeLikeToPost(PostLikeRequest request) {
        Optional<User> likedByOpt = userRepository.findById(request.userId());

        if(likedByOpt.isEmpty()) {
            return PostLikeResult.failure(PostLikeResult.Code.USER_NOT_FOUND,
                    "Could not find author with id = %d in database!".formatted(request.userId()));
        }
        User likedBy = likedByOpt.get();

        Optional<Post> postOpt = postRepository.findById(request.postId());
        if(postOpt.isEmpty()) {
            return PostLikeResult.failure(PostLikeResult.Code.POST_NOT_FOUND,
                    "Could not find post with id = %d in database!".formatted(request.postId()));
        }

        Post post = postOpt.get();

        if(!postRepository.existsPostLikedBy(post.getId(), likedBy.getId())) {
            return PostLikeResult.failure(PostLikeResult.Code.USER_DOES_NOT_LIKES_POST,
                    "User with id = %d does not likes post with id = %d".formatted(likedBy.getId(), post.getId()));
        }

        Long postLikeIncrement = post.getLikeCount() - 1;
        post.setLikeCount(postLikeIncrement);
        post.getLikedBy().removeIf(user -> user.equals(likedBy));
        postRepository.save(post);

        userRepository.save(post.getUser());
        return PostLikeResult.success(post.getId(), likedBy.getId());
    }

    public Page<PostResponse> findRecentPostsAvailableForUser(Long userId, int page, int size) {
        if(!userRepository.existsById(userId)) {
            log.debug("Could not find user with id = {}", userId);
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("created_at"));
        Page<Post> posts = postRepository.findRecentPostsAvailableForUser(pageable, userId);
        return posts.map(PostMapper::fromPostEntityToResponse);
    }

    public Page<PostResponse> checkUserPosts(Long userId, Long userToCheckId, int page, int size) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<User> userToCheckOpt = userRepository.findById(userToCheckId);
        if(userOpt.isEmpty()) {
            log.debug("Could not find user with id = {}", userId);
            return Page.empty();
        }

        if(userToCheckOpt.isEmpty()) {
            log.debug("Could not find user to check with id = {}", userToCheckId);
            return Page.empty();
        }

        User userToCheck = userToCheckOpt.get();
        UserProfileConfig userToCheckProfileConfig = userToCheck.getUserProfileConfig();

        if(userToCheckProfileConfig == null) {
            log.debug("User with id = {} does not have configuration", userToCheckId);
            return Page.empty();
        }

        if(userToCheckProfileConfig.getUserPrivacyLevel().equals(UserProfilePrivacyLevel.PRIVATE)) {
            log.debug("User to check have private profile");
            return Page.empty();
        }

        if(!userRepository.areUsersFriends(userId, userToCheckId) &&
                (!userToCheckProfileConfig.getUserPrivacyLevel().equals(UserProfilePrivacyLevel.PUBLIC))) {
            log.debug("User profile is not available for current user");
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
        Page<Post> posts = postRepository.findPostsByUserId(pageable, userToCheckId);
        return posts.map(PostMapper::fromPostEntityToResponse);
    }

    public List<Post> findAllPostEntities() {
        return postRepository.findAll();
    }

}
