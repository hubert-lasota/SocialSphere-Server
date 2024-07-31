package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.notification.request.UserFriendRequestDto;
import org.hl.socialspherebackend.api.dto.notification.response.NotificationErrorCode;
import org.hl.socialspherebackend.api.dto.notification.response.UserFriendRequestResponse;
import org.hl.socialspherebackend.api.dto.notification.response.UserFriendRequestResult;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileConfigRequest;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
import org.hl.socialspherebackend.api.dto.user.response.*;
import org.hl.socialspherebackend.api.entity.notification.UserFriendRequest;
import org.hl.socialspherebackend.api.entity.notification.UserFriendRequestStatus;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.api.entity.user.UserProfile;
import org.hl.socialspherebackend.api.entity.user.UserProfileConfig;
import org.hl.socialspherebackend.api.entity.user.UserProfilePicture;
import org.hl.socialspherebackend.application.util.FileUtils;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final UserValidator userValidator;

    public UserFacade(UserRepository userRepository, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
    }


    public UserFriendRequestResult sendFriendRequest(UserFriendRequestDto request) {
        Optional<User> senderOpt = userRepository.findById(request.senderId());
        Optional<User> receiverOpt = userRepository.findById(request.receiverId());
        if(senderOpt.isEmpty()) {
            return UserFriendRequestResult.failure(
                    NotificationErrorCode.SENDER_NOT_FOUND,
                    "sender with id = %d does not exits in database!".formatted(request.senderId())
            );
        }
        if(receiverOpt.isEmpty()) {
            return UserFriendRequestResult.failure(
                    NotificationErrorCode.RECEIVER_NOT_FOUND,
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
            return UserFriendRequestResult.failure(NotificationErrorCode.SENDER_ALREADY_SENT_FRIEND_REQUEST,
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
        return UserFriendRequestResult.success(response);
    }

    public UserFriendRequestResult acceptFriendRequest(UserFriendRequestDto request) {
      return responseToFriendRequest(request.senderId(), request.receiverId(), UserFriendRequestStatus.ACCEPTED);
    }


    public UserFriendRequestResult rejectFriendRequest(UserFriendRequestDto request) {
        return responseToFriendRequest(request.senderId(), request.receiverId(), UserFriendRequestStatus.REJECTED);
    }

    private UserFriendRequestResult responseToFriendRequest(Long senderId,
                                                            Long receiverId,
                                                            UserFriendRequestStatus status) {

        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = userRepository.findById(receiverId);

        if(senderOpt.isEmpty()) {
            return UserFriendRequestResult.failure(
                    NotificationErrorCode.SENDER_NOT_FOUND,
                    "sender with id = %d does not exits in database!".formatted(senderId)
            );
        }
        if(receiverOpt.isEmpty()) {
            return UserFriendRequestResult.failure(
                    NotificationErrorCode.RECEIVER_NOT_FOUND,
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
            return UserFriendRequestResult.failure(
                    NotificationErrorCode.FRIEND_REQUEST_NOT_FOUND,
                    "Friend request does not exits in database!"
            );
        }

        if(friendRequests.size() > 1) {
            throw new RuntimeException("Found more than one friend request: %s".formatted(friendRequests));
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
        return UserFriendRequestResult.success(response);
    }

    public UserProfileResult createUserProfile(Long userId, UserProfileRequest request, MultipartFile profilePicture) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return UserProfileResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId));
        }

        User user = userOpt.get();

        if(user.getUserProfile() != null) {
            return UserProfileResult.failure(UserErrorCode.USER_PROFILE_ALREADY_EXISTS,
                    "User with id = %d already have profile.");
        }

        UserValidateResult validateResult = userValidator.validate(request);
        if(!validateResult.isValid()) {
            return UserProfileResult.failure(validateResult.code(), validateResult.message());
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
        return UserProfileResult.success(response);
    }


    public UserProfileConfigResult createUserProfileConfig(Long userId, UserProfileConfigRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return UserProfileConfigResult.failure(
                    UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId)
            );
        }

        User user = userOpt.get();

        if(user.getUserProfileConfig() != null) {
            return UserProfileConfigResult.failure(
                    UserErrorCode.USER_PROFILE_CONFIG_ALREADY_EXISTS,
                    "User with id = %d already have profile config. Use POST endpoint"
            );
        }

        UserProfileConfig userProfileConfig = UserMapper.fromRequestToEntity(request, user);
        user.setUserProfileConfig(userProfileConfig);

        userRepository.save(user);

        UserProfileConfigResponse response = UserMapper.fromEntityToResponse(userProfileConfig);
        return UserProfileConfigResult.success(response);
    }


    public UserResult findUserById(Long userId, Long currentUserId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return UserResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId));
        }
        User user = userOpt.get();

        RelationshipStatus relationshipStatus = null;
        if(currentUserId != null) {
            Optional<User> currentUserOpt = userRepository.findById(currentUserId);
            User currentUser = currentUserOpt.get();
            relationshipStatus = getRelationshipStatusFromUser(currentUser, user);
        }

        UserProfile userProfile = user.getUserProfile();
        UserProfileResponse userProfileResponse = UserMapper.fromEntityToResponse(userProfile);

        UserProfileConfig userProfileConfig = user.getUserProfileConfig();
        UserProfileConfigResponse userProfileConfigResponse = UserMapper.fromEntityToResponse(userProfileConfig);

        UserResponse userResponse = UserMapper.fromEntityToResponse(user, relationshipStatus);

        return UserResult.success(userResponse,
                userProfileResponse,
                userProfileConfigResponse);
    }

    public UserProfileResult findUserProfileByUserId(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return UserProfileResult.failure(
                    UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exists in database!".formatted(userId)
            );
        }
        User user = userOpt.get();
        UserProfile userProfile = user.getUserProfile();

        if(userProfile == null) {
            return UserProfileResult.failure(
                    UserErrorCode.USER_PROFILE_NOT_FOUND,
                    "User with id = %d does not have profile in database!".formatted(userId)
            );
        }

        UserProfileResponse response = UserMapper.fromEntityToResponse(userProfile);
        return UserProfileResult.success(response);
    }

    public UserProfileConfigResult findUserProfileConfigByUserId(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return UserProfileConfigResult.failure(
              UserErrorCode.USER_NOT_FOUND,
              "User with id = %d does not exits in database".formatted(userId)
            );
        }

        User user = userOpt.get();
        UserProfileConfig userProfileConfig = user.getUserProfileConfig();

        if(userProfileConfig == null) {
            return UserProfileConfigResult.failure(
                    UserErrorCode.USER_PROFILE_CONFIG_NOT_FOUND,
                    "User profile config with user id = %d does not exits in database!".formatted(userId)
            );
        }

        UserProfileConfigResponse response = UserMapper.fromEntityToResponse(userProfileConfig);
        return UserProfileConfigResult.success(response);
    }

    public UserFriendListResult findUserFriends(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return UserFriendListResult.failure(
                    UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId)
            );
        }

        User user = userOpt.get();

        Set<User> userFriends = user.getFriends();
        if(userFriends.isEmpty()) {
            return UserFriendListResult.failure(
                    UserErrorCode.USER_HAS_NO_FRIENDS,
                    "User with id = %d does not have friends!".formatted(userId)
            );
        }

        Set<UserFriendResponse> userFriendsResponse = userFriends
                .stream()
                .map(u -> UserMapper.fromEntityToUserFriendResponse(u, RelationshipStatus.FRIEND))
                .collect(toSet());

        UserFriendListResponse response = new UserFriendListResponse(userFriendsResponse);
        return UserFriendListResult.success(response);
    }

    public Page<UserFriendResponse> findUserFriends(Long userId, int page, int size) {
        if(!userRepository.existsById(userId)) {
            return Page.empty();
        }

        List<User> userFriends = userRepository.findUserFriends(userId);
        if(userFriends.isEmpty()) {
            return Page.empty();
        }

        Page<User> userFriendsPage = createPageImpl(userFriends, page, size);
        return userFriendsPage.map(u -> UserMapper.fromEntityToUserFriendResponse(u, RelationshipStatus.FRIEND));
    }

    public Page<UserFriendResponse> findCheckedUserFriends(Long currentUserId, Long userToCheckId, int page, int size) {
        Optional<User> currentUserOpt = userRepository.findById(currentUserId);
        Optional<User> userToCheckOpt = userRepository.findById(userToCheckId);
        if(currentUserOpt.isEmpty() || userToCheckOpt.isEmpty()) {
            log.debug("current user or user to check does not exists in database!");
            return Page.empty();
        }
        User currentUser = currentUserOpt.get();
        User userToCheck = userToCheckOpt.get();

        List<User> userToCheckFriends = List.copyOf(userToCheck.getFriends());
        if(userToCheckFriends.isEmpty()) {
            return Page.empty();
        }

        Page<User> userToCheckFriendsPage = createPageImpl(userToCheckFriends, page, size);

        return userToCheckFriendsPage.map(u -> {
            RelationshipStatus relationshipStatus = getRelationshipStatusFromUser(currentUser, userToCheck);
            return UserMapper.fromEntityToUserFriendResponse(u, relationshipStatus);
        });
    }

    public SearchUsersResult findUsers(Long userId, final String containsString, Integer maxSize) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return SearchUsersResult.failure(UserErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exits in database".formatted(userId));
        }
        User currentUser = userOpt.get();

        List<User> userEntities = userRepository.findUserHeaders();
        if(userEntities.isEmpty()) {
            return SearchUsersResult.failure(UserErrorCode.USERS_NOT_FOUND,
                    "There are no users in database");
        }

        List<User> userEntitiesWithProfiles = userEntities
                .stream()
                .filter(u -> u.getUserProfile() != null)
                .toList();

        if(userEntitiesWithProfiles.isEmpty()) {
            return SearchUsersResult.failure(UserErrorCode.USERS_NOT_FOUND,
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

        List<SearchUsersResponse> response = userEntitiesResponse
                .stream()
                .map(u -> UserMapper.fromEntityToSearchUsersResponse(u, getRelationshipStatusFromUser(currentUser, u)))
                .toList();


        if(response.isEmpty()) {
            return SearchUsersResult.failure(UserErrorCode.SEARCH_USERS_NOT_FOUND,
                    "There are no users with these parameters");
        }

        return SearchUsersResult.success(response);
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

    public byte[] findUserProfilePictureByUserId(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            log.debug("Cannot find user profile picture because user with id = {} does not exits in database!", userId);
            return null;
        }
        User user = userOpt.get();

        UserProfile userProfile = user.getUserProfile();
        if(userProfile == null) {
            log.debug("Cannot find user profile picture because user with id = {} does not have profile!", userId);
            return null;
        }

        UserProfilePicture userProfilePicture = userProfile.getProfilePicture();
        if(userProfilePicture == null) {
            log.debug("User with id = {} does not have profile picture", userId);
            return null;
        }

        return FileUtils.decompressFile(userProfilePicture.getImage());
    }

    public UserProfileResult updateUserProfile(Long userId, UserProfileRequest request, MultipartFile profilePicture) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return UserProfileResult.failure(
                    UserErrorCode.USER_NOT_FOUND,
                    "User id = %d doesn't exits in database".formatted(userId)
            );
        }
        User user = userOpt.get();

        UserProfile userProfile = user.getUserProfile();
        if(userProfile == null) {
            return UserProfileResult.failure(
                    UserErrorCode.USER_PROFILE_NOT_FOUND,
                    "User Profile with user id = %d doesn't exits in database".formatted(userId)
            );
        }

        UserValidateResult validateResult = userValidator.validate(request);
        if(!validateResult.isValid()) {
            return UserProfileResult.failure(validateResult.code(), validateResult.message());
        }

        updateUserProfileEntity(userProfile, request, profilePicture);
        userRepository.save(user);

        UserProfileResponse response = UserMapper.fromEntityToResponse(userProfile);
        return UserProfileResult.success(response);
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

    public UserProfileConfigResult updateUserProfileConfig(Long userId, UserProfileConfigRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return UserProfileConfigResult.failure(
                    UserErrorCode.USER_NOT_FOUND,
                    "User id = %d doesn't exits in database".formatted(userId)
            );
        }

        User user = userOpt.get();
        UserProfileConfig userProfileConfig = user.getUserProfileConfig();

        if(userProfileConfig == null) {
            return UserProfileConfigResult.failure(
                    UserErrorCode.USER_PROFILE_CONFIG_NOT_FOUND,
                    "User Profile Config with user id = %d doesn't exits in database".formatted(userId)
            );
        }

        updateUserProfileConfigEntity(userProfileConfig, request);
        user.setUserProfileConfig(userProfileConfig);
        userRepository.save(user);

        UserProfileConfigResponse response = UserMapper.fromEntityToResponse(userProfileConfig);
        return UserProfileConfigResult.success(response);
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


    private <T> Page<T> createPageImpl(List<T> list, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        List<T> subList = list.subList(start, end);
        return new PageImpl<>(subList, pageable, list.size());
    }

    private RelationshipStatus getRelationshipStatusFromUser(User currentUser, User user) {
        RelationshipStatus relationshipStatus;

        boolean isFriend = currentUser.getFriends()
                .stream()
                .anyMatch(u -> u.equals(user));

        if(user.equals(currentUser)) {
            relationshipStatus = RelationshipStatus.YOU;
        } else if(isFriend) {
            relationshipStatus = RelationshipStatus.FRIEND;
        } else {
            relationshipStatus = RelationshipStatus.STRANGER;
        }

        return relationshipStatus;
    }

}
