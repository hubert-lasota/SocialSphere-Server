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
import org.hl.socialspherebackend.application.common.Observable;
import org.hl.socialspherebackend.application.common.Observer;
import org.hl.socialspherebackend.application.user.UserPermissionCheckResult;
import org.hl.socialspherebackend.application.user.UserProfilePermissionChecker;
import org.hl.socialspherebackend.application.util.FileUtils;
import org.hl.socialspherebackend.application.util.PageUtils;
import org.hl.socialspherebackend.application.validator.RequestValidateResult;
import org.hl.socialspherebackend.application.validator.RequestValidatorChain;
import org.hl.socialspherebackend.infrastructure.post.PostCommentRepository;
import org.hl.socialspherebackend.infrastructure.post.PostRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static java.util.stream.Collectors.toSet;

public class PostFacade implements Observable<PostUpdateDetails> {

    private static final Logger log = LoggerFactory.getLogger(PostFacade.class);

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;
    private final UserProfilePermissionChecker permissionChecker;
    private final RequestValidatorChain requestValidator;
    private final Set<Observer<PostUpdateDetails>> observers;
    private final Clock clock;

    public PostFacade(PostRepository postRepository,
                      PostCommentRepository postCommentRepository,
                      UserRepository userRepository,
                      UserProfilePermissionChecker permissionChecker,
                      RequestValidatorChain requestValidator,
                      Set<Observer<PostUpdateDetails>> observers,
                      Clock clock) {
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.userRepository = userRepository;
        this.permissionChecker = permissionChecker;
        this.requestValidator = requestValidator;
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


    public DataResult<PostResponse> createPost(PostRequest request, List<MultipartFile> images) {
        Optional<User> userOpt = userRepository.findById(request.userId());
        if(userOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "Could not find user with id = %d in database!".formatted(request.userId()));
        }
        User user = userOpt.get();

        RequestValidateResult validateResult = requestValidator.validate(request);
        if(!validateResult.valid()) {
            return DataResult.failure(validateResult.errorCode(), validateResult.errorMessage());
        }

        Instant now = Instant.now(clock);
        Post post = new Post(request.content(), 0L, 0L, now, now, user);

        if(images != null && !images.isEmpty()) {
            Set<PostImage> postImages = new HashSet<>(images.size());
            for(MultipartFile img : images) {
                String type = img.getContentType();
                String name = img.getOriginalFilename();
                byte[] compressedImg;
                try {
                    compressedImg = FileUtils.compressFile(img.getBytes());
                } catch (IOException e) {
                    log.debug("Could not get bytes from image. Error: {}", e.getMessage());
                    return DataResult.failure(PostErrorCode.POST_IMAGES_ARE_BROKEN,
                            "Could not get bytes from image. Img = %s is broken".formatted(img.getOriginalFilename()));
                }
                PostImage postImage = new PostImage(compressedImg, type, name, post);
                postImages.add(postImage);
            }
            post.setImages(postImages);

        }

        postRepository.save(post);
        PostResponse response = PostMapper.fromEntityToResponse(post, false);
        return DataResult.success(response);
    }

