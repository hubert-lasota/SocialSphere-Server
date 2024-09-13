package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.dto.user.request.UserFriendRequestDto;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileConfigRequest;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
import org.hl.socialspherebackend.api.dto.user.response.*;
import org.hl.socialspherebackend.api.entity.chat.UserFriendRequest;
import org.hl.socialspherebackend.api.entity.chat.UserFriendRequestStatus;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.api.entity.user.UserProfile;
import org.hl.socialspherebackend.api.entity.user.UserProfileConfig;
import org.hl.socialspherebackend.api.entity.user.UserProfilePicture;
import org.hl.socialspherebackend.application.common.Observable;
import org.hl.socialspherebackend.application.common.Observer;
import org.hl.socialspherebackend.application.util.AuthUtils;
import org.hl.socialspherebackend.application.util.FileUtils;
import org.hl.socialspherebackend.application.util.PageUtils;
import org.hl.socialspherebackend.application.util.UserUtils;
import org.hl.socialspherebackend.application.validator.RequestValidateResult;
import org.hl.socialspherebackend.application.validator.RequestValidatorChain;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toSet;

public class UserFacade implements Observable<UserFriendRequestResponse> {

    private static final Logger log = LoggerFactory.getLogger(UserFacade.class);

    private final UserRepository userRepository;
    private final UserProfilePermissionChecker permissionChecker;
    private final RequestValidatorChain requestValidator;
    private final Clock clock;
    private final Set<Observer<UserFriendRequestResponse>> observers;

    public UserFacade(UserRepository userRepository,
                      UserProfilePermissionChecker permissionChecker,
                      RequestValidatorChain requestValidator,
                      Clock clock,
                      Set<Observer<UserFriendRequestResponse>> observers) {
        this.userRepository = userRepository;
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
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }

        Long receiverId = friendRequest.receiverId();
        Optional<User> receiverOpt = userRepository.findById(receiverId);

        if(receiverOpt.isEmpty()) {
            return DataResult.failure(
                    UserErrorCode.RECEIVER_NOT_FOUND,
                    "receiver with id = %d does not exits in database!".formatted(receiverId)
            );
        }
        User sender = currentUserOpt.get();
        User receiver = receiverOpt.get();

        boolean didSenderSentFriendRequest = sender.getSentFriendRequests()
                .stream()
                .anyMatch(request -> request.getSender().equals(sender) && request.getReceiver().equals(receiver));

        if(didSenderSentFriendRequest) {
            return DataResult.failure(UserErrorCode.SENDER_ALREADY_SENT_FRIEND_REQUEST,
                    "You already sent friend request!");
        }

        List<User> senderFriends = userRepository.findUserFriends(sender.getId());
        boolean isReceiverInSenderFriendList = senderFriends.stream()
                .anyMatch(friend -> friend.equals(receiver));

        if(isReceiverInSenderFriendList) {
            return DataResult.failure(UserErrorCode.RECEIVER_IS_ALREADY_FRIEND,
                    "Receiver with id = %d is already in current user friend list!".formatted(receiverId));
        }

        UserFriendRequest userFriendRequest = new UserFriendRequest(sender, receiver, UserFriendRequestStatus.WAITING_FOR_RESPONSE);
        userRepository.save(sender);

