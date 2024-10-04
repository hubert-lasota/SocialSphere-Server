package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.dto.user.request.*;
import org.hl.socialspherebackend.api.dto.user.response.*;
import org.hl.socialspherebackend.api.entity.user.*;
import org.hl.socialspherebackend.application.common.Observer;
import org.hl.socialspherebackend.application.util.AuthUtils;
import org.hl.socialspherebackend.application.validator.RequestValidateResult;
import org.hl.socialspherebackend.application.validator.RequestValidatorChain;
import org.hl.socialspherebackend.infrastructure.user.UserFriendRequestRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class UserFacadeTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserFriendRequestRepository userFriendRequestRepository;

    @Mock
    private UserProfilePermissionChecker permissionChecker;

    @Mock
    private RequestValidatorChain requestValidator;

    @Mock
    private Clock clock;

    @Mock
    private Set<Observer> observers;

    @InjectMocks
    private UserFacade userFacade;

    private User mockUser;
    private MockedStatic<AuthUtils> mockedAuthUtils = mockStatic(AuthUtils.class);

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
    }

    @AfterEach
    void tearDown() {
        mockedAuthUtils.close();
    }

    @Test
    void should_send_friend_request_successfully() {
        UserFriendRequestDto dto = new UserFriendRequestDto(2L);
        when(userFriendRequestRepository.findSentFriendRequestsByUserId(mockUser.getId())).thenReturn(Collections.emptyList());
        when(userFriendRequestRepository.findSentFriendRequestsByUserId(dto.receiverId())).thenReturn(Collections.emptyList());

        DataResult<UserFriendRequestResponse> result = userFacade.sendFriendRequest(dto);

        assertTrue(result.isSuccess());
        verify(userFriendRequestRepository, times(1)).save(any(UserFriendRequest.class));
    }

    @Test
    void should_not_send_friend_request_when_sender_already_sent_friend_request() {
        User receiver = new User("test", "test", Instant.now(clock));
        receiver.setId(2L);
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));

        UserFriendRequestDto dto = new UserFriendRequestDto(receiver.getId());
        when(userFriendRequestRepository.findSentFriendRequestsByUserId(dto.receiverId())).thenReturn(Collections.emptyList());
        List<UserFriendRequest> friendRequests =
                List.of(new UserFriendRequest(mockUser, receiver, UserFriendRequestStatus.WAITING_FOR_RESPONSE, Instant.now(clock)));
        when(userFriendRequestRepository.findSentFriendRequestsByUserId(mockUser.getId())).thenReturn(friendRequests);

        DataResult<UserFriendRequestResponse> result = userFacade.sendFriendRequest(dto);

        assertFalse(result.isSuccess());
        verify(userFriendRequestRepository, times(0)).save(any(UserFriendRequest.class));
    }

    @Test
    void should_not_send_friend_request_when_receiver_already_sent_friend_request() {
        User receiver = new User("test", "test", Instant.now(clock));
        receiver.setId(2L);
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));

        UserFriendRequestDto dto = new UserFriendRequestDto(receiver.getId());
        when(userFriendRequestRepository.findSentFriendRequestsByUserId(mockUser.getId())).thenReturn(Collections.emptyList());
        List<UserFriendRequest> friendRequests =
                List.of(new UserFriendRequest(receiver, mockUser, UserFriendRequestStatus.WAITING_FOR_RESPONSE, Instant.now(clock)));
        when(userFriendRequestRepository.findSentFriendRequestsByUserId(receiver.getId())).thenReturn(friendRequests);

        DataResult<UserFriendRequestResponse> result = userFacade.sendFriendRequest(dto);

        assertFalse(result.isSuccess());
        verify(userFriendRequestRepository, times(0)).save(any(UserFriendRequest.class));
    }

    @Test
    void should_not_send_friend_request_when_users_are_already_friends() {
        User receiver = new User("test", "test", Instant.now(clock));
        receiver.setId(2L);
        when(userRepository.findUserFriends(mockUser.getId())).thenReturn(List.of(receiver));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        DataResult<UserFriendRequestResponse> result = userFacade.sendFriendRequest(new UserFriendRequestDto(receiver.getId()));

        assertFalse(result.isSuccess());
        verify(userFriendRequestRepository, times(0)).save(any(UserFriendRequest.class));
    }

    @Test
    void should_accept_friend_request_successfully() {
        User receiver = new User("test", "test", Instant.now(clock));
        receiver.setUserProfile(new UserProfile("First name1", "Last name1", "City1", "Country1", receiver));
        receiver.setId(2L);
        Long friendRequestId = 1L;
        UserFriendRequest friendRequest =
                new UserFriendRequest(mockUser, receiver, UserFriendRequestStatus.WAITING_FOR_RESPONSE, Instant.now(clock));
        friendRequest.setId(friendRequestId);
        when(userFriendRequestRepository.findById(friendRequestId)).thenReturn(Optional.of(friendRequest));


        DataResult<UserFriendRequestResponse> result = userFacade.acceptFriendRequest(friendRequestId);

        assertTrue(result.isSuccess());
        assertEquals(UserFriendRequestStatus.ACCEPTED, result.getData().status());
        verify(userFriendRequestRepository, times(1)).save(any(UserFriendRequest.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void should_reject_friend_request_successfully() {
        User receiver = new User("test", "test", Instant.now(clock));
        receiver.setUserProfile(new UserProfile("First name1", "Last name1", "City1", "Country1", receiver));
        receiver.setId(2L);
        Long friendRequestId = 1L;
        UserFriendRequest friendRequest =
                new UserFriendRequest(mockUser, receiver, UserFriendRequestStatus.WAITING_FOR_RESPONSE, Instant.now(clock));
        friendRequest.setId(friendRequestId);
        when(userFriendRequestRepository.findById(friendRequestId)).thenReturn(Optional.of(friendRequest));


        DataResult<UserFriendRequestResponse> result = userFacade.rejectFriendRequest(friendRequestId);

        assertTrue(result.isSuccess());
        assertEquals(UserFriendRequestStatus.REJECTED, result.getData().status());
        verify(userFriendRequestRepository, times(1)).save(any(UserFriendRequest.class));
        verify(userRepository, times(0)).save(any(User.class));
    }


    @Test
    void should_create_user_profile_successfully() {
        mockUser.setUserProfile(null);
        UserProfileRequest request = new UserProfileRequest("firstName10", "lastName10", "city10", "country10");
        when(requestValidator.validate(request)).thenReturn(new RequestValidateResult(true, null, null));

        DataResult<UserProfileResponse> result = userFacade.createUserProfile(request, null);

        assertTrue(result.isSuccess());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void should_not_create_user_profile_when_user_already_has_profile() {
        UserProfileRequest request = new UserProfileRequest("firstName10", "lastName10", "city10", "country10");
        when(requestValidator.validate(request)).thenReturn(new RequestValidateResult(true, null, null));

        DataResult<UserProfileResponse> result = userFacade.createUserProfile(request, null);

        assertFalse(result.isSuccess());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void should_create_user_profile_config_successfully() {
        UserProfilePrivacyLevel privacyLevel = UserProfilePrivacyLevel.PUBLIC;
        UserProfileConfigRequest request = new UserProfileConfigRequest(privacyLevel);

        DataResult<UserProfileConfigResponse> result = userFacade.createUserProfileConfig(request);

        assertTrue(result.isSuccess());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void should_not_create_user_profile_config_when_user_already_has_profile_config() {
        UserProfilePrivacyLevel privacyLevel = UserProfilePrivacyLevel.PUBLIC;
        mockUser.setUserProfileConfig(new UserProfileConfig(privacyLevel, mockUser));
        UserProfileConfigRequest request = new UserProfileConfigRequest(privacyLevel);

        DataResult<UserProfileConfigResponse> result = userFacade.createUserProfileConfig(request);

        assertFalse(result.isSuccess());
        verify(userRepository, times(0)).save(any(User.class));
    }


    @Test
    void should_find_users_successfully() {
        final String pattern = "To";
        User user = new User("test", "test", Instant.now(clock));
        user.setId(1L);
        User user2 = new User("test", "test", Instant.now(clock));
        user2.setId(2L);
        User user3 = new User("test", "test", Instant.now(clock));
        user3.setId(3L);

        UserProfile userProfile = new UserProfile("First name1", "Last name1", "City1", "Country1", user);
        UserProfile userProfile2 = new UserProfile("Tom", "Last name2", "City2", "Country2", user2);
        UserProfile userProfile3 = new UserProfile("FirstName1", "Tomlinson", "City3", "Country3", user3);
        user.setUserProfile(userProfile);
        user2.setUserProfile(userProfile2);
        user3.setUserProfile(userProfile3);
        List<User> users = List.of(user, user2, user3);
        when(userRepository.findAll()).thenReturn(users);
        List<Long> expectedIds = List.of(2L, 3L);

        DataResult<Set<UserHeaderResponse>> result = userFacade.findUsers(pattern, 3);
        List<Long> actualIds = result.getData()
                .stream()
                .map(UserHeaderResponse::userId)
                .toList();

        assertTrue(result.isSuccess());
        assertEquals(expectedIds.size(), actualIds.size());
        assertTrue(actualIds.containsAll(expectedIds));
    }

    @Test
    void should_not_find_users_when_size_is_zero() {
        final String pattern = "To";
        User user = new User("test", "test", Instant.now(clock));
        user.setId(1L);
        User user2 = new User("test", "test", Instant.now(clock));
        user2.setId(2L);
        User user3 = new User("test", "test", Instant.now(clock));
        user3.setId(3L);

        UserProfile userProfile = new UserProfile("First name1", "Last name1", "City1", "Country1", user);
        UserProfile userProfile2 = new UserProfile("Tom", "Last name2", "City2", "Country2", user2);
        UserProfile userProfile3 = new UserProfile("FirstName1", "Tomlinson", "City3", "Country3", user3);
        user.setUserProfile(userProfile);
        user2.setUserProfile(userProfile2);
        user3.setUserProfile(userProfile3);
        List<User> users = List.of(user, user2, user3);
        when(userRepository.findAll()).thenReturn(users);

        DataResult<Set<UserHeaderResponse>> result = userFacade.findUsers(pattern, 0);


        assertFalse(result.isSuccess());
    }

    @Test
    void should_search_friends_by_first_name_and_last_name_successfully() {
        SearchFriendsRequest request = new SearchFriendsRequest(
          "To",
          "To",
          "",
          "",
          SearchFriendsRelationshipStatus.ALL
        );
        User user = new User("test", "test", Instant.now(clock));
        user.setId(10L);
        User user2 = new User("test", "test", Instant.now(clock));
        user2.setId(20L);
        User user3 = new User("test", "test", Instant.now(clock));
        user3.setId(30L);

        UserProfile userProfile = new UserProfile("First name1", "Last name1", "City1", "Country1", user);
        UserProfile userProfile2 = new UserProfile("Tom", "Tomlinson", "City2", "Country2", user2);
        UserProfile userProfile3 = new UserProfile("FirstName1", "Tomlinson", "City3", "Country3", user3);
        user.setUserProfile(userProfile);
        user2.setUserProfile(userProfile2);
        user3.setUserProfile(userProfile3);
        List<User> users = List.of(user, user2, user3, mockUser);

        when(userRepository.findAll()).thenReturn(users);
        when(requestValidator.validate(request)).thenReturn(new RequestValidateResult(true, null, null));


        DataResult<Page<UserWithProfileResponse>> result = userFacade.searchFriends(request, 0, 5);

        assertTrue(result.isSuccess());
        assertTrue(result.getData().isLast());
        assertEquals(1L, result.getData().getTotalElements());
    }

    @Test
    void should_search_friends_by_all_patterns_successfully() {
        SearchFriendsRequest request = new SearchFriendsRequest(
                "Firs",
                "Last",
                "City",
                "Country",
                SearchFriendsRelationshipStatus.ALL
        );
        User user = new User("test", "test", Instant.now(clock));
        user.setId(10L);
        User user2 = new User("test", "test", Instant.now(clock));
        user2.setId(20L);
        User user3 = new User("test", "test", Instant.now(clock));
        user3.setId(30L);

        UserProfile userProfile = new UserProfile("First name1", "Last name1", "City1", "Country1", user);
        UserProfile userProfile2 = new UserProfile("Tom", "Tomlinson", "City2", "Country2", user2);
        UserProfile userProfile3 = new UserProfile("FirstName1", "Tomlinson", "City3", "Country3", user3);
        user.setUserProfile(userProfile);
        user2.setUserProfile(userProfile2);
        user3.setUserProfile(userProfile3);
        List<User> users = List.of(user, user2, user3, mockUser);

        when(userRepository.findAll()).thenReturn(users);
        when(requestValidator.validate(request)).thenReturn(new RequestValidateResult(true, null, null));


        DataResult<Page<UserWithProfileResponse>> result = userFacade.searchFriends(request, 0, 5);

        assertTrue(result.isSuccess());
        assertTrue(result.getData().isLast());
        assertEquals(1L, result.getData().getTotalElements());
    }


    @Test
    void should_search_friends_by_city_and_country_successfully() {
        SearchFriendsRequest request = new SearchFriendsRequest(
                "",
                "",
                "City",
                "Country",
                SearchFriendsRelationshipStatus.ALL
        );
        User user = new User("test", "test", Instant.now(clock));
        user.setId(10L);
        User user2 = new User("test", "test", Instant.now(clock));
        user2.setId(20L);
        User user3 = new User("test", "test", Instant.now(clock));
        user3.setId(30L);

        UserProfile userProfile = new UserProfile("First name1", "Last name1", "City1", "Country1", user);
        UserProfile userProfile2 = new UserProfile("Tom", "Tomlinson", "City2", "Country2", user2);
        UserProfile userProfile3 = new UserProfile("FirstName1", "Tomlinson", "test", "test", user3);
        user.setUserProfile(userProfile);
        user2.setUserProfile(userProfile2);
        user3.setUserProfile(userProfile3);
        List<User> users = List.of(user, user2, user3, mockUser);

        when(userRepository.findAll()).thenReturn(users);
        when(requestValidator.validate(request)).thenReturn(new RequestValidateResult(true, null, null));


        DataResult<Page<UserWithProfileResponse>> result = userFacade.searchFriends(request, 0, 5);

        assertTrue(result.isSuccess());
        assertTrue(result.getData().isLast());
        assertEquals(2L, result.getData().getTotalElements());
    }

}