    public DataResult<PostCommentResponse> addCommentToPost(PostCommentRequest request) {
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

        RequestValidateResult validateResult = requestValidator.validate(request);
        if(!validateResult.valid()) {
            return DataResult.failure(validateResult.errorCode(), validateResult.errorMessage());
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

    public DataResult<PostLikeResponse> addLikeToPost(PostLikeRequest request) {
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


    public DataResult<PostLikeResponse> removeLikeFromPost(Long postId, Long userId) {
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

    public DataResult<Page<PostResponse>> findUserPosts(Long currentUserId, Long userToCheckId, int page, int size) {
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

    public DataResult<Page<PostResponse>> findCurrentUserPosts(Long currentUserId, int page, int size) {
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

    public DataResult<Page<PostResponse>> findRecentPostsAvailableForUser(Long userId, int page, int size) {
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
        if(postPage.isEmpty()) {
            return DataResult.failure(PostErrorCode.NO_MORE_CONTENT_ON_PAGE,
                    "Page(%d) has no content!".formatted(page));
        }
        Page<PostResponse> response = postPage.map(post -> {
            boolean isLiked = checkIfCurrentUserLikedPost(user, post);
            return PostMapper.fromEntityToResponse(post, isLiked);
        });
        return DataResult.success(response);
    }

    public DataResult<Page<PostCommentResponse>> findPostComments(Long postId, int page, int size) {
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
        if(postCommentPage.isEmpty()) {
            return DataResult.failure(PostErrorCode.NO_MORE_CONTENT_ON_PAGE,
                    "There are no comments on page=%d".formatted(page));
        }

        Page<PostCommentResponse> response = postCommentPage.map(PostMapper::fromEntityToResponse);
        return DataResult.success(response);
    }

    public DataResult<PostResponse> updatePost(Long postId, PostRequest request, List<MultipartFile> images) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if(postOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.POST_NOT_FOUND,
                    "Could not find post with id = %d".formatted(postId));
        }
        RequestValidateResult validateResult = requestValidator.validate(request);
        if(!validateResult.valid()) {
            return DataResult.failure(validateResult.errorCode(), validateResult.errorMessage());
        }

        Post post = postOpt.get();
        User postAuthor = post.getUser();
        Long postAuthorId = postAuthor.getId();
        Long requestUserId = request.userId();
        if(!postAuthorId.equals(requestUserId)) {
            return DataResult.failure(PostErrorCode.USER_IS_NOT_POST_AUTHOR,
                    "User with id = %d is not post(%d) author".formatted(postAuthorId, postId));
        }

        post.setContent(request.content());

        if(images != null && !images.isEmpty()) {
            Set<PostImage> postImages = images.stream()
                    .map(img -> {
                        String name = img.getOriginalFilename();
                        String type;
                        if(name == null) {
                            name = "example";
                            type = img.getContentType();
                        } else {
                            type = FileUtils.getTypeFromFilename(name);
                        }

                        try {
                            byte[] contentBytes = img.getBytes();
                            return new PostImage(contentBytes, type, name, post);
                        } catch (IOException e) {
                            log.error("Error occurred on getting bytes from img({}). Error message: {}", img, e.getMessage());
                            return null;
                        }
                    })
                    .collect(toSet());

            post.setImages(postImages);
        }

        PostResponse response = PostMapper.fromEntityToResponse(post, checkIfCurrentUserLikedPost(postAuthor, post));
        return DataResult.success(response);
    }

    public DataResult<PostCommentResponse> updatePostComment(Long postCommentId, PostCommentRequest request) {
        Optional<PostComment> commentOpt = postCommentRepository.findById(postCommentId);
        if(commentOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.POST_COMMENT_NOT_FOUND,
                    "Post comment with id=%d is not found".formatted(postCommentId));
        }
        RequestValidateResult validateResult = requestValidator.validate(request);
        if(!validateResult.valid()) {
            return DataResult.failure(validateResult.errorCode(), validateResult.errorMessage());
        }

        PostComment comment = commentOpt.get();
        User commentAuthor = comment.getCommentAuthor();
        Long commentAuthorId = commentAuthor.getId();
        if(!commentAuthorId.equals(request.authorId())) {
            return DataResult.failure(PostErrorCode.USER_IS_NOT_POST_COMMENT_AUTHOR,
                    "User with id=%d is not comment(%d) author".formatted(commentAuthorId, postCommentId));
        }

        comment.setContent(request.content());

        PostCommentResponse response = PostMapper.fromEntityToResponse(comment);
        return DataResult.success(response);
    }

    public DataResult<String> deletePost(Long postId, Long userId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if(postOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.POST_NOT_FOUND,
                    "Post with id = %d is not found".formatted(postId));
        }
        Post post = postOpt.get();
        User postAuthor = post.getUser();
        if(!postAuthor.getId().equals(userId)) {
            return DataResult.failure(PostErrorCode.USER_IS_NOT_POST_AUTHOR,
                    "User with id=%d is not post(%d) author".formatted(userId, postId));
        }

        postRepository.delete(post);
        return DataResult.success("Post with id = %d has been deleted".formatted(postId));
    }

    public DataResult<String> deletePostComment(Long postCommentId, Long userId) {
        Optional<PostComment> commentOpt = postCommentRepository.findById(postCommentId);
        if(commentOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.POST_COMMENT_NOT_FOUND,
                    "PostComment with id = %d is not found".formatted(postCommentId));
        }
        PostComment comment = commentOpt.get();
        User commentAuthor = comment.getCommentAuthor();
        if(!commentAuthor.getId().equals(userId)) {
            return DataResult.failure(PostErrorCode.USER_IS_NOT_POST_COMMENT_AUTHOR,
                    "User with id=%d is not post(%d) author".formatted(userId, postCommentId));
        }

        Post post = comment.getPost();
        post.getComments().remove(comment);
        postRepository.save(post);
        return DataResult.success("PostComment with id = %d has been deleted".formatted(postCommentId));
    }

    private boolean checkIfCurrentUserLikedPost(User user, Post post) {
        for(User likedBy : post.getLikedBy()) {
            if(likedBy.equals(user))
                return true;
        }
        return false;
    }

}
