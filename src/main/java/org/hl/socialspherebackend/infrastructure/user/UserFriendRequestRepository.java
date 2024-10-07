package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.api.entity.user.UserFriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserFriendRequestRepository extends JpaRepository<UserFriendRequest, Long> {

    @Query(value = """
        select * 
        from user_friend_request
        where receiver_id = :userId
    """, nativeQuery = true)
    List<UserFriendRequest> findReceivedFriendRequestsByUserId(Long userId);

    @Query(value = """
        select * 
        from user_friend_request
        where sender_id = :userId
    """, nativeQuery = true)
    List<UserFriendRequest> findSentFriendRequestsByUserId(Long userId);

    @Query(value = """
        select * 
        from user_friend_request
        where sender_id = :senderId and receiver_id = :receiverId
    """, nativeQuery = true)
    Optional<UserFriendRequest> findFriendRequestBySenderIdAndReceiverId(Long senderId, Long receiverId);

    @Query(value = """
        select case when count(*) > 0 then cast(1 as bit) else cast(0 as bit) end as c
        from user_friend_request\s
        where sender_id = :senderId and receiver_id = :receiverId and status = 'WAITING_FOR_RESPONSE'
    """, nativeQuery = true)
    boolean isSenderWaitingForFriendResponse(Long senderId, Long receiverId);

}