        UserFriendRequestResponse response = UserMapper.fromEntityToResponse(userFriendRequest);
        notifyObservers(response);
        return DataResult.success(response);
    }

    public DataResult<UserFriendRequestResponse>  acceptFriendRequest(UserFriendRequestDto request) {
        return responseToFriendRequest(request.receiverId(), UserFriendRequestStatus.ACCEPTED);
    }

    public DataResult<UserFriendRequestResponse>  rejectFriendRequest(UserFriendRequestDto request) {
        return responseToFriendRequest(request.receiverId(), UserFriendRequestStatus.REJECTED);
    }

    private DataResult<UserFriendRequestResponse>  responseToFriendRequest(Long receiverId, UserFriendRequestStatus status) {
        Optional<User> senderOpt = AuthUtils.getCurrentUser();
        Optional<User> receiverOpt = userRepository.findById(receiverId);

        if(senderOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.SENDER_NOT_FOUND, "Current user not found!");
        }
        if(receiverOpt.isEmpty()) {
            return DataResult.failure(
                    UserErrorCode.RECEIVER_NOT_FOUND,
                    "receiver with id = %d does not exits in database!".formatted(receiverId)
            );
        }

        User sender = senderOpt.get();
        User receiver = receiverOpt.get();

        Set<UserFriendRequest> friendRequests = receiver.getReceivedFriendRequests()
                .stream()
                .filter(fr -> fr.getSender().equals(sender))
                .collect(toSet());

        if(friendRequests.isEmpty()) {
            return DataResult.failure(
                    UserErrorCode.FRIEND_REQUEST_NOT_FOUND,
                    "Friend request does not exits in database!"
            );
        }

        if(friendRequests.size() > 1) {
            log.error("There is {} friend requests. Only one will be stored in database, rest are going to be removed",
                    friendRequests.size());
        }

        UserFriendRequest friendRequest = friendRequests.
                stream().
                findFirst().
                get();

        friendRequest.setStatus(status);

        userRepository.save(sender);

        UserFriendRequestResponse response = UserMapper.fromEntityToResponse(friendRequest);
        notifyObservers(response);
        return DataResult.success(response);
    }

    public DataResult<UserProfileResponse> createUserProfile(UserProfileRequest request, MultipartFile profilePicture) {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "Could not find current user!");
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
            return DataResult.failure(
                    UserErrorCode.USER_PROFILE_CONFIG_ALREADY_EXISTS,
                    "User with id = %d already have profile config. Use POST endpoint"
            );
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
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }
        User user = currentUserOpt.get();
        UserWrapperResponse response = UserMapper.fromEntityToUserWrapperResponse(user, RelationshipStatus.YOU);
        return DataResult.success(response);
    }

    public DataResult<UserHeaderResponse> findCurrentUserHeader() {
        DataResult<UserWrapperResponse> result = findCurrentUser();
        if(result.isFailure()) {
            return DataResult.failure(result.getErrorCode(), result.getErrorMessage());
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

        RelationshipStatus status = UserUtils.getRelationshipStatusFromUser(currentUser, user);
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
            return DataResult.failure(
                    UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exists in database!".formatted(userId)
            );
        }
        User user = userOpt.get();
        return getUserProfileResult(user);
    }

    private DataResult<UserProfileResponse> getUserProfileResult(User user) {
        UserProfile userProfile = user.getUserProfile();

        if(userProfile == null) {
            return DataResult.failure(
                    UserErrorCode.USER_PROFILE_NOT_FOUND,
                    "User with id = %d does not have profile in database!".formatted(user.getId())
            );
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
            return DataResult.failure(
              UserErrorCode.USER_NOT_FOUND,
              "User with id = %d does not exits in database".formatted(userId)
            );
        }

        User user = userOpt.get();
        return getUserProfileConfigResult(user);
    }

    private DataResult<UserProfileConfigResponse> getUserProfileConfigResult(User user) {
        UserProfileConfig userProfileConfig = user.getUserProfileConfig();

        if(userProfileConfig == null) {
            return DataResult.failure(
                    UserErrorCode.USER_PROFILE_CONFIG_NOT_FOUND,
                    "User profile config with user id = %d does not exits in database!".formatted(user.getId())
            );
        }

        UserProfileConfigResponse response = UserMapper.fromEntityToResponse(userProfileConfig);
        return DataResult.success(response);
    }

    public DataResult<UserFriendListResponse> findCurrentUserFriends() {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,"Could not find current user!");
        }

        User user = userOpt.get();

        Set<User> userFriends = user.getFriends();
        if(userFriends.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_HAS_NO_FRIENDS, "Current user has no friends!");
        }

        Set<UserFriendResponse> userFriendsResponse = userFriends
                .stream()
                .map(u -> UserMapper.fromEntityToUserFriendResponse(u, RelationshipStatus.FRIEND))
                .collect(toSet());

        UserFriendListResponse response = new UserFriendListResponse(userFriendsResponse);
        return DataResult.success(response);
    }

    public DataResult<Page<UserFriendResponse>> findCurrentUserFriends(int page, int size) {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }

        User currentUser = currentUserOpt.get();

        List<User> userFriends = userRepository.findUserFriends(currentUser.getId());
        if(userFriends.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_HAS_NO_FRIENDS, "Current user has no friends!");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userFriendsPage = PageUtils.createPageImpl(userFriends, pageable);
        Page<UserFriendResponse> response = userFriendsPage.map(u -> UserMapper.fromEntityToUserFriendResponse(u, RelationshipStatus.FRIEND));
        return DataResult.success(response);
    }

    public DataResult<Page<UserFriendResponse>> findUserFriends(Long userId, int page, int size) {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        Optional<User> userToCheckOpt = userRepository.findById(userId);
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }
        if(userToCheckOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId));
        }
        User currentUser = currentUserOpt.get();
        User userToCheck = userToCheckOpt.get();

        UserPermissionCheckResult permissionResult = permissionChecker.checkUserProfileResourceAccess(currentUser, userToCheck);
        if(!permissionResult.allowed()) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_ACCESS_NOT_ALLOWED,
                    permissionResult.notAllowedErrorMessage());
        }

        List<User> userToCheckFriends = userRepository.findUserFriends(userId);
        if(userToCheckFriends.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_HAS_NO_FRIENDS,
                    "User to check with id = %d does not have friends!".formatted(userId));
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userToCheckFriendsPage = PageUtils.createPageImpl(userToCheckFriends, pageable);

        Page<UserFriendResponse> response = userToCheckFriendsPage.map(u -> {
            RelationshipStatus status = UserUtils.getRelationshipStatusFromUser(currentUser, u);
            return UserMapper.fromEntityToUserFriendResponse(u, status);
        });
        return DataResult.success(response);
    }

    public DataResult<Set<UserHeaderResponse>> findUsers(final String containsString, Integer size) {
        Optional<User> currentUserOpt = AuthUtils.getCurrentUser();
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }
        User currentUser = currentUserOpt.get();

        List<User> userEntities = userRepository.findUserHeaders();
        if(userEntities.isEmpty()) {
            return DataResult.failure(UserErrorCode.USERS_NOT_FOUND,
                    "There are no users in database");
        }

        List<User> userEntitiesWithProfiles = userEntities
                .stream()
                .filter(u -> u.getUserProfile() != null)
                .toList();

        if(userEntitiesWithProfiles.isEmpty()) {
            return DataResult.failure(UserErrorCode.USERS_NOT_FOUND,
                    "There are no users in database");
        }

        Pattern pattern = Pattern.compile("^" + containsString + ".*$", Pattern.CASE_INSENSITIVE);

        List<User> userEntitiesThatContainsFirstNameString = userEntitiesWithProfiles
                .stream()
                .filter(u -> matchFirstName(pattern, u))
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
                    .filter(u -> matchLastName(pattern, u))
                    .toList();

            for(int i = userEntitiesResponse.size(); i < size && i < userEntitiesThatContainsLastNameString.size(); i++) {
                userEntitiesResponse.add(userEntitiesThatContainsLastNameString.get(i));
            }
        }

        Set<UserHeaderResponse> response = userEntitiesResponse
                .stream()
                .map(u -> UserMapper.fromEntityToUserHeaderResponse(u, UserUtils.getRelationshipStatusFromUser(currentUser, u)))
                .collect(toSet());


        if(response.isEmpty()) {
            return DataResult.failure(UserErrorCode.SEARCH_USERS_NOT_FOUND,
                    "There are no users with these parameters");
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
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,"Could not find current user!");
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

    public DataResult<UserProfileResponse> updateCurrentUserProfile(UserProfileRequest request, MultipartFile profilePicture) {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }
        User user = userOpt.get();

        UserProfile userProfile = user.getUserProfile();
        if(userProfile == null) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_NOT_FOUND, "Current user has no profile!");
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
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }

        User user = userOpt.get();
        UserProfileConfig userProfileConfig = user.getUserProfileConfig();

        if(userProfileConfig == null) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_CONFIG_NOT_FOUND, "Current user has no profile config!");
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
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "Could not find current user!");
        }

        if(friendOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND, "User friend with id=%d not found".formatted(friendId));
        }

        User user = userOpt.get();
        User friend = friendOpt.get();

        user.removeFriend(friend);
        userRepository.save(user);
        userRepository.save(friend);
        return DataResult.success("Successfully removed user with id=%d from friend list".formatted(friendId));
    }

}
