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
import org.hl.socialspherebackend.application.util.FileUtils;
import org.hl.socialspherebackend.application.util.PageUtils;
import org.hl.socialspherebackend.application.validator.RequestValidator;
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

public class UserFacade {

    private static final Logger log = LoggerFactory.getLogger(UserFacade.class);

    private final UserRepository userRepository;
    private final UserProfilePermissionChecker permissionChecker;
    private final RequestValidator<UserProfileRequest, UserValidateResult> userProfileValidator;
    private final UserFriendRequestNotificationSender notificationSender;
    private final Clock clock;

    public UserFacade(UserRepository userRepository,
                      UserProfilePermissionChecker permissionChecker,
                      RequestValidator<UserProfileRequest, UserValidateResult> userProfileValidator,
                      UserFriendRequestNotificationSender notificationSender,
                      Clock clock) {
        this.userRepository = userRepository;
        this.permissionChecker = permissionChecker;
        this.userProfileValidator = userProfileValidator;
        this.notificationSender = notificationSender;
        this.clock = clock;
    }


    public DataResult<UserFriendRequestResponse, UserErrorCode> sendFriendRequest(UserFriendRequestDto request) {
        Optional<User> senderOpt = userRepository.findById(request.senderId());
        Optional<User> receiverOpt = userRepository.findById(request.receiverId());
        if(senderOpt.isEmpty()) {
            return DataResult.failure(
                    UserErrorCode.SENDER_NOT_FOUND,
                    "sender with id = %d does not exits in database!".formatted(request.senderId())
            );
        }
        if(receiverOpt.isEmpty()) {
            return DataResult.failure(
                    UserErrorCode.RECEIVER_NOT_FOUND,
                    "receiver with id = %d does not exits in database!".formatted(request.receiverId())
            );
        }
        User sender = senderOpt.get();
        User receiver = receiverOpt.get();

        boolean didSenderSentFriendRequest = sender.getSentFriendRequests()
                .stream()
                .findAny()
                .isPresent();

        if(didSenderSentFriendRequest) {
            return DataResult.failure(UserErrorCode.SENDER_ALREADY_SENT_FRIEND_REQUEST,
                    "Sender with id = %s already sent friend request!".formatted(request.senderId()));
        }

        UserFriendRequest userFriendRequest = new UserFriendRequest(sender, receiver, UserFriendRequestStatus.WAITING_FOR_RESPONSE);

        sender.appendSentFriendRequest(userFriendRequest);
        sender.appendReceivedFriendRequest(userFriendRequest);
        receiver.appendSentFriendRequest(userFriendRequest);
        receiver.appendReceivedFriendRequest(userFriendRequest);
        userRepository.save(sender);
        userRepository.save(receiver);

        UserFriendRequestResponse response = UserMapper.fromEntityToResponse(userFriendRequest);
        notificationSender.send(response);
        return DataResult.success(response);
    }

    public DataResult<UserFriendRequestResponse, UserErrorCode>  acceptFriendRequest(UserFriendRequestDto request) {
        return responseToFriendRequest(request.senderId(), request.receiverId(), UserFriendRequestStatus.ACCEPTED);
    }


    public DataResult<UserFriendRequestResponse, UserErrorCode>  rejectFriendRequest(UserFriendRequestDto request) {
        return responseToFriendRequest(request.senderId(), request.receiverId(), UserFriendRequestStatus.REJECTED);
    }

    private DataResult<UserFriendRequestResponse, UserErrorCode>  responseToFriendRequest(Long senderId,
                                                            Long receiverId,
                                                            UserFriendRequestStatus status) {

        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = userRepository.findById(receiverId);

        if(senderOpt.isEmpty()) {
            return DataResult.failure(
                    UserErrorCode.SENDER_NOT_FOUND,
                    "sender with id = %d does not exits in database!".formatted(senderId)
            );
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
            log.debug("There is {} friend requests. Only one will be stored in database, rest are going to be removed",
                    friendRequests.size());
           saveFirstSentFriendRequestAndRemoveRest(sender, friendRequests);
           saveFirstReceivedFriendRequestAndRemoveRest(receiver, friendRequests);
        }


        UserFriendRequest friendRequest = friendRequests.
                stream().
                findFirst().
                get();

        friendRequest.setStatus(status);

        sender.getSentFriendRequests().removeIf(fr -> fr.getId().equals(friendRequest.getId()));
        sender.getReceivedFriendRequests().removeIf(fr -> fr.getId().equals(friendRequest.getId()));
        receiver.getSentFriendRequests().removeIf(fr -> fr.getId().equals(friendRequest.getId()));
        receiver.getReceivedFriendRequests().removeIf(fr -> fr.getId().equals(friendRequest.getId()));

        sender.appendSentFriendRequest(friendRequest);
        sender.appendReceivedFriendRequest(friendRequest);
        receiver.appendSentFriendRequest(friendRequest);
        receiver.appendReceivedFriendRequest(friendRequest);

        userRepository.save(sender);
        userRepository.save(receiver);

        UserFriendRequestResponse response = UserMapper.fromEntityToResponse(friendRequest);
        notificationSender.send(response);
        return DataResult.success(response);
    }

