package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.user.request.UserProfileConfigRequest;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
import org.hl.socialspherebackend.api.dto.user.response.*;
import org.hl.socialspherebackend.api.entity.user.*;
import org.hl.socialspherebackend.application.util.FileUtils;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toSet;

public class UserFacade implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserFacade.class);

    private final UserRepository userRepository;

    public UserFacade(UserRepository userRepository) {
        this.userRepository = userRepository;

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isPresent()) {
            log.debug("Load user: {}", user.get());
            return user.get();
        }
        log.debug("There is no user with username: \"{}\" in database!", username);
        throw new UsernameNotFoundException("Invalid credentials");
    }

    public UserFriendRequestResult sendFriendRequest(Long senderId, Long receiverId) {
        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = userRepository.findById(receiverId);
        if(senderOpt.isEmpty()) {
            return UserFriendRequestResult.failure(
                    UserFriendRequestResult.Code.SENDER_NOT_FOUND,
                    "sender with id = %d does not exits in database!".formatted(senderId)
            );
        }
        if(receiverOpt.isEmpty()) {
            return UserFriendRequestResult.failure(
                    UserFriendRequestResult.Code.RECEIVER_NOT_FOUND,
                    "receiver with id = %d does not exits in database!".formatted(receiverId)
            );
        }
        User sender = senderOpt.get();
        User receiver = receiverOpt.get();
        UserFriendRequest userFriendRequest = new UserFriendRequest(sender, receiver, UserFriendRequestStatus.WAITING_FOR_RESPONSE);

        sender.appendSentFriendRequest(userFriendRequest);
        sender.appendReceivedFriendRequest(userFriendRequest);
        receiver.appendSentFriendRequest(userFriendRequest);
        receiver.appendReceivedFriendRequest(userFriendRequest);
        userRepository.save(sender);
        userRepository.save(receiver);

        UserFriendRequestResponse response = UserMapper.fromUserFriendRequestEntityToResponse(userFriendRequest);
        return UserFriendRequestResult.success(response, UserFriendRequestResult.Code.SENT);
    }

    public UserFriendRequestResult acceptFriendRequest(Long senderId, Long receiverId) {
      return responseToFriendRequest(senderId, receiverId, UserFriendRequestStatus.ACCEPTED);
    }


    public UserFriendRequestResult rejectFriendRequest(Long senderId, Long receiverId) {
        return responseToFriendRequest(senderId, receiverId, UserFriendRequestStatus.REJECTED);
    }

    private UserFriendRequestResult responseToFriendRequest(Long senderId,
                                                            Long receiverId,
                                                            UserFriendRequestStatus status) {

        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = userRepository.findById(receiverId);

        if(senderOpt.isEmpty()) {
            return UserFriendRequestResult.failure(
                    UserFriendRequestResult.Code.SENDER_NOT_FOUND,
                    "sender with id = %d does not exits in database!".formatted(senderId)
            );
        }
        if(receiverOpt.isEmpty()) {
            return UserFriendRequestResult.failure(
                    UserFriendRequestResult.Code.RECEIVER_NOT_FOUND,
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
                    UserFriendRequestResult.Code.FRIEND_REQUEST_NOT_FOUND,
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

        UserFriendRequestResponse response = UserMapper.fromUserFriendRequestEntityToResponse(friendRequest);
        return UserFriendRequestResult.success(response, UserFriendRequestResult.Code.REPLIED);
    }

    public UserProfileResult createUserProfile(Long userId, UserProfileRequest request, MultipartFile profilePicture) {
        Optional<User> userOpt = findUserEntityById(userId);
        if(userOpt.isEmpty()) {
            return UserProfileResult.failure(UserProfileResult.Code.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId));
        }

        User user = userOpt.get();

        if(user.getUserProfile() != null) {
            return UserProfileResult.failure(UserProfileResult.Code.CANNOT_CREATE,
                    "User with id = %d already have profile. Use POST endpoint!");
        }

        UserProfile userProfile = UserMapper.fromRequestToUserProfileEntity(request, user);

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

        UserProfileResponse response = UserMapper.fromUserProfileEntityToResponse(userProfile, imgResponse);
        return UserProfileResult.success(response, UserProfileResult.Code.CREATED);
    }


    public UserProfileConfigResult createUserProfileConfig(Long userId, UserProfileConfigRequest request) {
        Optional<User> userOpt = findUserEntityById(userId);
        if(userOpt.isEmpty()) {
            return UserProfileConfigResult.failure(
                    UserProfileConfigResult.Code.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId)
            );
        }

        User user = userOpt.get();

        if(user.getUserProfileConfig() != null) {
            return UserProfileConfigResult.failure(
                    UserProfileConfigResult.Code.CANNOT_CREATE,
                    "User with id = %d already have profile config. Use POST endpoint"
            );
        }

        UserProfileConfig userProfileConfig = UserMapper.fromRequestToUserProfileConfigEntity(request, user);
        user.setUserProfileConfig(userProfileConfig);

        userRepository.save(user);

        UserProfileConfigResponse response = UserMapper.fromUserProfileConfigEntityToResponse(userProfileConfig);
        return UserProfileConfigResult.success(response, UserProfileConfigResult.Code.CREATED);
    }


    public UserResult findUserById(Long userId) {
        Optional<User> userOpt = findUserEntityById(userId);
        if(userOpt.isEmpty()) {
            return UserResult.failure(UserResult.Code.NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId));
        }

        User user = userOpt.get();

        UserProfile userProfile = user.getUserProfile();
        UserProfilePicture userProfilePicture = userProfile.getProfilePicture();
        UserProfileResponse userProfileResponse;
        byte[] profilePic = null;
        if(userProfilePicture != null) {
            profilePic = FileUtils.decompressFile(userProfilePicture.getImage());
        }
        userProfileResponse = UserMapper.fromUserProfileEntityToResponse(userProfile, profilePic);

        UserProfileConfig userProfileConfig = user.getUserProfileConfig();
        UserProfileConfigResponse userProfileConfigResponse = UserMapper.fromUserProfileConfigEntityToResponse(userProfileConfig);

        UserResponse userResponse = UserMapper.fromUserEntityToResponse(user);

        return UserResult.success(userResponse,
                userProfileResponse,
                userProfileConfigResponse,
                UserResult.Code.FOUND);
    }

    public UserProfileResult findUserProfileByUserId(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return UserProfileResult.failure(
                    UserProfileResult.Code.USER_NOT_FOUND,
                    "User with id = %d does not exists in database!".formatted(userId)
            );
        }
        User user = userOpt.get();
        UserProfile userProfile = user.getUserProfile();

        if(userProfile == null) {
            return UserProfileResult.failure(
                    UserProfileResult.Code.USER_PROFILE_NOT_FOUND,
                    "User with id = %d does not have profile in database!".formatted(userId)
            );
        }

        byte[] profilePicture = userProfile.getProfilePicture().getImage();
        if(profilePicture != null) {
            profilePicture = FileUtils.decompressFile(profilePicture);
        }

        UserProfileResponse response = UserMapper.fromUserProfileEntityToResponse(userProfile, profilePicture);
        return UserProfileResult.success(response, UserProfileResult.Code.FOUND);
    }

    public UserProfileConfigResult findUserProfileConfigByUserId(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return UserProfileConfigResult.failure(
              UserProfileConfigResult.Code.USER_NOT_FOUND,
              "User with id = %d does not exits in database".formatted(userId)
            );
        }

        User user = userOpt.get();
        UserProfileConfig userProfileConfig = user.getUserProfileConfig();

        if(userProfileConfig == null) {
            return UserProfileConfigResult.failure(
                    UserProfileConfigResult.Code.USER_PROFILE_CONFIG_NOT_FOUND,
                    "User profile config with user id = %d does not exits in database!".formatted(userId)
            );
        }

        UserProfileConfigResponse response = UserMapper.fromUserProfileConfigEntityToResponse(userProfileConfig);
        return UserProfileConfigResult.success(response, UserProfileConfigResult.Code.FOUND);
    }

    public UserFriendListResult findUserFriends(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return UserFriendListResult.failure(
                    UserFriendListResult.Code.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId)
            );
        }

        User user = userOpt.get();

        Set<User> userFriends = user.getUserFriendList();
        if(userFriends.isEmpty()) {
            return UserFriendListResult.failure(
                    UserFriendListResult.Code.NOT_FOUND,
                    "User with id = %d does not have friends!".formatted(userId)
            );
        }

        Set<UserFriendResponse> userFriendsResponse = userFriends
                .stream()
                .map(UserMapper::fromUserEntityToUserFriendResponse)
                .collect(toSet());

        UserFriendSetResponse response = new UserFriendSetResponse(userFriendsResponse);
        return UserFriendListResult.success(response, UserFriendListResult.Code.FOUND);
    }

    public Page<UserFriendResponse> findUserFriends(Long userId, int page, int size) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return Page.empty();
        }
        User user = userOpt.get();
        List<User> userFriends = List.copyOf(user.getUserFriendList());
        if(userFriends.isEmpty()) {
            return Page.empty();
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = new PageImpl<>(userFriends, pageable, userFriends.size());

        return userPage.map(UserMapper::fromUserEntityToUserFriendResponse);
    }

    public SearchUsersResult findUsers(Long userId, final String containsString, Integer maxSize) {
        if(!userRepository.existsById(userId)) {
            return SearchUsersResult.failure(SearchUsersResult.Code.USER_DOES_NOT_EXITS,
                    "User with id = %d does not exits in database".formatted(userId));
        }

        List<User> userEntities = userRepository.findUserHeaders();
        if(userEntities.isEmpty()) {
            return SearchUsersResult.failure(SearchUsersResult.Code.USERS_NOT_FOUND,
                    "There are no users in database");
        }

        List<User> userEntitiesWithProfiles = userEntities
                .stream()
                .filter(u -> u.getUserProfile() != null)
                .toList();

        if(userEntitiesWithProfiles.isEmpty()) {
            return SearchUsersResult.failure(SearchUsersResult.Code.USERS_NOT_FOUND,
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
                .map(UserMapper::fromUserEntityToSearchUsersResponse)
                .toList();


        if(response.isEmpty()) {
            return SearchUsersResult.failure(SearchUsersResult.Code.NOT_FOUND,
                    "There are no users with these parameters");
        }

        return SearchUsersResult.success(response, SearchUsersResult.Code.FOUND);
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
                    UserProfileResult.Code.USER_NOT_FOUND,
                    "User id = %d doesn't exits in database".formatted(userId)
            );
        }
        User user = userOpt.get();

        UserProfile userProfile = user.getUserProfile();
        if(userProfile == null) {
            return UserProfileResult.failure(
                    UserProfileResult.Code.USER_PROFILE_NOT_FOUND,
                    "User Profile with user id = %d doesn't exits in database".formatted(userId)
            );
        }

        updateUserProfileEntity(userProfile, request, profilePicture);
        userRepository.save(user);

        UserProfilePicture userProfilePicture = userProfile.getProfilePicture();
        byte[] profilePictureResponse = null;
        if(userProfilePicture != null) {
            profilePictureResponse = FileUtils.decompressFile(userProfilePicture.getImage());
        }

        UserProfileResponse response = UserMapper.fromUserProfileEntityToResponse(userProfile, profilePictureResponse);
        return UserProfileResult.success(response, UserProfileResult.Code.UPDATED);
    }

    private void updateUserProfileEntity(UserProfile entity, UserProfileRequest request, MultipartFile profilePicture) {
        entity.setFirstName(request.firstName());
        entity.setLastName(request.lastName());
        entity.setCity(request.city());
        entity.setCountry(request.country());

        if(profilePicture != null) {
            try {
                String imgType = profilePicture.getOriginalFilename();
                byte[] compressedImg = FileUtils.compressFile(profilePicture.getBytes());
                UserProfilePicture pic = new UserProfilePicture(imgType, compressedImg);
                entity.setProfilePicture(pic);
            } catch (IOException exc) {
                log.error("Error occurred with decompressing file - {}", profilePicture);
            }

        }

    }

    public UserProfileConfigResult updateUserProfileConfig(Long userId, UserProfileConfigRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if(userOpt.isEmpty()) {
            return UserProfileConfigResult.failure(
                    UserProfileConfigResult.Code.USER_NOT_FOUND,
                    "User id = %d doesn't exits in database".formatted(userId)
            );
        }

        User user = userOpt.get();
        UserProfileConfig userProfileConfig = user.getUserProfileConfig();

        if(userProfileConfig == null) {
            return UserProfileConfigResult.failure(
                    UserProfileConfigResult.Code.USER_PROFILE_CONFIG_NOT_FOUND,
                    "User Profile Config with user id = %d doesn't exits in database".formatted(userId)
            );
        }

        updateUserProfileConfigEntity(userProfileConfig, request);
        user.setUserProfileConfig(userProfileConfig);
        userRepository.save(user);

        UserProfileConfigResponse response = UserMapper.fromUserProfileConfigEntityToResponse(userProfileConfig);
        return UserProfileConfigResult.success(response, UserProfileConfigResult.Code.UPDATED);
    }

    private void updateUserProfileConfigEntity(UserProfileConfig entity, UserProfileConfigRequest request) {
        entity.setUserProfilePrivacyLevel(request.userProfilePrivacyLevel());
    }


    public Optional<User> findUserEntityById(Long userId) {
        return userRepository.findById(userId);
    }

    public boolean existsUserById(Long userId) {
        return userRepository.existsById(userId);
    }

    public boolean existsUserByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean areUsersFriends(Long userId, Long friendId) {
        return userRepository.areUsersFriends(userId, friendId);
    }

    public User saveUserEntity(User user) {
        return userRepository.save(user);
    }

    public List<User> findAllUserEntities() {
        return userRepository.findAll();
    }

    public Long countUserEntities() {
        return userRepository.count();
    }

}
