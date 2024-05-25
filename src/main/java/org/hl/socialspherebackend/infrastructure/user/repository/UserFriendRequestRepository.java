package org.hl.socialspherebackend.infrastructure.user.repository;

import org.hl.socialspherebackend.api.entity.user.UserFriendRequest;
import org.hl.socialspherebackend.api.entity.user.UserFriendRequestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFriendRequestRepository extends JpaRepository<UserFriendRequest, UserFriendRequestId> {

}
