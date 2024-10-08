package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.dto.user.request.*;
import org.hl.socialspherebackend.api.dto.user.response.*;
import org.hl.socialspherebackend.api.entity.user.*;
import org.hl.socialspherebackend.application.common.Observable;
import org.hl.socialspherebackend.application.common.Observer;
import org.hl.socialspherebackend.application.util.AuthUtils;
import org.hl.socialspherebackend.application.util.FileUtils;
import org.hl.socialspherebackend.application.util.PageUtils;
import org.hl.socialspherebackend.application.validator.RequestValidateResult;
import org.hl.socialspherebackend.application.validator.RequestValidatorChain;
import org.hl.socialspherebackend.infrastructure.user.UserFriendRequestRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class UserFacade implements Observable<UserFriendRequestResponse> {

    private static final Logger log = LoggerFactory.getLogger(UserFacade.class);

    private final UserRepository userRepository;
    private final UserFriendRequestRepository userFriendRequestRepository;
    private final UserProfilePermissionChecker permissionChecker;
    private final RequestValidatorChain requestValidator;
    private final Clock clock;
    private final Set<Observer<UserFriendRequestResponse>> observers;

    public UserFacade(UserRepository userRepository,
                      UserFriendRequestRepository userFriendRequestRepository,
                      UserProfilePermissionChecker permissionChecker,
                      RequestValidatorChain requestValidator,
                      Clock clock,
                      Set<Observer<UserFriendRequestResponse>> observers) {
        this.userRepository = userRepository;
        this.userFriendRequestRepository = userFriendRequestRepository;
        this.permissionChecker = permissionChecker;
        this.requestValidator = requestValidator;
        this.clock = clock;
        this.observers = observers;
    }


    @Override
    public void addObserver(Observer<UserFriendRequestResponse> observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<UserFriendRequestResponse> observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(UserFriendRequestResponse subject) {
        observers.forEach(observer -> observer.update(subject));
    }


    public DataResult<UserFriendRequestResponse> sendFriendRequest(UserFriendRequestDto friendRequest) {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "Could not find current user!");
        }

        Long receiverId = friendRequest.receiverId();
        Optional<User> receiverOpt = userRepository.findById(receiverId);

        if(receiverOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.RECEIVER_NOT_FOUND,
                    "receiver with id = %d does not exits in database!".formatted(receiverId));
        }
        User sender = currentUserOpt.get();
        User receiver = receiverOpt.get();

        List<User> senderFriends = userRepository.findUserFriends(sender.getId());
        boolean isReceiverInSenderFriendList = senderFriends.stream()
                .anyMatch(friend -> friend.equals(receiver));

        if(isReceiverInSenderFriendList) {
            return DataResult.failure(UserErrorCode.RECEIVER_IS_ALREADY_FRIEND,
                    "Receiver with id = %d is already in current user friend list!".formatted(receiverId));
        }

        boolean didReceiverSentFriendRequest = userFriendRequestRepository.findSentFriendRequestsByUserId(receiver.getId())
                .stream()
                .anyMatch(request -> request.getSender().equals(receiver) && request.getReceiver().equals(sender)
                        && request.getStatus().equals(UserFriendRequestStatus.WAITING_FOR_RESPONSE));

        if(didReceiverSentFriendRequest) {
            return DataResult.failure(UserErrorCode.RECEIVER_ALREADY_SENT_FRIEND_REQUEST,
                    "Receiver with id = %d already sent you friend request!".formatted(receiverId));
        }

        boolean didSenderSentFriendRequest = userFriendRequestRepository.findSentFriendRequestsByUserId(sender.getId())
                .stream()
                .anyMatch(request -> request.getSender().equals(sender) && request.getReceiver().equals(receiver)
                        && request.getStatus().equals(UserFriendRequestStatus.WAITING_FOR_RESPONSE));

        if(didSenderSentFriendRequest) {
            return DataResult.failure(UserErrorCode.SENDER_ALREADY_SENT_FRIEND_REQUEST, "You already sent friend request!");
        }

        Instant now = Instant.now(clock);
        UserFriendRequest userFriendRequest = new UserFriendRequest(sender, receiver, UserFriendRequestStatus.WAITING_FOR_RESPONSE, now);
        userFriendRequestRepository.save(userFriendRequest);

        UserFriendRequestResponse response = UserMapper.fromEntityToResponse(userFriendRequest);
        notifyObservers(response);
        return DataResult.success(response);
    }

    public DataResult<UserFriendRequestResponse>  acceptFriendRequest(Long friendRequestId) {
        return responseToFriendRequest(friendRequestId, UserFriendRequestStatus.ACCEPTED);
    }

    public DataResult<UserFriendRequestResponse>  rejectFriendRequest(Long friendRequestId) {
        return responseToFriendRequest(friendRequestId, UserFriendRequestStatus.REJECTED);
    }

    private DataResult<UserFriendRequestResponse>  responseToFriendRequest(Long friendRequestId, UserFriendRequestStatus status) {
        Optional<UserFriendRequest> userFriendRequestOpt = userFriendRequestRepository.findById(friendRequestId);
        if(userFriendRequestOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.FRIEND_REQUEST_NOT_FOUND,
                    "Could not find friend request with id = %d".formatted(friendRequestId));
        }

        Optional<User> senderOpt = AuthUtils.getCurrentUser();

        if(senderOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.SENDER_NOT_FOUND, "Current user not found!");
        }

        UserFriendRequest userFriendRequest = userFriendRequestOpt.get();

        userFriendRequest.setStatus(status);
        Instant now = Instant.now(clock);
        userFriendRequest.setRepliedAt(now);

        userFriendRequestRepository.save(userFriendRequest);
        if(status.equals(UserFriendRequestStatus.ACCEPTED)) {
            User s = userFriendRequest.getSender();
            User r = userFriendRequest.getReceiver();
            s.setFriends(userRepository.findFriendsField(s.getId()));
            r.setInverseFriends(userRepository.findInverseFriendsField(r.getId()));
            s.appendFriend(r);
            userRepository.save(s);
        }

        UserFriendRequestResponse response = UserMapper.fromEntityToResponse(userFriendRequest);
        notifyObservers(response);
        return DataResult.success(response);
    }

    public DataResult<UserProfileResponse> createUserProfile(UserProfileRequest request, MultipartFile profilePicture) {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }

        User user = userOpt.get();

        if(user.getUserProfile() != null) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_ALREADY_EXISTS,
                    "User with id = %d already have profile.");
        }

        RequestValidateResult validateResult = requestValidator.validate(request);
        if(!validateResult.valid()) {
            return DataResult.failure(validateResult.errorCode(), validateResult.errorMessage());
        }

        UserProfile userProfile = UserMapper.fromRequestToEntity(request, user);

        byte[] imgResponse = null;
        if(profilePicture != null) {
            String imgType = profilePicture.getOriginalFilename();
            try {
                imgResponse = profilePicture.getBytes();
                byte[] compressedProfilePicture = FileUtils.compressFile(imgResponse);
                UserProfilePicture userProfilePicture = new UserProfilePicture(imgType, compressedProfilePicture);
                userProfile.setProfilePicture(userProfilePicture);
            } catch (IOException exc) {
                log.debug("Problem occurred with getting bytes in profile picture - {}", profilePicture);
            }
        }

        user.setUserProfile(userProfile);
        userRepository.save(user);

        UserProfileResponse response = UserMapper.fromEntityToResponse(userProfile);
        return DataResult.success(response);
    }


    public DataResult<UserProfileConfigResponse> createUserProfileConfig(UserProfileConfigRequest request) {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }

        User user = userOpt.get();

        if(user.getUserProfileConfig() != null) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_CONFIG_ALREADY_EXISTS,
                    "User with id = %d already have profile config.");
        }

        UserProfileConfig userProfileConfig = UserMapper.fromRequestToEntity(request, user);
        user.setUserProfileConfig(userProfileConfig);

        userRepository.save(user);

        UserProfileConfigResponse response = UserMapper.fromEntityToResponse(userProfileConfig);
        return DataResult.success(response);
    }


    public DataResult<UserWrapperResponse> findCurrentUser() {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "Could not find current user!");
        }
        User user = currentUserOpt.get();
        UserWrapperResponse response = UserMapper.fromEntityToUserWrapperResponse(user, RelationshipStatus.YOU);
        return DataResult.success(response);
    }

    public DataResult<UserHeaderResponse> findCurrentUserHeader() {
        DataResult<UserWrapperResponse> result = findCurrentUser();
        if(result.isFailure()) {
            return DataResult.failure(result.getErrorCode(), result.getErrorMessage(), result.getHttpStatus());
        }

        UserWrapperResponse data = result.getData();
        UserHeaderResponse response = new UserHeaderResponse(
                data.user().id(),
                data.userProfile().firstName(),
                data.userProfile().lastName(),
                data.userProfile().profilePicture(),
                data.user().relationshipStatus()
        );
        return DataResult.success(response);
    }

    public DataResult<UserWithProfileResponse> findUserById(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId));
        }
        User user = userOpt.get();
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }
        User currentUser = currentUserOpt.get();

        RelationshipStatus status = getRelationshipStatusFromUser(currentUser, user);
        UserWithProfileResponse response = UserMapper.fromEntityToUserWithProfileResponse(user, status);
        return DataResult.success(response);
    }


    public DataResult<UserProfileResponse> findCurrentUserProfile() {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }
        User user = currentUserOpt.get();
        return getUserProfileResult(user);
    }

    public DataResult<UserProfileResponse> findUserProfileByUserId(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exists in database!".formatted(userId));
        }
        User user = userOpt.get();
        return getUserProfileResult(user);
    }

    private DataResult<UserProfileResponse> getUserProfileResult(User user) {
        UserProfile userProfile = user.getUserProfile();

        if(userProfile == null) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_NOT_FOUND,
                    "User with id = %d does not have profile in database!".formatted(user.getId()));
        }

        UserProfileResponse response = UserMapper.fromEntityToResponse(userProfile);
        return DataResult.success(response);
    }

    public DataResult<UserProfileConfigResponse> findCurrentUserProfileConfig() {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }
        User user = currentUserOpt.get();
        return getUserProfileConfigResult(user);
    }

    public DataResult<UserProfileConfigResponse> findUserProfileConfigByUserId(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exits in database".formatted(userId));
        }

        User user = userOpt.get();
        return getUserProfileConfigResult(user);
    }

    private DataResult<UserProfileConfigResponse> getUserProfileConfigResult(User user) {
        UserProfileConfig userProfileConfig = user.getUserProfileConfig();

        if(userProfileConfig == null) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_CONFIG_NOT_FOUND,
                    "User profile config with user id = %d does not exits in database!".formatted(user.getId()));
        }

        UserProfileConfigResponse response = UserMapper.fromEntityToResponse(userProfileConfig);
        return DataResult.success(response);
    }

    public DataResult<Set<UserWithProfileResponse>> findCurrentUserFriends() {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "Could not find current user!");
        }

        User user = userOpt.get();

        Set<User> userFriends = user.getFriends();
        if(userFriends.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_HAS_NO_FRIENDS,
                    "Current user has no friends!");
        }

        Set<UserWithProfileResponse> userFriendsResponse = userFriends
                .stream()
                .map(u -> UserMapper.fromEntityToUserWithProfileResponse(u, RelationshipStatus.FRIEND))
                .collect(toSet());

        return DataResult.success(userFriendsResponse);
    }

    public DataResult<Page<UserWithProfileResponse>> findCurrentUserFriends(int page, int size) {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "Could not find current user!");
        }

        User currentUser = currentUserOpt.get();

        List<User> userFriends = userRepository.findUserFriends(currentUser.getId());
        if(userFriends.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_HAS_NO_FRIENDS,
                    "Current user has no friends!");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userFriendsPage = PageUtils.createPageImpl(userFriends, pageable);
        Page<UserWithProfileResponse> response =
                userFriendsPage.map(u -> UserMapper.fromEntityToUserWithProfileResponse(u, RelationshipStatus.FRIEND));
        return DataResult.success(response);
    }

    public DataResult<Page<UserWithProfileResponse>> findUserFriends(Long userId, int page, int size) {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        Optional<User> userToCheckOpt = userRepository.findById(userId);
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "Could not find current user!");
        }
        if(userToCheckOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId));
        }
        User currentUser = currentUserOpt.get();
        User userToCheck = userToCheckOpt.get();

        UserPermissionCheckResult permissionResult = permissionChecker.checkUserProfileResourceAccess(currentUser, userToCheck);
        if(!permissionResult.allowed()) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_ACCESS_NOT_ALLOWED, permissionResult.notAllowedErrorMessage());
        }

        List<User> userToCheckFriends = userRepository.findUserFriends(userId);
        if(userToCheckFriends.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_HAS_NO_FRIENDS,
                    "User to check with id = %d does not have friends!".formatted(userId),
                    HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userToCheckFriendsPage = PageUtils.createPageImpl(userToCheckFriends, pageable);

        Page<UserWithProfileResponse> response = userToCheckFriendsPage.map(u -> {
            RelationshipStatus status = getRelationshipStatusFromUser(currentUser, u);
            return UserMapper.fromEntityToUserWithProfileResponse(u, status);
        });
        return DataResult.success(response);
    }

    public DataResult<Set<UserWithProfileResponse>> findCurrentUserFriendsWithNoSharedChat() {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "Could not find current user in security context!", HttpStatus.UNAUTHORIZED);
        }
        User currentUser = currentUserOpt.get();

        List<User> userFriendsWithNoSharedChat = userRepository.findUserFriendsWithNoSharedChat(currentUser.getId());
        if(userFriendsWithNoSharedChat.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_HAS_NO_FRIENDS, "Current user has no friends with no shared chats!");
        }
        Set<UserWithProfileResponse> response = userFriendsWithNoSharedChat.stream()
                .map((u) -> UserMapper.fromEntityToUserWithProfileResponse(u, RelationshipStatus.FRIEND))
                .collect(toSet());

        return DataResult.success(response);
    }

    public DataResult<Set<UserHeaderResponse>> findUsers(final String pattern, Integer size) {
        if(pattern == null || pattern.isBlank()) {
            return DataResult.failure(UserErrorCode.SEARCH_USERS_NOT_FOUND, "Pattern cannot be null or blank");
        }
        final String patternLowerCase = pattern.toLowerCase();

        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }
        User currentUser = currentUserOpt.get();

        List<User> userEntities = userRepository.findAll();
        if(userEntities.isEmpty()) {
            return DataResult.failure(UserErrorCode.USERS_NOT_FOUND, "There are no users in database");
        }

        Set<User> userEntitiesFiltered = userEntities
                .stream()
                .filter(u -> u.getUserProfile() != null)
                .filter(u -> {
                    String firstName = u.getUserProfile().getFirstName().toLowerCase();
                    String lastName = u.getUserProfile().getLastName().toLowerCase();
                    String name = firstName + " " + lastName;
                    return firstName.startsWith(patternLowerCase) || lastName.startsWith(patternLowerCase) || name.startsWith(patternLowerCase);
                })
                .filter(u -> !u.equals(currentUser))
                .limit(size)
                .collect(toSet());

        if(userEntitiesFiltered.isEmpty()) {
            return DataResult.failure(UserErrorCode.SEARCH_USERS_NOT_FOUND, "There are no users in database");
        }

        Set<UserHeaderResponse> response = userEntitiesFiltered
                .stream()
                .map(u -> UserMapper.fromEntityToUserHeaderResponse(u, getRelationshipStatusFromUser(currentUser, u)))
                .collect(toSet());

        return DataResult.success(response);
    }

    public DataResult<byte[]> findCurrentUserProfilePicture() {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }
        User user = userOpt.get();

        UserProfile userProfile = user.getUserProfile();
        if(userProfile == null) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_NOT_FOUND, "Current user has no profile");
        }

        UserProfilePicture userProfilePicture = userProfile.getProfilePicture();
        if(userProfilePicture == null) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_PICTURE_NOT_FOUND, "Current user has no profile picture!");
        }

        byte[] response = FileUtils.decompressFile(userProfilePicture.getImage());
        return DataResult.success(response);
    }

    public DataResult<Set<UserFriendNotificationResponse>> findCurrentUserFriendNotifications() {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }
        User user = userOpt.get();
        Long userId = user.getId();

        List<UserFriendRequest> receivedFriendRequests = userFriendRequestRepository.findReceivedFriendRequestsByUserId(userId);
        List<UserFriendRequest> sentFriendRequests = userFriendRequestRepository.findSentFriendRequestsByUserId(userId);

        Stream<UserFriendRequest> waitingForResponseFriendRequests = receivedFriendRequests.stream()
                .filter(fr -> fr.getStatus().equals(UserFriendRequestStatus.WAITING_FOR_RESPONSE));

        Stream<UserFriendRequest> repliedFriendRequests = sentFriendRequests.stream()
                .filter(fr -> !fr.getStatus().equals(UserFriendRequestStatus.WAITING_FOR_RESPONSE));

        Set<UserFriendNotificationResponse> response = Stream.concat(waitingForResponseFriendRequests, repliedFriendRequests)
                .sorted(Comparator.comparing(UserFriendRequest::getSentAt))
                .map(fr -> getNotificationFromFriendRequest(fr, user))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if(response.isEmpty()) {
            return DataResult.failure(UserErrorCode.FRIEND_NOTIFICATIONS_NOT_FOUND, "Current user has no friend notifications");
        }
        return DataResult.success(response);
    }

    private UserFriendNotificationResponse getNotificationFromFriendRequest(UserFriendRequest friendRequest, User currentUser) {
        UserFriendRequestStatus status = friendRequest.getStatus();
        User sender;
        Instant sentAt;
        if(status.equals(UserFriendRequestStatus.WAITING_FOR_RESPONSE)) {
            sender = friendRequest.getSender();
            sentAt = friendRequest.getSentAt();
        } else {
            sender = friendRequest.getReceiver();
            sentAt = friendRequest.getRepliedAt();
        }
        RelationshipStatus relationshipStatus = getRelationshipStatusFromUser(currentUser, sender);
        UserHeaderResponse senderHeader = UserMapper.fromEntityToUserHeaderResponse(sender, relationshipStatus);
        return new UserFriendNotificationResponse(friendRequest.getId(), senderHeader, status, sentAt);
    }

    public DataResult<UserFriendRequestResponse> findUserFriendRequestForCurrentUser(Long userId) {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        Optional<User> userOpt = userRepository.findById(userId);
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }

        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find user with id = %d".formatted(userId));
        }

        User currentUser = currentUserOpt.get();
        User user = userOpt.get();
        Optional<UserFriendRequest> friendRequestOpt =
                userFriendRequestRepository.findFriendRequestBySenderIdAndReceiverId(user.getId(), currentUser.getId());

        if(friendRequestOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.FRIEND_REQUEST_NOT_FOUND,
                    "Could not find friend request sent to current user from user with id = %d".formatted(user.getId()));
        }

        UserFriendRequestResponse response = UserMapper.fromEntityToResponse(friendRequestOpt.get());
        return DataResult.success(response);
    }

    public DataResult<Page<UserWithProfileResponse>> findUsersBySearchFriendsRelationshipStatus(SearchFriendsRelationshipStatus relationshipStatus, int page, int size) {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }

        User currentUser = currentUserOpt.get();
        List<User> users = getUsersBySearchRelationshipStatus(relationshipStatus, currentUser.getId())
                .stream()
                .filter(u -> !u.equals(currentUser))
                .toList();

        Page<User> userPage = PageUtils.createPageImpl(users, PageRequest.of(page, size));
        if(userPage.isEmpty()) {
            return DataResult.failure(UserErrorCode.SEARCH_USERS_NOT_FOUND,
                    "Could not find users with relationship status = %s".formatted(relationshipStatus));
        }
        Page<UserWithProfileResponse> response =
                userPage.map(u -> UserMapper.fromEntityToUserWithProfileResponse(u, getRelationshipStatusFromUser(currentUser, u)));
        return DataResult.success(response);
    }

    public DataResult<Page<UserWithProfileResponse>> searchFriends(SearchFriendsRequest request, int page, int size) {
        RequestValidateResult validateResult = requestValidator.validate(request);
        if(!validateResult.valid()) {
            return DataResult.failure(validateResult.errorCode(), validateResult.errorMessage());
        }

        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }

        User currentUser = currentUserOpt.get();
        Long currentUserId = currentUser.getId();
        SearchFriendsRelationshipStatus status = request.relationshipStatus();
        List<User> usersFilteredByRelationshipStatus = getUsersBySearchRelationshipStatus(status, currentUserId);

        if(usersFilteredByRelationshipStatus.isEmpty()) {
            return DataResult.failure(UserErrorCode.SEARCH_USERS_NOT_FOUND,
                    "There are no users that match relationship status = '%s'".formatted(status));
        }

        String firstNamePattern = request.firstNamePattern();
        List<User> usersFilteredByFirstName;
        if(firstNamePattern == null || firstNamePattern.isEmpty()) {
            usersFilteredByFirstName = usersFilteredByRelationshipStatus;
        } else {
            final String firstNamePatternToLowerCase = firstNamePattern.toLowerCase();
            usersFilteredByFirstName = usersFilteredByRelationshipStatus.stream()
                    .filter(u -> u.getUserProfile().getFirstName().toLowerCase().startsWith(firstNamePatternToLowerCase))
                    .toList();
        }

        if(usersFilteredByFirstName.isEmpty()) {
            return DataResult.failure(UserErrorCode.SEARCH_USERS_NOT_FOUND,
                    "There are no users that match firstNamePattern = '%s'".formatted(firstNamePattern));
        }

        String lastNamePattern = request.lastNamePattern();
        List<User> usersFilteredByLastName ;
        if(lastNamePattern == null || lastNamePattern.isEmpty()) {
            usersFilteredByLastName = usersFilteredByFirstName;
        } else {
            final String lastNamePatternToLowerCase = lastNamePattern.toLowerCase();
            usersFilteredByLastName = usersFilteredByFirstName.stream()
                    .filter(u -> u.getUserProfile().getLastName().toLowerCase().startsWith(lastNamePatternToLowerCase))
                    .toList();
        }

        if(usersFilteredByLastName.isEmpty()) {
            return DataResult.failure(UserErrorCode.SEARCH_USERS_NOT_FOUND,
                    "There are no users that match lastNamePattern = '%s'".formatted(lastNamePattern));
        }

        String cityPattern = request.cityPattern();
        List<User> usersFilteredByCity;
        if(cityPattern == null || cityPattern.isEmpty()) {
            usersFilteredByCity = usersFilteredByLastName;
        } else {
            final String cityPatternToLowerCase = cityPattern.toLowerCase();
            usersFilteredByCity = usersFilteredByLastName.stream()
                    .filter(u -> u.getUserProfile().getCity().toLowerCase().startsWith(cityPatternToLowerCase))
                    .toList();
        }

        if(usersFilteredByCity.isEmpty()) {
            return DataResult.failure(UserErrorCode.SEARCH_USERS_NOT_FOUND,
                    "There are no users that match cityPattern = '%s'".formatted(cityPattern));
        }

        String countryPattern = request.countryPattern();
        List<User> usersFilteredByCountry;
        if(countryPattern == null || countryPattern.isEmpty()) {
            usersFilteredByCountry = usersFilteredByCity;
        } else {
            final String countryPatternToLowerCase = countryPattern.toLowerCase();
            usersFilteredByCountry = usersFilteredByCity.stream()
                    .filter(u -> u.getUserProfile().getCountry().toLowerCase().startsWith(countryPatternToLowerCase))
                    .toList();
        }

        List<User> usersWithFilteredCurrentUser = usersFilteredByCountry.stream()
                .filter(u -> !u.equals(currentUser))
                .toList();

        if(usersWithFilteredCurrentUser.isEmpty()) {
            return DataResult.failure(UserErrorCode.SEARCH_USERS_NOT_FOUND,
                    "There are no users that match countryPattern = '%s'".formatted(countryPattern));
        }

        Page<User> pageOfFilteredUsers = PageUtils.createPageImpl(usersWithFilteredCurrentUser, PageRequest.of(page, size));

        Page<UserWithProfileResponse> response = pageOfFilteredUsers
                .map(u -> UserMapper.fromEntityToUserWithProfileResponse(u, getRelationshipStatusFromUser(currentUser, u)));

        return DataResult.success(response);
    }


    public DataResult<UserProfileResponse> updateCurrentUserProfile(UserProfileRequest request, MultipartFile profilePicture) {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "Could not find current user!");
        }
        User user = userOpt.get();

        UserProfile userProfile = user.getUserProfile();
        if(userProfile == null) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_NOT_FOUND,
                    "Current user has no profile!");
        }

        RequestValidateResult validateResult = requestValidator.validate(request);
        if(!validateResult.valid()) {
            return DataResult.failure(validateResult.errorCode(), validateResult.errorMessage());
        }

        updateUserProfileEntity(userProfile, request, profilePicture);
        userRepository.save(user);

        UserProfileResponse response = UserMapper.fromEntityToResponse(userProfile);
        return DataResult.success(response);
    }

    private void updateUserProfileEntity(UserProfile entity, UserProfileRequest request, MultipartFile profilePicture) {
        entity.setFirstName(request.firstName());
        entity.setLastName(request.lastName());
        entity.setCity(request.city());
        entity.setCountry(request.country());

        if(profilePicture != null) {
            UserProfilePicture entityProfilePicture = entity.getProfilePicture();
            try {
                String imgType = profilePicture.getContentType();
                byte[] compressedImg = FileUtils.compressFile(profilePicture.getBytes());
                if(entityProfilePicture == null) {
                    entityProfilePicture = new UserProfilePicture(imgType, compressedImg);
                } else {
                    entityProfilePicture.setImage(compressedImg);
                    entityProfilePicture.setImageType(imgType);
                }
                entity.setProfilePicture(entityProfilePicture);
            } catch (IOException exc) {
                log.error("Error occurred with decompressing file - {}", profilePicture);
            }

        }

    }

    public DataResult<UserProfileConfigResponse> updateCurrentUserProfileConfig(UserProfileConfigRequest request) {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "Could not find current user!");
        }

        User user = userOpt.get();
        UserProfileConfig userProfileConfig = user.getUserProfileConfig();

        if(userProfileConfig == null) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_CONFIG_NOT_FOUND,
                    "Current user has no profile config!");
        }

        updateUserProfileConfigEntity(userProfileConfig, request);
        user.setUserProfileConfig(userProfileConfig);
        userRepository.save(user);

        UserProfileConfigResponse response = UserMapper.fromEntityToResponse(userProfileConfig);
        return DataResult.success(response);
    }

    private void updateUserProfileConfigEntity(UserProfileConfig entity, UserProfileConfigRequest request) {
        entity.setUserProfilePrivacyLevel(request.profilePrivacyLevel());
    }

    public DataResult<String> removeFromFriendList(Long friendId) {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        Optional<User> friendOpt = userRepository.findById(friendId);
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "Could not find current user!");
        }

        if(friendOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "User friend with id=%d not found".formatted(friendId));
        }

        User user = userOpt.get();

        userRepository.removeFromFriends(user.getId(), friendId);
        return DataResult.success("Successfully removed user with id=%d from friend list".formatted(friendId));
    }

    public DataResult<Boolean> isCurrentUserWaitingForFriendResponse(Long userId) {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user in security context");
        }
        User currentUser = currentUserOpt.get();

        boolean response = userFriendRequestRepository.isSenderWaitingForFriendResponse(currentUser.getId(), userId);
        return DataResult.success(response);
    }

    public DataResult<Boolean> isUserWaitingForCurrentUserFriendResponse(Long userId) {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user in security context");
        }
        User currentUser = currentUserOpt.get();

        boolean response = userFriendRequestRepository.isSenderWaitingForFriendResponse(userId, currentUser.getId());
        return DataResult.success(response);
    }

    public DataResult<Boolean> isCurrentUserHasPermissionToCheckProfile(Long userId) {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        Optional<User> userToCheckOpt = userRepository.findById(userId);
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user in security context");
        }
        if(userToCheckOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find user with id = %d".formatted(userId));
        }

        User currentUser = currentUserOpt.get();
        User userToCheck = userToCheckOpt.get();

        UserPermissionCheckResult permissionCheckResult = permissionChecker.checkUserProfileResourceAccess(currentUser, userToCheck);
        if(!permissionCheckResult.allowed()) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_ACCESS_NOT_ALLOWED, permissionCheckResult.notAllowedErrorMessage());
        }

        return DataResult.success(true);
    }

    private RelationshipStatus getRelationshipStatusFromUser(User currentUser, User user) {
        if(user.equals(currentUser)) {
            return RelationshipStatus.YOU;
        }

        RelationshipStatus relationshipStatus;
        boolean isFriend = userRepository.findUserFriends(currentUser.getId())
                .stream()
                .anyMatch(u -> u.equals(user));

        if(isFriend) {
            relationshipStatus = RelationshipStatus.FRIEND;
        } else {
            relationshipStatus = RelationshipStatus.STRANGER;
        }

        return relationshipStatus;
    }

    private List<User> getUsersBySearchRelationshipStatus(SearchFriendsRelationshipStatus status, Long currentUserId) {
        List<User> users;
        if(status.equals(SearchFriendsRelationshipStatus.FRIENDS)) {
            users = userRepository.findUserFriends(currentUserId);
        } else if(status.equals(SearchFriendsRelationshipStatus.STRANGER)) {
            users = userRepository.findUserStrangers(currentUserId);
        } else {
            users = userRepository.findAll();
        }
        return users;
    }

}
