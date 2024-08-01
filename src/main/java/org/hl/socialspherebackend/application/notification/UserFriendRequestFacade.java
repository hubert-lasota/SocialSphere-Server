package org.hl.socialspherebackend.application.notification;

import org.hl.socialspherebackend.api.dto.notification.request.UserFriendRequestDto;
import org.hl.socialspherebackend.api.dto.notification.response.NotificationErrorCode;
import org.hl.socialspherebackend.api.dto.notification.response.UserFriendRequestResponse;
import org.hl.socialspherebackend.api.dto.notification.response.UserFriendRequestResult;
import org.hl.socialspherebackend.api.entity.notification.UserFriendRequest;
import org.hl.socialspherebackend.api.entity.notification.UserFriendRequestStatus;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.application.user.UserMapper;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class UserFriendRequestFacade {

    private static final Logger log = LoggerFactory.getLogger(UserFriendRequestFacade.class);

    private final UserRepository userRepository;

    public UserFriendRequestFacade(UserRepository userRepository) {
        this.userRepository = userRepository;
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
            log.warn("There is {} friend requests. Only one will be stored in database, rest are going to be removed",
                    friendRequests.size());
            // TODO REMOVE REST
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

}