    private void saveFirstSentFriendRequestAndRemoveRest(User user, Set<UserFriendRequest> friendRequests) {
        boolean isFirst = true;
        for(UserFriendRequest request : friendRequests) {
            if(isFirst) {
                isFirst = false;
                continue;
            }
            user.removeSentFriendRequest(request);
        }
    }

    private void saveFirstReceivedFriendRequestAndRemoveRest(User user, Set<UserFriendRequest> friendRequests) {
        boolean isFirst = true;
        for(UserFriendRequest request : friendRequests) {
            if(isFirst) {
                isFirst = false;
                continue;
            }
            user.removeReceivedFriendRequest(request);
        }
    }

    public DataResult<UserProfileResponse, UserErrorCode> createUserProfile(Long userId, UserProfileRequest request, MultipartFile profilePicture) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId));
        }

        User user = userOpt.get();

        if(user.getUserProfile() != null) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_ALREADY_EXISTS,
                    "User with id = %d already have profile.");
        }

        UserValidateResult validateResult = userProfileValidator.validate(request);
        if(!validateResult.isValid()) {
            return DataResult.failure(validateResult.code(), validateResult.message());
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


    public DataResult<UserProfileConfigResponse, UserErrorCode> createUserProfileConfig(Long userId, UserProfileConfigRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return DataResult.failure(
                    UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId)
            );
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


    public DataResult<UserWrapperResponse, UserErrorCode> findUserById(Long userId, Long currentUserId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId));
        }
        User user = userOpt.get();

        RelationshipStatus relationshipStatus = null;
        if(currentUserId != null) {
            Optional<User> currentUserOpt = userRepository.findById(currentUserId);
            User currentUser = currentUserOpt.get();
            relationshipStatus = UserUtils.getRelationshipStatusFromUser(currentUser, user);
        }

        UserWrapperResponse response = UserMapper.fromEntityToUserWrapperResponse(user, relationshipStatus);
        return DataResult.success(response);
    }

    public DataResult<UserProfileResponse, UserErrorCode> findUserProfileByUserId(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return DataResult.failure(
                    UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exists in database!".formatted(userId)
            );
        }
        User user = userOpt.get();
        UserProfile userProfile = user.getUserProfile();

        if(userProfile == null) {
            return DataResult.failure(
                    UserErrorCode.USER_PROFILE_NOT_FOUND,
                    "User with id = %d does not have profile in database!".formatted(userId)
            );
        }

        UserProfileResponse response = UserMapper.fromEntityToResponse(userProfile);
        return DataResult.success(response);
    }

    public DataResult<UserProfileConfigResponse, UserErrorCode> findUserProfileConfigByUserId(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return DataResult.failure(
              UserErrorCode.USER_NOT_FOUND,
              "User with id = %d does not exits in database".formatted(userId)
            );
        }

        User user = userOpt.get();
        UserProfileConfig userProfileConfig = user.getUserProfileConfig();

        if(userProfileConfig == null) {
            return DataResult.failure(
                    UserErrorCode.USER_PROFILE_CONFIG_NOT_FOUND,
                    "User profile config with user id = %d does not exits in database!".formatted(userId)
            );
        }

        UserProfileConfigResponse response = UserMapper.fromEntityToResponse(userProfileConfig);
        return DataResult.success(response);
    }

    public DataResult<UserFriendListResponse, UserErrorCode> findUserFriends(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return DataResult.failure(
                    UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId)
            );
        }

        User user = userOpt.get();

        Set<User> userFriends = user.getFriends();
        if(userFriends.isEmpty()) {
            return DataResult.failure(
                    UserErrorCode.USER_HAS_NO_FRIENDS,
                    "User with id = %d does not have friends!".formatted(userId)
            );
        }

        Set<UserFriendResponse> userFriendsResponse = userFriends
                .stream()
                .map(u -> UserMapper.fromEntityToUserFriendResponse(u, RelationshipStatus.FRIEND))
                .collect(toSet());

        UserFriendListResponse response = new UserFriendListResponse(userFriendsResponse);
        return DataResult.success(response);
    }

    public DataResult<Page<UserFriendResponse>, UserErrorCode> findUserFriends(Long userId, int page, int size) {
        if(!userRepository.existsById(userId)) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exists in database!".formatted(userId));
        }

        List<User> userFriends = userRepository.findUserFriends(userId);
        if(userFriends.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_HAS_NO_FRIENDS,
                    "User with id = %d does not have friends!".formatted(userId));
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userFriendsPage = PageUtils.createPageImpl(userFriends, pageable);
        Page<UserFriendResponse> response = userFriendsPage.map(u -> UserMapper.fromEntityToUserFriendResponse(u, RelationshipStatus.FRIEND));
        return DataResult.success(response);
    }

    public DataResult<Page<UserFriendResponse>, UserErrorCode> findCheckedUserFriends(Long currentUserId, Long userToCheckId, int page, int size) {
        Optional<User> currentUserOpt = userRepository.findById(currentUserId);
        Optional<User> userToCheckOpt = userRepository.findById(userToCheckId);
        if(currentUserOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "Current user with id = %d does not exits in database!".formatted(currentUserId));
        }
        if(userToCheckOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "User to check with id = %d does not exits in database!".formatted(userToCheckId));
        }
        User currentUser = currentUserOpt.get();
        User userToCheck = userToCheckOpt.get();

        UserPermissionCheckResult permissionResult = permissionChecker.checkUserProfileResourceAccess(currentUser, userToCheck);
        if(!permissionResult.allowed()) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_ACCESS_NOT_ALLOWED,
                    permissionResult.notAllowedErrorMessage());
        }

        List<User> userToCheckFriends = userRepository.findUserFriends(userToCheckId);
        if(userToCheckFriends.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_HAS_NO_FRIENDS,
                    "User to check with id = %d does not have friends!".formatted(userToCheckId));
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userToCheckFriendsPage = PageUtils.createPageImpl(userToCheckFriends, pageable);

        Page<UserFriendResponse> response = userToCheckFriendsPage.map(u -> {
            RelationshipStatus status = UserUtils.getRelationshipStatusFromUser(currentUser, u);
            return UserMapper.fromEntityToUserFriendResponse(u, status);
        });
        return DataResult.success(response);
    }

    public DataResult<Set<UserHeaderResponse>, UserErrorCode> findUsers(Long userId, final String containsString, Integer maxSize) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exits in database".formatted(userId));
        }
        User currentUser = userOpt.get();

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

        List<User> userEntitiesResponse = new ArrayList<>(maxSize);

        if(!userEntitiesThatContainsFirstNameString.isEmpty()) {
            for(int i = 0; i < maxSize && i < userEntitiesThatContainsFirstNameString.size(); i++) {
                userEntitiesResponse.add(userEntitiesThatContainsFirstNameString.get(i));
            }
        }

        if(userEntitiesResponse.size() < maxSize) {
            List<User> userEntitiesThatContainsLastNameString = userEntitiesWithProfiles.stream()
                    .filter(u -> checkUserInList(userEntitiesThatContainsFirstNameString, u))
                    .filter(u -> matchLastName(pattern, u))
                    .toList();

            for(int i = userEntitiesResponse.size(); i < maxSize && i < userEntitiesThatContainsLastNameString.size(); i++) {
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

    public DataResult<byte[], UserErrorCode> findUserProfilePictureByUserId(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return DataResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "Cannot find user profile picture because user with id = %d does not exits in database!".formatted(userId));
        }
        User user = userOpt.get();

        UserProfile userProfile = user.getUserProfile();
        if(userProfile == null) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_NOT_FOUND,
                    "Cannot find user profile picture because user with id = %d does not have profile!".formatted(userId));
        }

        UserProfilePicture userProfilePicture = userProfile.getProfilePicture();
        if(userProfilePicture == null) {
            return DataResult.failure(UserErrorCode.USER_PROFILE_PICTURE_NOT_FOUND,
                    "Cannot find user profile picture because user with id = %d does not have profile picture!".formatted(userId));
        }

        byte[] response = FileUtils.decompressFile(userProfilePicture.getImage());
        return DataResult.success(response);
    }

    public DataResult<UserProfileResponse, UserErrorCode> updateUserProfile(Long userId, UserProfileRequest request, MultipartFile profilePicture) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return DataResult.failure(
                    UserErrorCode.USER_NOT_FOUND,
                    "User id = %d doesn't exits in database".formatted(userId)
            );
        }
        User user = userOpt.get();

        UserProfile userProfile = user.getUserProfile();
        if(userProfile == null) {
            return DataResult.failure(
                    UserErrorCode.USER_PROFILE_NOT_FOUND,
                    "User Profile with user id = %d doesn't exits in database".formatted(userId)
            );
        }

        UserValidateResult validateResult = userProfileValidator.validate(request);
        if(!validateResult.isValid()) {
            return DataResult.failure(validateResult.code(), validateResult.message());
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

    public DataResult<UserProfileConfigResponse, UserErrorCode> updateUserProfileConfig(Long userId, UserProfileConfigRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return DataResult.failure(
                    UserErrorCode.USER_NOT_FOUND,
                    "User id = %d doesn't exits in database".formatted(userId)
            );
        }

        User user = userOpt.get();
        UserProfileConfig userProfileConfig = user.getUserProfileConfig();

        if(userProfileConfig == null) {
            return DataResult.failure(
                    UserErrorCode.USER_PROFILE_CONFIG_NOT_FOUND,
                    "User Profile Config with user id = %d doesn't exits in database".formatted(userId)
            );
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

    public boolean removeFromFriendList(Long userId, Long friendId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<User> friendOpt = userRepository.findById(friendId);
        if(userOpt.isEmpty() && friendOpt.isEmpty()) {
            log.debug("User or Friend is empty");
            return false;
        }
        User user = userOpt.get();
        User friend = friendOpt.get();

        user.removeFriend(friend);
        userRepository.save(user);
        userRepository.save(friend);
        return true;
    }

}
