package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.dto.user.request.UserFriendRequestDto;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileConfigRequest;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            return DataResult.failure(UserErrorCode.SENDER_NOT_FOUND, "Current user not found!", HttpStatus.BAD_REQUEST);
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

    public DataResult<UserWrapperResponse> findUserById(Long userId) {
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
        UserWrapperResponse response = UserMapper.fromEntityToUserWrapperResponse(user, status);
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

    public DataResult<Set<UserWrapperResponse>> findCurrentUserFriends() {
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

        Set<UserWrapperResponse> userFriendsResponse = userFriends
                .stream()
                .map(u -> UserMapper.fromEntityToUserWrapperResponse(u, RelationshipStatus.FRIEND))
                .collect(toSet());

        return DataResult.success(userFriendsResponse);
    }

    public DataResult<Page<UserWrapperResponse>> findCurrentUserFriends(int page, int size) {
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
        Page<UserWrapperResponse> response = userFriendsPage.map(u -> UserMapper.fromEntityToUserWrapperResponse(u, RelationshipStatus.FRIEND));
        return DataResult.success(response);
    }

    public DataResult<Page<UserWrapperResponse>> findUserFriends(Long userId, int page, int size) {
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

        Page<UserWrapperResponse> response = userToCheckFriendsPage.map(u -> {
            RelationshipStatus status = getRelationshipStatusFromUser(currentUser, u);
            return UserMapper.fromEntityToUserWrapperResponse(u, status);
        });
        return DataResult.success(response);
    }

    public DataResult<Set<UserWrapperResponse>> findCurrentUserFriendsWithNoSharedChat() {
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
        Set<UserWrapperResponse> response = userFriendsWithNoSharedChat.stream()
                .map((u) -> UserMapper.fromEntityToUserWrapperResponse(u, RelationshipStatus.FRIEND))
                .collect(toSet());

        return DataResult.success(response);
    }

    public DataResult<Set<UserHeaderResponse>> findUsers(final String pattern, Integer size) {
        if(pattern == null || pattern.isBlank()) {
            return DataResult.failure(UserErrorCode.SEARCH_USERS_NOT_FOUND, "Pattern cannot be null or blank");
        }

        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }
        User currentUser = currentUserOpt.get();

        List<User> userEntities = userRepository.findAll();
        if(userEntities.isEmpty()) {
            return DataResult.failure(UserErrorCode.USERS_NOT_FOUND, "There are no users in database");
        }

        List<User> userEntitiesWithProfiles = userEntities
                .stream()
                .filter(u -> u.getUserProfile() != null)
                .toList();

        if(userEntitiesWithProfiles.isEmpty()) {
            return DataResult.failure(UserErrorCode.USERS_NOT_FOUND, "There are no users in database");
        }

        Pattern compiledPattern = Pattern.compile("^" + pattern + ".*$", Pattern.CASE_INSENSITIVE);

        List<User> userEntitiesThatContainsFirstNameString = userEntitiesWithProfiles
                .stream()
                .filter(u -> matchFirstName(compiledPattern, u))
                .toList();

        List<User> userEntitiesResponse = new ArrayList<>(size);

        if(!userEntitiesThatContainsFirstNameString.isEmpty()) {
            for(int i = 0; i < size && i < userEntitiesThatContainsFirstNameString.size(); i++) {
                userEntitiesResponse.add(userEntitiesThatContainsFirstNameString.get(i));
            }
        }

        if(userEntitiesResponse.size() < size) {
            List<User> userEntitiesThatContainsLastNameString = userEntitiesWithProfiles.stream()
                    .filter(u -> checkUserInList(userEntitiesThatContainsFirstNameString, u))
                    .filter(u -> matchLastName(compiledPattern, u))
                    .toList();

            for(int i = userEntitiesResponse.size(); i < size && i < userEntitiesThatContainsLastNameString.size(); i++) {
                userEntitiesResponse.add(userEntitiesThatContainsLastNameString.get(i));
            }
        }

        Set<UserHeaderResponse> response = userEntitiesResponse
                .stream()
                .map(u -> UserMapper.fromEntityToUserHeaderResponse(u, getRelationshipStatusFromUser(currentUser, u)))
                .collect(toSet());


        if(response.isEmpty()) {
            return DataResult.failure(UserErrorCode.SEARCH_USERS_NOT_FOUND, "There are no users with these parameters");
        }

        return DataResult.success(response);
    }

    private boolean matchFirstName(Pattern pattern, User user) {
        String firstName = user.getUserProfile().getFirstName();
        return matchUserField(pattern, firstName);
    }

    private boolean matchLastName(Pattern pattern, User user) {
        String lastName = user.getUserProfile().getLastName();
        return matchUserField(pattern, lastName);
    }

    private boolean matchUserField(Pattern pattern, String matcher) {
        Matcher m = pattern.matcher(matcher);
        return m.matches();
    }

    private boolean checkUserInList(List<User> users, User user) {
        for(User u : users) {
            if(u.equals(user)) return false;
        }
        return true;
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

}
