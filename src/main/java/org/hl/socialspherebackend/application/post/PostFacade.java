package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.notification.response.PostUpdateDetails;
import org.hl.socialspherebackend.api.dto.post.request.PostCommentRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostLikeRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostRequest;
import org.hl.socialspherebackend.api.dto.post.response.*;
import org.hl.socialspherebackend.api.entity.notification.PostUpdateType;
import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.post.PostComment;
import org.hl.socialspherebackend.api.entity.post.PostImage;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.api.entity.user.UserProfileConfig;
import org.hl.socialspherebackend.api.entity.user.UserProfilePrivacyLevel;
import org.hl.socialspherebackend.application.notification.NotificationMapper;
import org.hl.socialspherebackend.application.pattern.behavioral.Observable;
import org.hl.socialspherebackend.application.pattern.behavioral.Observer;
import org.hl.socialspherebackend.infrastructure.post.PostCommentRepository;
import org.hl.socialspherebackend.infrastructure.post.PostRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PostFacade implements Observable<PostUpdateDetails> {

    private static final Logger log = LoggerFactory.getLogger(PostFacade.class);

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostValidator postValidator;
    private final PostCommentValidator postCommentValidator;
    private final UserRepository userRepository;
    private final Set<Observer<PostUpdateDetails>> observers;

    public PostFacade(PostRepository postRepository,
                      PostCommentRepository postCommentRepository,
                      PostValidator postValidator,
                      PostCommentValidator postCommentValidator,
                      UserRepository userRepository,
                      Set<Observer<PostUpdateDetails>> observers) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postValidator = postValidator;
        this.postCommentValidator = postCommentValidator;
        this.postCommentRepository = postCommentRepository;
        this.observers = observers;
    }


    @Override
    public void addObserver(Observer<PostUpdateDetails> observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<PostUpdateDetails> observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(PostUpdateDetails subject) {
        observers.forEach((observer) -> observer.update(subject));
    }


    public PostResult createPost(PostRequest request) {
        Optional<User> userOpt = userRepository.findById(request.userId());
        if(userOpt.isEmpty()) {
            return PostResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "Could not find user with id = %d in database!".formatted(request.userId()));
        }
        User user = userOpt.get();

        PostValidateResult validateResult = postValidator.validate(request);
        if(!validateResult.isValid()) {
            return PostResult.failure(validateResult.code(), validateResult.message());
        }

        Instant now = Instant.now();
        Post post = new Post(request.content(), 0L, 0L, now, now, user);

        if(request.images() != null) {
            Set<PostImage> postImages = PostMapper.fromRequestToEntities(request.images());
            postImages.forEach(img -> {
                img.setPost(post);
                post.appendPostImage(img);
            });
        }

        postRepository.save(post);
        PostResponse response = PostMapper.fromEntityToResponse(post, false);
        return PostResult.success(response);
    }

    public PostCommentResult addCommentToPost(PostCommentRequest request) {
        Optional<User> authorOpt = userRepository.findById(request.authorId());
        if(authorOpt.isEmpty()) {
            return PostCommentResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "Could not find author with id = %d in database!".formatted(request.authorId()));
        }

        User author = authorOpt.get();

        Optional<Post> postOpt = postRepository.findById(request.postId());
        if(postOpt.isEmpty()) {
            return PostCommentResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "Could not find post with id = %d in database!".formatted(request.postId()));
        }

        PostValidateResult validateResult = postCommentValidator.validate(request);
        if(!validateResult.isValid()) {
            return PostCommentResult.failure(validateResult.code(), validateResult.message());
        }

        Post post = postOpt.get();
        Long incrementPostCommentCount = post.getCommentCount() + 1L;
        post.setCommentCount(incrementPostCommentCount);

        Instant now = Instant.now();
        PostComment postComment = new PostComment(author, post, request.content(), now, now);
        post.appendPostComment(postComment);

        // not postRepository.save() because it forces synchronization with the post_comment table, otherwise the post comment id will be null
        postCommentRepository.save(postComment);

        PostCommentResponse response = PostMapper.fromEntityToResponse(postComment);
        return PostCommentResult.success(response);
    }

    public PostLikeResult addLikeToPost(PostLikeRequest request) {
        Optional<User> likedByOpt = userRepository.findById(request.userId());

        if(likedByOpt.isEmpty()) {
            return PostLikeResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "Could not find author with id = %d in database!".formatted(request.userId()));
        }
        User likedBy = likedByOpt.get();

        Optional<Post> postOpt = postRepository.findById(request.postId());

        if(postOpt.isEmpty()) {
            return PostLikeResult.failure(PostErrorCode.POST_NOT_FOUND,
                    "Could not find post with id = %d in database!".formatted(request.postId()));
        }

        Post post = postOpt.get();

        boolean isLikedByUser = post.getLikedBy()
                .stream()
                .anyMatch(user -> user.equals(likedBy));

        if(isLikedByUser) {
            return PostLikeResult.failure(PostErrorCode.USER_ALREADY_LIKES_POST,
                    "User with id = %d already likes post with id = %d".formatted(likedBy.getId(), post.getId()));
        }

        Long postLikeIncrement = post.getLikeCount() + 1;
        post.setLikeCount(postLikeIncrement);
        post.appendLikedBy(likedBy);
        postRepository.save(post);

        Instant now = Instant.now();
        notifyObservers(NotificationMapper.fromEntitiesToResponse(post, PostUpdateType.LIKE, likedBy, now));

        return PostLikeResult.success(post.getId(), likedBy.getId());
    }


    public PostLikeResult removeLikeFromPost(Long postId, Long userId) {
        Optional<User> likedByOpt = userRepository.findById(userId);

        if(likedByOpt.isEmpty()) {
            return PostLikeResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "Could not find author with id = %d in database!".formatted(userId));
        }
        User likedBy = likedByOpt.get();

        Optional<Post> postOpt = postRepository.findById(postId);
        if(postOpt.isEmpty()) {
            return PostLikeResult.failure(PostErrorCode.POST_NOT_FOUND,
                    "Could not find post with id = %d in database!".formatted(postId));
        }

        Post post = postOpt.get();
        if(postRepository.existsPostLikedBy(post.getId(), likedBy.getId()) < 0) {
            return PostLikeResult.failure(PostErrorCode.USER_DOES_NOT_LIKE_POST,
                    "User with id = %d does not likes post with id = %d".formatted(likedBy.getId(), post.getId()));
        }

        Long postLikeIncrement = post.getLikeCount() - 1;
        post.setLikeCount(postLikeIncrement);
        post.getLikedBy().removeIf(user -> user.equals(likedBy));
        postRepository.save(post);

        return PostLikeResult.success(post.getId(), likedBy.getId());
    }

    public Page<PostResponse> findUserPosts(Long currentUserId, Long userToCheckId, int page, int size) {
        Optional<User> currentUserOpt = userRepository.findById(currentUserId);
        Optional<User> userToCheckOpt = userRepository.findById(userToCheckId);
        if(currentUserOpt.isEmpty()) {
            log.debug("Could not find user with id = {}", currentUserId);
            return Page.empty();
        }

        if(userToCheckOpt.isEmpty()) {
            log.debug("Could not find user to check with id = {}", userToCheckId);
            return Page.empty();
        }

        User currentUser = currentUserOpt.get();
        User userToCheck = userToCheckOpt.get();
        UserProfileConfig userToCheckProfileConfig = userToCheck.getUserProfileConfig();

        if(userToCheckProfileConfig == null) {
            log.debug("User with id = {} does not have configuration", userToCheckId);
            return Page.empty();
        }
        UserProfilePrivacyLevel userToCheckProfilePrivacyLevel = userToCheckProfileConfig.getUserPrivacyLevel();

        if(userToCheckProfilePrivacyLevel.equals(UserProfilePrivacyLevel.PRIVATE)) {
            log.debug("User to check have private profile");
            return Page.empty();
        }

        boolean areUsersFriends = currentUser.getFriends()
                .stream()
                .anyMatch(u -> u.equals(userToCheck));

        if(!areUsersFriends && (userToCheckProfilePrivacyLevel.equals(UserProfilePrivacyLevel.FRIENDS))) {
            log.debug("User profile is not available for current user");
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
        Page<Post> posts = postRepository.findByUser(pageable, userToCheck);
        return posts.map(post -> {
            boolean isLiked = checkIfCurrentUserLikedPost(currentUser, post);
            return PostMapper.fromEntityToResponse(post, isLiked);
        });
    }

    public Page<PostResponse> findCurrentUserPosts(Long currentUserId, int page, int size) {
        Optional<User> currentUserOpt = userRepository.findById(currentUserId);
        if(currentUserOpt.isEmpty()) {
            log.debug("Could not find user with id = {}", currentUserId);
            return Page.empty();
        }
        User currentUser = currentUserOpt.get();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
        Page<Post> posts = postRepository.findByUser(pageable, currentUser);

        return posts.map(post -> {
            boolean isLiked = checkIfCurrentUserLikedPost(currentUser, post);
            return PostMapper.fromEntityToResponse(post, isLiked);
        });
    }

    public Page<PostResponse> findRecentPostsAvailableForUser(Long userId, int page, int size) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            log.debug("Could not find user with id = {}", userId);
            return Page.empty();
        }
        User user = userOpt.get();

        Pageable pageable = PageRequest.of(page, size, Sort.by("created_at"));
        Page<Post> postPage = postRepository.findRecentPostsAvailableForUser(pageable, userId);
        return postPage.map(post -> {
            boolean isLiked = checkIfCurrentUserLikedPost(user, post);
            return PostMapper.fromEntityToResponse(post, isLiked);
        });
    }

    public Page<PostCommentResponse> findPostComments(Long postId, int page, int size) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if(postOpt.isEmpty()) {
            log.debug("Could not find post with id = {}", postId);
            return Page.empty();
        }
        Post post = postOpt.get();

        Set<PostComment> comments = post.getComments();
        if(comments.isEmpty()) {
            log.debug("Post with id = {} has no comments", postId);
            return Page.empty();
        }


        List<PostComment> sortedComments = comments.stream()
                .sorted(Comparator.comparing(PostComment::getCreatedAt).reversed())
                .toList();

        Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), comments.size());

        List<PostComment> sortedCommentsSublist = sortedComments.subList(start, end);
        Page<PostComment> postCommentPage = new PageImpl<>(sortedCommentsSublist, pageable, sortedComments.size());

        return postCommentPage.map(PostMapper::fromEntityToResponse);
    }

    private boolean checkIfCurrentUserLikedPost(User user, Post post) {
        for(User likedBy : post.getLikedBy()) {
            if(likedBy.equals(user))
                return true;
        }
        return false;
    }

}
