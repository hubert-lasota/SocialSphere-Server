package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.dto.post.request.PostCommentRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostLikeRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostRequest;
import org.hl.socialspherebackend.api.dto.post.response.*;
import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.post.PostComment;
import org.hl.socialspherebackend.api.entity.post.PostImage;
import org.hl.socialspherebackend.api.entity.post.PostUpdateType;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.application.pattern.behavioral.Observable;
import org.hl.socialspherebackend.application.pattern.behavioral.Observer;
import org.hl.socialspherebackend.application.user.UserPermissionCheckResult;
import org.hl.socialspherebackend.application.user.UserProfilePermissionChecker;
import org.hl.socialspherebackend.application.util.PageUtils;
import org.hl.socialspherebackend.application.validator.RequestValidator;
import org.hl.socialspherebackend.infrastructure.post.PostCommentRepository;
import org.hl.socialspherebackend.infrastructure.post.PostRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PostFacade implements Observable<PostUpdateDetails> {

    private static final Logger log = LoggerFactory.getLogger(PostFacade.class);

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;
    private final UserProfilePermissionChecker permissionChecker;
    private final RequestValidator<PostRequest, PostValidateResult> postValidator;
    private final RequestValidator<PostCommentRequest, PostValidateResult> postCommentValidator;
    private final Set<Observer<PostUpdateDetails>> observers;
    private final Clock clock;

    public PostFacade(PostRepository postRepository,
                      PostCommentRepository postCommentRepository,
                      UserRepository userRepository,
                      UserProfilePermissionChecker permissionChecker,
                      RequestValidator<PostRequest, PostValidateResult> postValidator,
                      RequestValidator<PostCommentRequest, PostValidateResult> postCommentValidator,
                      Set<Observer<PostUpdateDetails>> observers,
                      Clock clock) {
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.userRepository = userRepository;
        this.permissionChecker = permissionChecker;
        this.postValidator = postValidator;
        this.postCommentValidator = postCommentValidator;
        this.observers = observers;
        this.clock = clock;
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


    public DataResult<PostResponse, PostErrorCode> createPost(PostRequest request) {
        Optional<User> userOpt = userRepository.findById(request.userId());
        if(userOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "Could not find user with id = %d in database!".formatted(request.userId()));
        }
        User user = userOpt.get();

        PostValidateResult validateResult = postValidator.validate(request);
        if(!validateResult.isValid()) {
            return DataResult.failure(validateResult.code(), validateResult.message());
        }

        Instant now = Instant.now(clock);
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
        return DataResult.success(response);
    }

    public DataResult<PostCommentResponse, PostErrorCode> addCommentToPost(PostCommentRequest request) {
        Optional<User> authorOpt = userRepository.findById(request.authorId());
        if(authorOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "Could not find author with id = %d in database!".formatted(request.authorId()));
        }

        User author = authorOpt.get();

        Optional<Post> postOpt = postRepository.findById(request.postId());
        if(postOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "Could not find post with id = %d in database!".formatted(request.postId()));
        }

        PostValidateResult validateResult = postCommentValidator.validate(request);
        if(!validateResult.isValid()) {
            return DataResult.failure(validateResult.code(), validateResult.message());
        }

        Post post = postOpt.get();
        Long incrementPostCommentCount = post.getCommentCount() + 1L;
        post.setCommentCount(incrementPostCommentCount);

        Instant now = Instant.now(clock);
        PostComment postComment = new PostComment(author, post, request.content(), now, now);
        post.appendPostComment(postComment);

        // not postRepository.save() because it forces synchronization with the post_comment table, otherwise the post comment id will be null
        postCommentRepository.save(postComment);

        PostCommentResponse response = PostMapper.fromEntityToResponse(postComment);
        return DataResult.success(response);
    }

    public DataResult<PostLikeResponse, PostErrorCode> addLikeToPost(PostLikeRequest request) {
        Optional<User> likedByOpt = userRepository.findById(request.userId());

        if(likedByOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "Could not find author with id = %d in database!".formatted(request.userId()));
        }
        User likedBy = likedByOpt.get();

        Optional<Post> postOpt = postRepository.findById(request.postId());

        if(postOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.POST_NOT_FOUND,
                    "Could not find post with id = %d in database!".formatted(request.postId()));
        }

        Post post = postOpt.get();

        boolean isLikedByUser = post.getLikedBy()
                .stream()
                .anyMatch(user -> user.equals(likedBy));

        if(isLikedByUser) {
            return DataResult.failure(PostErrorCode.USER_ALREADY_LIKES_POST,
                    "User with id = %d already likes post with id = %d".formatted(likedBy.getId(), post.getId()));
        }

        Long postLikeIncrement = post.getLikeCount() + 1;
        post.setLikeCount(postLikeIncrement);
        post.appendLikedBy(likedBy);
        postRepository.save(post);

        Instant now = Instant.now(clock);
        notifyObservers(PostMapper.fromEntitiesToResponse(post, PostUpdateType.LIKE, likedBy, now));

        return DataResult.success(new PostLikeResponse(post.getId(), likedBy.getId()));
    }


    public DataResult<PostLikeResponse, PostErrorCode> removeLikeFromPost(Long postId, Long userId) {
        Optional<User> likedByOpt = userRepository.findById(userId);

        if(likedByOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "Could not find author with id = %d in database!".formatted(userId));
        }
        User likedBy = likedByOpt.get();

        Optional<Post> postOpt = postRepository.findById(postId);
        if(postOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.POST_NOT_FOUND,
                    "Could not find post with id = %d in database!".formatted(postId));
        }

        Post post = postOpt.get();
        if(!postRepository.existsPostLikedBy(post.getId(), likedBy.getId())) {
            return DataResult.failure(PostErrorCode.USER_DOES_NOT_LIKE_POST,
                    "User with id = %d does not like post with id = %d".formatted(likedBy.getId(), post.getId()));
        }

        Long postLikeIncrement = post.getLikeCount() - 1;
        post.setLikeCount(postLikeIncrement);
        post.getLikedBy().removeIf(user -> user.equals(likedBy));
        postRepository.save(post);

        return DataResult.success(new PostLikeResponse(post.getId(), likedBy.getId()));
    }

    public DataResult<Page<PostResponse>, PostErrorCode> findUserPosts(Long currentUserId, Long userToCheckId, int page, int size) {
        Optional<User> currentUserOpt = userRepository.findById(currentUserId);
        Optional<User> userToCheckOpt = userRepository.findById(userToCheckId);
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "Could not find user with id = %s".formatted(currentUserId));
        }

        if(userToCheckOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "Could not find user with id = %d".formatted(userToCheckId));
        }

        User currentUser = currentUserOpt.get();
        User userToCheck = userToCheckOpt.get();

        UserPermissionCheckResult permissionCheckResult = permissionChecker.checkUserProfileResourceAccess(currentUser, userToCheck);
        if(!permissionCheckResult.allowed()) {
            return DataResult.failure(PostErrorCode.USER_POSTS_ARE_NOT_ALLOWED_TO_FETCH,
                    permissionCheckResult.notAllowedErrorMessage());
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
        Page<Post> posts = postRepository.findByUser(pageable, userToCheck);
        Page<PostResponse> response = posts.map(post -> {
            boolean isLiked = checkIfCurrentUserLikedPost(currentUser, post);
            return PostMapper.fromEntityToResponse(post, isLiked);
        });
        return DataResult.success(response);
    }

    public DataResult<Page<PostResponse>, PostErrorCode> findCurrentUserPosts(Long currentUserId, int page, int size) {
        Optional<User> currentUserOpt = userRepository.findById(currentUserId);
        if(currentUserOpt.isEmpty()) {
            log.debug("Could not find user with id = {}", currentUserId);
            return DataResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "Could not find user with id = %d".formatted(currentUserId));
        }
        User currentUser = currentUserOpt.get();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
        Page<Post> posts = postRepository.findByUser(pageable, currentUser);

        Page<PostResponse> response = posts.map(post -> {
            boolean isLiked = checkIfCurrentUserLikedPost(currentUser, post);
            return PostMapper.fromEntityToResponse(post, isLiked);
        });
        return DataResult.success(response);
    }

    public DataResult<Page<PostResponse>, PostErrorCode> findRecentPostsAvailableForUser(Long userId, int page, int size) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            log.debug("Could not find user with id = {}", userId);
            return DataResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "Could not find user with id = %d".formatted(userId));
        }
        User user = userOpt.get();


        List<Post> posts = postRepository.findAllSortedByCreatedAtDesc();
        List<Post> permissionFilteredPosts = posts.stream()
                .filter(post -> {
                    User userToCheck = post.getUser();
                    return permissionChecker.checkUserProfileResourceAccess(user, userToCheck).allowed();
                })
                .toList();

        Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
        Page<Post> postPage = PageUtils.createPageImpl(permissionFilteredPosts, pageable);
        Page<PostResponse> response = postPage.map(post -> {
            boolean isLiked = checkIfCurrentUserLikedPost(user, post);
            return PostMapper.fromEntityToResponse(post, isLiked);
        });
        return DataResult.success(response);
    }

    public DataResult<Page<PostCommentResponse>, PostErrorCode> findPostComments(Long postId, int page, int size) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if(postOpt.isEmpty()) {
            log.debug("Could not find post with id = {}", postId);
            return DataResult.failure(PostErrorCode.POST_NOT_FOUND,
                    "Could not find user with id = %d".formatted(postId));
        }
        Post post = postOpt.get();

        Set<PostComment> comments = post.getComments();
        if(comments.isEmpty()) {
            log.debug("Post with id = {} has no comments", postId);
            return DataResult.failure(PostErrorCode.POST_HAS_NO_COMMENTS,
                    "Post with id = %d has no comments".formatted(postId));
        }


        List<PostComment> sortedComments = comments.stream()
                .sorted(Comparator.comparing(PostComment::getCreatedAt).reversed())
                .toList();

        Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
        Page<PostComment> postCommentPage = PageUtils.createPageImpl(sortedComments, pageable);

        Page<PostCommentResponse> response = postCommentPage.map(PostMapper::fromEntityToResponse);
        return DataResult.success(response);
    }

    private boolean checkIfCurrentUserLikedPost(User user, Post post) {
        for(User likedBy : post.getLikedBy()) {
            if(likedBy.equals(user))
                return true;
        }
        return false;
    }

}
