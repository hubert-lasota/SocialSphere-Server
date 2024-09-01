package org.hl.socialspherebackend.application.post;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PostFacadeTest {
//
//    private PostFacade postFacade;
//
//    @Mock
//    private PostRepository postRepository;
//
//    @Mock
//    private PostCommentRepository postCommentRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private UserProfilePermissionChecker permissionChecker;
//
//    @Mock
//    private RequestValidatorChain<PostRequest, PostValidateResult> postValidator;
//
//    @Mock
//    private RequestValidatorChain<PostCommentRequest, PostValidateResult> postCommentValidator;
//
//    @Mock
//    private Clock clock;
//
//    private Long existingUserId;
//
//    private Long secondExistingUserId;
//
//    private Long nonExistingUserId;
//
//    private User user;
//
//    private User user2;
//
//    private Long existingPostId;
//
//    private Long nonExistingPostId;
//
//    private Post post;
//
//    private static ZonedDateTime NOW = ZonedDateTime.of(
//            2024,
//            8,
//            1,
//            1,
//            0,
//            0,
//            0,
//            ZoneId.of("GMT")
//    );
//
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        lenient().when(clock.getZone()).thenReturn(NOW.getZone());
//        lenient().when(clock.instant()).thenReturn(NOW.toInstant());
//
//        initUserData();
//        initPostData();
//
//        postFacade = new PostFacade(postRepository, postCommentRepository, userRepository, permissionChecker, postValidator, postCommentValidator, new HashSet<>(), clock);
//    }
//
//
//    void initUserData() {
//        nonExistingUserId = -1L;
//        existingUserId = 1L;
//        secondExistingUserId = 2L;
//
//        user = new User("username", "password");
//        user.setId(existingUserId);
//        user.setUserProfile(new UserProfile("firstname", "lastname","city", "country", user));
//        user.setUserProfileConfig(new UserProfileConfig(UserProfilePrivacyLevel.PUBLIC, user));
//
//        user2 = new User("username2", "password2");
//        user2.setId(secondExistingUserId);
//        user2.setUserProfile(new UserProfile("firstname", "lastname","city", "country", user2));
//        user2.setUserProfileConfig(new UserProfileConfig(UserProfilePrivacyLevel.PUBLIC, user2));
//
//
//        lenient().when(userRepository.findById(existingUserId)).thenReturn(Optional.of(user));
//        lenient().when(userRepository.findById(secondExistingUserId)).thenReturn(Optional.of(user2));
//        lenient().when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());
//    }
//
//    void initPostData() {
//        nonExistingPostId = -1L;
//        existingPostId = 1L;
//        post = new Post("This is post content!", 0L, 0L, NOW.toInstant(), NOW.toInstant(), user);
//        post.setId(existingPostId);
//        lenient().when(postRepository.findById(existingPostId)).thenReturn(Optional.of(post));
//
//        lenient().when(postRepository.existsPostLikedBy(existingPostId, existingUserId)).thenReturn(false);
//    }
//
//
//    @Test
//    public void should_use_different_validators_for_post_and_comment() {
//        assertThat(postValidator).isNotEqualTo(postCommentValidator);
//    }
//
//    @Test
//    public void should_createPost_return_success_postResult() {
//        PostRequest request = new PostRequest(existingUserId, "This is post content!", null);
//
//
//        PostValidateResult mockedValidateResult = new PostValidateResult(true, null, null);
//        lenient().when(postValidator.validate(any(PostRequest.class))).thenReturn(mockedValidateResult);
//
//
//        DataResult<?, ?> result = postFacade.createPost(request);
//
//
//        verify(postRepository, times(1)).save(any(Post.class));
//
//        assertThat(result.isSuccess()).isTrue();
//    }
//
//
//    @Test
//    public void should_createPost_return_failure_postResult_cause_of_bad_userId() {
//        PostRequest request = new PostRequest(nonExistingUserId, "This is post content!", null);
//
//        DataResult<?, ?> result = postFacade.createPost(request);
//
//        verify(postRepository, times(0)).save(any(Post.class));
//
//        assertThat(result.isSuccess()).isFalse();
//    }
//
//    @Test
//    public void should_createPost_return_failure_postResult_cause_of_not_validated_request() {
//        PostRequest request = new PostRequest(1L, "This is post content!", null);
//
//        PostValidateResult mockedValidateResult = new PostValidateResult(false, null, null);
//        when(postValidator.validate(request)).thenReturn(mockedValidateResult);
//
//        DataResult<?, ?> result = postFacade.createPost(request);
//
//        verify(postRepository, times(0)).save(any(Post.class));
//
//        assertThat(result.isSuccess()).isFalse();
//    }

//    @Test
//    public void should_addCommentToPost_return_success_postCommentResult() {
//        PostCommentRequest request = new PostCommentRequest(existingPostId, existingUserId, "This is a comment!");
//
//
//        PostValidateResult validateResult = new PostValidateResult(true, null, null);
//        when(postCommentValidator.validate(request)).thenReturn(validateResult);
//
//        DataResult<?, ?> result = postFacade.addCommentToPost(request);
//
//        verify(postCommentRepository, times(1)).save(any(PostComment.class));
//
//        assertThat(result.isSuccess()).isTrue();
//    }
//
//    @Test
//    public void should_addCommentToPost_return_failure_postCommentResult_cause_of_bad_userId() {
//        PostCommentRequest request = new PostCommentRequest(existingPostId, nonExistingUserId, "This is a comment!");
//
//        DataResult<?, ?> result = postFacade.addCommentToPost(request);
//
//        verify(postCommentRepository, times(0)).save(any(PostComment.class));
//
//        assertThat(result.isSuccess()).isFalse();
//    }
//
//    @Test
//    public void should_addCommentToPost_return_failure_postCommentResult_cause_of_bad_postId() {
//        PostCommentRequest request = new PostCommentRequest(nonExistingPostId, existingUserId, "This is a comment!");
//
//        DataResult<?, ?> result = postFacade.addCommentToPost(request);
//
//        verify(postCommentRepository, times(0)).save(any(PostComment.class));
//
//        assertThat(result.isSuccess()).isFalse();
//    }
//
//    @Test
//    public void should_addCommentToPost_return_failure_postCommentResult_cause_of_not_validated_request() {
//        PostCommentRequest request = new PostCommentRequest(1L, 1L, "This is a comment!");
//
//        PostValidateResult validateResult = new PostValidateResult(false, null, null);
//        when(postCommentValidator.validate(request)).thenReturn(validateResult);
//
//        DataResult<?, ?> result = postFacade.addCommentToPost(request);
//
//        verify(postCommentRepository, times(0)).save(any(PostComment.class));
//
//        assertThat(result.isSuccess()).isFalse();
//    }
//
//
//    @Test
//    public void should_addLikeToPost_return_success_postLikeResult() {
//        PostLikeRequest request = new PostLikeRequest(1L, 1L);
//
//        DataResult<?, ?> result = postFacade.addLikeToPost(request);
//
//        verify(postRepository, times(1)).save(post);
//        assertThat(result.isSuccess()).isTrue();
//    }
//
//    @Test
//    public void should_addLikeToPost_return_failure_postLikeResult_cause_of_bad_userId() {
//        PostLikeRequest request = new PostLikeRequest(existingPostId, nonExistingUserId);
//
//        DataResult<?, ?> result = postFacade.addLikeToPost(request);
//
//        assertThat(result.isSuccess()).isFalse();
//    }
//
//    @Test
//    public void should_addLikeToPost_return_failure_postLikeResult_cause_of_bad_postId() {
//        PostLikeRequest request = new PostLikeRequest(nonExistingPostId, existingUserId);
//
//        DataResult<?, ?> result = postFacade.addLikeToPost(request);
//
//        assertThat(result.isSuccess()).isFalse();
//    }
//
//    @Test
//    public void should_addLikeToPost_return_failure_postLikeResult_cause_of_already_liked() {
//        PostLikeRequest request = new PostLikeRequest(existingPostId, existingUserId);
//
//        post.appendLikedBy(user);
//
//        DataResult<?, ?> result = postFacade.addLikeToPost(request);
//
//        assertThat(result.isSuccess()).isFalse();
//    }
//
//
//    @Test
//    public void should_removeLikeFromPost_return_success_postLikeResult() {
//        Long postId = existingPostId;
//        Long userId = existingUserId;
//
//        lenient().when(postRepository.existsPostLikedBy(postId, userId)).thenReturn(true);
//        DataResult<?, ?> result = postFacade.removeLikeFromPost(postId, userId);
//
//        verify(postRepository, times(1)).save(post);
//        assertThat(result.isSuccess()).isTrue();
//    }
//
//    @Test
//    public void should_removeLikeFromPost_return_failure_postLikeResult_cause_of_bad_userId() {
//        Long postId = existingPostId;
//        Long userId = nonExistingUserId;
//
//        DataResult<?, ?> result = postFacade.removeLikeFromPost(postId, userId);
//
//        assertThat(result.isSuccess()).isFalse();
//    }
//
//    @Test
//    public void should_removeLikeFromPost_return_failure_postLikeResult_cause_of_bad_postId() {
//        Long postId = nonExistingPostId;
//        Long userId = existingUserId;
//
//        DataResult<?, ?> result = postFacade.removeLikeFromPost(postId, userId);
//
//        assertThat(result.isSuccess()).isFalse();
//    }
//
//    @Test
//    void should_removeLikeFromPost_return_failure_postLikeResult_cause_of_not_liked() {
//        Long postId = existingPostId;
//        Long userId = existingUserId;
//
//        lenient().when(postRepository.existsPostLikedBy(postId, userId)).thenReturn(false);
//        DataResult<?, ?> result = postFacade.removeLikeFromPost(postId, userId);
//
//        verify(postRepository, times(0)).save(any(Post.class));
//
//        assertThat(result.isFailure()).isTrue();
//    }
//
//    @Test
//    void should_findUserPosts_return_empty_page_when_currentUser_not_found() {
//        Long currentUserId = nonExistingUserId;
//        Long userToCheckId = existingUserId;
//
//        DataResult<?, ?> result = postFacade.findUserPosts(currentUserId, userToCheckId, 0, 10);
//
//        assertThat(result.isFailure()).isTrue();
//    }
//
//    @Test
//    void should_findUserPosts_return_empty_page_when_userToCheck_not_found() {
//        Long currentUserId = existingUserId;
//        Long userToCheckId = nonExistingUserId;
//
//        DataResult<?, ?> result = postFacade.findUserPosts(currentUserId, userToCheckId, 0, 10);
//
//        assertThat(result.isFailure()).isTrue();
//    }
//
//    @Test
//    void should_findUserPosts_return_empty_page_when_userToCheck_has_no_profile_config() {
//        Long currentUserId = existingUserId;
//        Long userToCheckId = secondExistingUserId;
//
//        lenient().when(permissionChecker.checkUserProfileResourceAccess(user, user2))
//                .thenReturn(new UserPermissionCheckResult(false, "User profile config is null"));
//        DataResult<?, ?> result = postFacade.findUserPosts(currentUserId, userToCheckId, 0, 10);
//
//        assertThat(result.isFailure()).isTrue();
//    }
//
//    @Test
//    void should_findUserPosts_return_empty_page_when_userToCheck_profile_is_private() {
//        Long currentUserId = existingUserId;
//        Long userToCheckId = secondExistingUserId;
//        user2.setUserProfileConfig(new UserProfileConfig(UserProfilePrivacyLevel.PRIVATE, user2));
//
//        lenient().when(permissionChecker.checkUserProfileResourceAccess(user, user2))
//                .thenReturn(new UserPermissionCheckResult(false, "user profile is private"));
//        DataResult<?, ?> result = postFacade.findUserPosts(currentUserId, userToCheckId, 0, 10);
//
//        assertThat(result.isFailure()).isTrue();
//    }
//
//    @Test
//    void should_findUserPosts_return_empty_page_when_userToCheck_profile_is_friends_only_and_not_friends() {
//        Long currentUserId = existingUserId;
//        Long userToCheckId = secondExistingUserId;
//        user2.setUserProfileConfig(new UserProfileConfig(UserProfilePrivacyLevel.FRIENDS, user2));
//
//        lenient().when(permissionChecker.checkUserProfileResourceAccess(user, user2))
//                .thenReturn(new UserPermissionCheckResult(false, "You profile is access friends only"));
//        DataResult<?, ?> result = postFacade.findUserPosts(currentUserId, userToCheckId, 0, 10);
//
//        assertThat(result.isFailure()).isTrue();
//    }
//
//    @Test
//    void should_findUserPosts_return_posts_when_userToCheck_profile_is_friends_and_users_are_friends() {
//        Long currentUserId = existingUserId;
//        Long userToCheckId = secondExistingUserId;
//        user2.setUserProfileConfig(new UserProfileConfig(UserProfilePrivacyLevel.FRIENDS, user2));
//        user2.appendFriend(user);
//        Post post1 = new Post("Post number one", 0L, 0L, NOW.toInstant(), NOW.toInstant(), user2);
//        post1.setId(1L);
//        Post post2 = new Post("Post number two", 0L, 0L, NOW.toInstant(), NOW.toInstant(), user2);
//        post2.setId(2L);
//        UserProfile profile = user2.getUserProfile();
//        UserProfileResponse profileResponse = new UserProfileResponse(profile.getFirstName(), profile.getLastName(), profile.getCity(), profile.getCountry(), null);
//
//        PostResponse postResponse1 =
//                new PostResponse(post1.getId(), user2.getId(), profileResponse, post1.getContent(), null, post1.getLikeCount(), post1.getCommentCount(), post1.getCreatedAt(), post1.getUpdatedAt(), false);
//
//        PostResponse postResponse2 =
//                new PostResponse(post2.getId(), user2.getId(), profileResponse, post2.getContent(), null, post2.getLikeCount(), post2.getCommentCount(), post2.getCreatedAt(), post2.getUpdatedAt(), false);
//
//        int page = 0;
//        int size = 10;
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
//
//        List<Post> listOfPost = List.of(post1, post2);
//        Page<Post> postPage = new PageImpl<>(listOfPost, pageable, listOfPost.size());
//
//        when(postRepository.findByUser(pageable, user2)).thenReturn(postPage);
//
//        lenient().when(permissionChecker.checkUserProfileResourceAccess(user, user2))
//                .thenReturn(new UserPermissionCheckResult(true, null));
//        DataResult<Page<PostResponse>, ?> result = postFacade.findUserPosts(currentUserId, userToCheckId, page, size);
//
//        assertThat(result.isSuccess()).isTrue();
//        assertThat(result.getData()).containsExactly(postResponse1, postResponse2);
//    }
//
//

}
