package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.dto.post.request.PostCommentRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostLikeRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostRequest;
import org.hl.socialspherebackend.api.dto.post.response.PostCommentResponse;
import org.hl.socialspherebackend.api.dto.post.response.PostErrorCode;
import org.hl.socialspherebackend.api.dto.post.response.PostLikeResponse;
import org.hl.socialspherebackend.api.dto.post.response.PostResponse;
import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.post.PostComment;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.api.entity.user.UserProfile;
import org.hl.socialspherebackend.application.common.Observer;
import org.hl.socialspherebackend.application.user.UserPermissionCheckResult;
import org.hl.socialspherebackend.application.user.UserProfilePermissionChecker;
import org.hl.socialspherebackend.application.util.AuthUtils;
import org.hl.socialspherebackend.application.validator.RequestValidateResult;
import org.hl.socialspherebackend.application.validator.RequestValidatorChain;
import org.hl.socialspherebackend.infrastructure.post.PostCommentRepository;
import org.hl.socialspherebackend.infrastructure.post.PostRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PostFacadeTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostCommentRepository postCommentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfilePermissionChecker permissionChecker;

    @Mock
    private RequestValidatorChain requestValidator;

    @Mock
    private Clock clock;

    @Mock
    private Set<Observer> observers;

    @InjectMocks
    private PostFacade postFacade;

    private User mockUser;
    private Post mockPost;
    private PostComment mockPostComment;
    private PostRequest postRequest;
    private PostCommentRequest postCommentRequest;
    private PostLikeRequest postLikeRequest;
    private final MockedStatic<AuthUtils> mockedAuthUtils = mockStatic(AuthUtils.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(Instant.now(clock)).thenReturn(LocalDateTime.of(2020, 10, 15, 14, 30, 0).toInstant(ZoneOffset.UTC));

        mockUser = new User("user1", "pass", Instant.now(clock));
        mockUser.setId(1L);
        mockUser.setUserProfile(new UserProfile("First name1", "Last name1", "City1", "Country1", mockUser));
        mockedAuthUtils.when(AuthUtils::getCurrentUser).thenReturn(Optional.of(mockUser));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));

        when(permissionChecker.checkUserProfileResourceAccess(any(User.class), any(User.class)))
                .thenReturn(new UserPermissionCheckResult(true, null));


        mockPost = new Post("post content", 0L, 0L, Instant.now(clock), Instant.now(clock), mockUser);
        mockPost.setId(1L);
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(mockPost));
        when(postRepository.save(any(Post.class))).thenReturn(mockPost);

        when(postRepository.existsPostLikedBy(anyLong(), anyLong())).thenReturn(true);

        mockPostComment = new PostComment(mockUser, mockPost, "comment content", Instant.now(clock), Instant.now(clock));
        mockPostComment.setId(1L);
        when(postCommentRepository.save(any(PostComment.class))).thenReturn(mockPostComment);

        postRequest = new PostRequest(mockPost.getContent());
        postCommentRequest = new PostCommentRequest(1L, mockPostComment.getContent());
        postLikeRequest = new PostLikeRequest(1L);
        when(requestValidator.validate(postRequest)).thenReturn(new RequestValidateResult(true, null, null));
        when(requestValidator.validate(postCommentRequest)).thenReturn(new RequestValidateResult(true, null, null));

    }

    @AfterEach
    void tearDown() {
        mockedAuthUtils.close();
    }

    @Test
    void should_create_post_successfully() {
        DataResult<PostResponse> result = postFacade.createPost(postRequest, Collections.emptyList());

        assertTrue(result.isSuccess());
        assertEquals(mockPost.getContent(), result.getData().content());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void should_not_create_post_when_validation_fails() {
        when(requestValidator.validate(postRequest)).thenReturn(new RequestValidateResult(false, null, null));

        DataResult<PostResponse> result = postFacade.createPost(postRequest, Collections.emptyList());

        assertFalse(result.isSuccess());
        verify(postRepository, times(0)).save(any(Post.class));
    }

    @Test
    void should_add_comment_to_post_successfully() {
        Long commentCount = mockPost.getCommentCount();
        Long expectedCommentCount = commentCount + 1;
        DataResult<PostCommentResponse> result = postFacade.addCommentToPost(postCommentRequest);

        assertTrue(result.isSuccess());
        assertEquals(mockPostComment.getContent(), result.getData().content());
        assertEquals(expectedCommentCount, mockPost.getCommentCount());
        verify(postCommentRepository, times(1)).save(any(PostComment.class));
    }

    @Test
    void should_not_add_comment_to_post_when_validation_fails() {
        when(requestValidator.validate(postCommentRequest)).thenReturn(new RequestValidateResult(false, null, null));

        DataResult<PostCommentResponse> result = postFacade.addCommentToPost(postCommentRequest);
        assertFalse(result.isSuccess());
        verify(postCommentRepository, times(0)).save(any(PostComment.class));
    }

    @Test
    void should_not_add_comment_to_post_when_post_is_not_found() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        DataResult<PostCommentResponse> result = postFacade.addCommentToPost(postCommentRequest);

        assertFalse(result.isSuccess());
        verify(postCommentRepository, times(0)).save(any(PostComment.class));
    }

    @Test
    void should_add_like_to_post_successfully() {
        Long likeCount = mockPost.getLikeCount();
        Long expectedLikeCount = likeCount + 1;
        DataResult<PostLikeResponse> result = postFacade.addLikeToPost(postLikeRequest);

        assertTrue(result.isSuccess());
        assertEquals(mockPost.getId(), result.getData().postId());
        assertEquals(expectedLikeCount, mockPost.getLikeCount());
        assertEquals(mockUser.getId(), result.getData().userId());
        verify(postRepository, times(1)).save(mockPost);
    }

    @Test
    void should_not_add_like_to_post_when_user_already_likes_post() {
        mockPost.setLikedBy(Set.of(mockUser));
        PostErrorCode expectedError = PostErrorCode.USER_ALREADY_LIKES_POST;

        DataResult<PostLikeResponse> result = postFacade.addLikeToPost(postLikeRequest);
        assertFalse(result.isSuccess());
        assertEquals(expectedError, result.getErrorCode());
        verify(postRepository, times(0)).save(mockPost);
    }

    @Test
    void should_find_user_posts_successfully() {
        Page<Post> mockPage = new PageImpl<>(Collections.singletonList(mockPost));
        when(postRepository.findByUser(any(PageRequest.class), any(User.class))).thenReturn(mockPage);

        DataResult<Page<PostResponse>> result = postFacade.findUserPosts(1L, 0, 10);

        assertTrue(result.isSuccess());
        assertFalse(result.getData().isEmpty());
        assertEquals(mockPost.getId(), result.getData().getContent().get(0).id());
    }

    @Test
    void should_remove_like_from_post_successfully() {
        DataResult<PostLikeResponse> result = postFacade.removeLikeFromPost(1L);

        assertTrue(result.isSuccess());
        assertEquals(mockPost.getId(), result.getData().postId());
        assertEquals(mockUser.getId(), result.getData().userId());
        verify(postRepository, times(1)).save(mockPost);
    }

    @Test
    void should_not_remove_like_from_post_when_user_does_not_likes_post() {
        when(postRepository.existsPostLikedBy(mockPost.getId(), mockUser.getId())).thenReturn(false);

        DataResult<PostLikeResponse> result = postFacade.removeLikeFromPost(mockPost.getId());

        assertFalse(result.isSuccess());
        verify(postRepository, times(0)).save(mockPost);
    }

}
