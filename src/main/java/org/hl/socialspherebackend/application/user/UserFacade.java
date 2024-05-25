package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.user.request.UserProfileConfigRequest;
import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
import org.hl.socialspherebackend.api.dto.user.response.*;
import org.hl.socialspherebackend.api.entity.user.*;
import org.hl.socialspherebackend.application.user.mapper.UserFriendRequestMapper;
import org.hl.socialspherebackend.application.user.mapper.UserMapper;
import org.hl.socialspherebackend.application.user.mapper.UserProfileConfigMapper;
import org.hl.socialspherebackend.application.user.mapper.UserProfileMapper;
import org.hl.socialspherebackend.infrastructure.user.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserFacade implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserFacade.class);

    private final UserRepository userRepository;
    private final UserProfileConfigRepository userProfileConfigRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserFriendRequestRepository userFriendRequestRepository;
    private final AuthorityRepository authorityRepository;


    public UserFacade(UserRepository userRepository,
                      UserProfileConfigRepository userProfileConfigRepository,
                      UserProfileRepository userProfileRepository,
                      UserFriendRequestRepository userFriendRequestRepository,
                      AuthorityRepository authorityRepository) {

        this.userRepository = userRepository;
        this.userProfileConfigRepository = userProfileConfigRepository;
        this.userProfileRepository = userProfileRepository;
        this.userFriendRequestRepository = userFriendRequestRepository;
        this.authorityRepository = authorityRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isPresent()) {
            log.info("load user: {}", user.get());
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

        UserFriendRequestId id = new UserFriendRequestId(senderId, receiverId);
        UserFriendRequest userFriendRequest = new UserFriendRequest();
        userFriendRequest.setId(id);
        userFriendRequest.setSender(sender);
        userFriendRequest.setReceiver(receiver);
        userFriendRequest.setStatus(UserFriendRequestStatus.WAITING_FOR_RESPONSE);
        userFriendRequestRepository.save(userFriendRequest);

        UserFriendRequestResponse response = UserFriendRequestMapper.fromEntityToResponse(userFriendRequest);
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
        if(!existsUserById(senderId)) {
            return UserFriendRequestResult.failure(
                    UserFriendRequestResult.Code.SENDER_NOT_FOUND,
                    "sender with id = %d does not exits in database!".formatted(senderId)
            );
        }
        if(!existsUserById(receiverId)) {
            return UserFriendRequestResult.failure(
                    UserFriendRequestResult.Code.RECEIVER_NOT_FOUND,
                    "receiver with id = %d does not exits in database!".formatted(receiverId)
            );
        }

        UserFriendRequestId id = new UserFriendRequestId(receiverId, senderId);
        Optional<UserFriendRequest> userFriendRequestOpt = userFriendRequestRepository.findById(id);
        if(userFriendRequestOpt.isEmpty()) {
            return UserFriendRequestResult.failure(
                    UserFriendRequestResult.Code.FRIEND_REQUEST_NOT_FOUND,
                    "Friend request does not exits in database!"
            );
        }

        UserFriendRequest userFriendRequest = userFriendRequestOpt.get();
        userFriendRequest.setStatus(status);
        userFriendRequestRepository.save(userFriendRequest);

        UserFriendRequestResponse response = UserFriendRequestMapper.fromEntityToResponse(userFriendRequest);
        return UserFriendRequestResult.success(response, UserFriendRequestResult.Code.REPLIED);
    }

    public UserProfileResult createUserProfile(Long userId, UserProfileRequest request) {
        Optional<User> userOpt = findUserEntityById(userId);
        if(userOpt.isEmpty()) {
            return UserProfileResult.failure(UserProfileResult.Code.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId));
        }

        if(existsUserProfileByUserId(userId)) {
            return UserProfileResult.failure(UserProfileResult.Code.CANNOT_CREATE,
                    "User with id = %d already have profile. Use POST endpoint!");
        }

        User user = userOpt.get();
        UserProfile userProfile = UserProfileMapper.fromRequestToEntity(request);
        userProfile.setUser(user);
        user.setUserProfile(userProfile);
        userProfileRepository.save(userProfile);
        userRepository.save(user);

        UserProfileResponse response = UserProfileMapper.fromEntityToResponse(userProfile);
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

        if(existsUserProfileConfigByUserId(userId)) {
            return UserProfileConfigResult.failure(
                    UserProfileConfigResult.Code.CANNOT_CREATE,
                    "User with id = %d already have profile config. Use POST endpoint"
            );
        }

        User user = userOpt.get();
        UserProfileConfig userProfileConfig = UserProfileConfigMapper.fromRequestToEntity(request);
        user.setUserProfileConfig(userProfileConfig);
        userProfileConfig.setUser(user);

        userRepository.save(user);
        userProfileConfigRepository.save(userProfileConfig);

        UserProfileConfigResponse response = UserProfileConfigMapper.fromEntityToResponse(userProfileConfig);
        return UserProfileConfigResult.success(response, UserProfileConfigResult.Code.CREATED);
    }


    public UserResult findUserById(Long userId) {
        Optional<User> userOpt = findUserEntityById(userId);
        if(userOpt.isEmpty()) {
            return UserResult.failure(UserResult.Code.NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId));
        }

        Optional<UserProfile> userProfileOpt = findUserProfileEntityByUserId(userId);
        UserProfileResponse userProfileResponse = null;
        if(userProfileOpt.isPresent()) {
            userProfileResponse = UserProfileMapper.fromEntityToResponse(userProfileOpt.get());
        }

        Optional<UserProfileConfig> userProfileConfigOpt = findUserProfileConfigEntityByUserId(userId);
        UserProfileConfigResponse userProfileConfigResponse = null;
        if(userProfileConfigOpt.isPresent()) {
            userProfileConfigResponse = UserProfileConfigMapper.fromEntityToResponse(userProfileConfigOpt.get());
        }

        UserResponse userResponse = UserMapper.fromEntityToResponse(userOpt.get());

        return UserResult.success(userResponse,
                userProfileResponse,
                userProfileConfigResponse,
                UserResult.Code.FOUND);
    }

    public UserProfileResult findUserProfileByUserId(Long userId) {
        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUserId(userId);
        if(userProfileOpt.isEmpty()) {
            return UserProfileResult.failure(
                    UserProfileResult.Code.USER_PROFILE_NOT_FOUND,
                    "User profile with user id = %d does not exists in database!".formatted(userId)
            );
        }

        UserProfileResponse response = UserProfileMapper.fromEntityToResponse(userProfileOpt.get());
        return UserProfileResult.success(response, UserProfileResult.Code.FOUND);
    }

    public UserProfileConfigResult findUserProfileConfigByUserId(Long userId) {
        Optional<UserProfileConfig> userProfileConfigOpt = userProfileConfigRepository.findByUserId(userId);
        if(userProfileConfigOpt.isEmpty()) {
            return UserProfileConfigResult.failure(
                    UserProfileConfigResult.Code.USER_PROFILE_CONFIG_NOT_FOUND,
                    "User profile config with user id = %d does not exits in database!".formatted(userId)
            );
        }

        UserProfileConfigResponse response = UserProfileConfigMapper.fromEntityToResponse(userProfileConfigOpt.get());
        return UserProfileConfigResult.success(response, UserProfileConfigResult.Code.FOUND);
    }

    public UserFriendListResult findUserFriends(Long userId) {
        if(!existsUserById(userId)) {
            return UserFriendListResult.failure(
                    UserFriendListResult.Code.USER_NOT_FOUND,
                    "User with id = %d does not exits in database!".formatted(userId)
            );
        }

        Optional<List<User>> userFriendsOpt = userRepository.findUserFriends(userId);
        if(userFriendsOpt.isEmpty() || userFriendsOpt.get().isEmpty()) {
            return UserFriendListResult.failure(
                    UserFriendListResult.Code.NOT_FOUND,
                    "User with id = %d does not have friends!".formatted(userId)
            );
        }

        List<User> userFriends = userFriendsOpt.get();
        List<UserFriendResponse> userFriendResponses = new ArrayList<>();
        userFriends.forEach(u -> {
            UserResponse user = UserMapper.fromEntityToResponse(u);
            UserProfileResponse userProfile = UserProfileMapper.fromEntityToResponse(u.getUserProfile());
            UserProfileConfigResponse userProfileConfig = UserProfileConfigMapper.fromEntityToResponse(u.getUserProfileConfig());
            userFriendResponses.add(new UserFriendResponse(user, userProfile, userProfileConfig));
        });


        UserFriendListResponse response = new UserFriendListResponse(userFriendResponses);
        return UserFriendListResult.success(response, UserFriendListResult.Code.FOUND);
    }

    public Page<UserFriendResponse> findUserFriends(Long userId, int page, int size) {
        if(!existsUserById(userId)) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findUserFriends(userId, pageable);

        Page<UserFriendResponse> response = userPage.map(u -> {
            UserResponse user = UserMapper.fromEntityToResponse(u);
            UserProfileResponse userProfile = UserProfileMapper.fromEntityToResponse(u.getUserProfile());
            UserProfileConfigResponse userProfileConfig = UserProfileConfigMapper.fromEntityToResponse(u.getUserProfileConfig());
            return new UserFriendResponse(user, userProfile, userProfileConfig);
        });

        return response;
    }

    public UserProfileResult updateUserProfile(Long userId, UserProfileRequest request) {
        if(!existsUserById(userId)) {
            return UserProfileResult.failure(
                    UserProfileResult.Code.USER_NOT_FOUND,
                    "User id = %d doesn't exits in database".formatted(userId)
            );
        }

        Optional<UserProfile> userProfileOpt = userProfileRepository.findByUserId(userId);
        if(userProfileOpt.isEmpty()) {
            return UserProfileResult.failure(
                    UserProfileResult.Code.USER_PROFILE_NOT_FOUND,
                    "User Profile with user id = %d doesn't exits in database".formatted(userId)
            );
        }

        UserProfile userProfile = userProfileOpt.get();
        updateUserProfileEntity(userProfile, request);
        userProfileRepository.save(userProfile);

        UserProfileResponse response = UserProfileMapper.fromEntityToResponse(userProfile);
        return UserProfileResult.success(response, UserProfileResult.Code.UPDATED);
    }

    private void updateUserProfileEntity(UserProfile entity, UserProfileRequest request) {
        entity.setFirstName(request.firstName());
        entity.setLastName(request.lastName());
        entity.setCity(request.city());
        entity.setCountry(request.country());
    }

    public UserProfileConfigResult updateUserProfileConfig(Long userId, UserProfileConfigRequest request) {
        if(!existsUserById(userId)) {
            return UserProfileConfigResult.failure(
                    UserProfileConfigResult.Code.USER_NOT_FOUND,
                    "User id = %d doesn't exits in database".formatted(userId)
            );
        }

        Optional<UserProfileConfig> userProfileConfigOpt = userProfileConfigRepository.findByUserId(userId);
        if(userProfileConfigOpt.isEmpty()) {
            return UserProfileConfigResult.failure(
                    UserProfileConfigResult.Code.USER_PROFILE_CONFIG_NOT_FOUND,
                    "User Profile Config with user id = %d doesn't exits in database".formatted(userId)
            );
        }

        UserProfileConfig userProfileConfig = userProfileConfigOpt.get();
        updateUserProfileConfigEntity(userProfileConfig, request);
        userProfileConfigRepository.save(userProfileConfig);

        UserProfileConfigResponse response = UserProfileConfigMapper.fromEntityToResponse(userProfileConfig);
        return UserProfileConfigResult.success(response, UserProfileConfigResult.Code.UPDATED);
    }

    private void updateUserProfileConfigEntity(UserProfileConfig entity, UserProfileConfigRequest request) {
        entity.setUserProfilePrivacyLevel(request.userProfilePrivacyLevel());
    }


    public Optional<User> findUserEntityById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<UserProfile> findUserProfileEntityByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId);
    }

    public Optional<UserProfileConfig> findUserProfileConfigEntityByUserId(Long userId) {
        return userProfileConfigRepository.findByUserId(userId);
    }

    public boolean existsUserById(Long userId) {
        return userRepository.existsById(userId);
    }

    public boolean existsUserByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsUserProfileByUserId(Long userId) {
        return userProfileRepository.existsByUserId(userId);
    }

    public boolean existsUserProfileConfigByUserId(Long userId) {
        return userProfileConfigRepository.existsByUserId(userId);
    }

    public boolean areUsersFriends(Long userId, Long friendId) {
        return userRepository.areUsersFriends(userId, friendId);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Authority saveAuthority(Authority authority) {
        return authorityRepository.save(authority);
    }


}
