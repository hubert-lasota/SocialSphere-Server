package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.api.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "select * from dbo.user_friend_list where user_id = :userId and user_friend_id = :friendId", nativeQuery = true)
    Boolean areUsersFriends(Long userId, Long friendId);

    Optional<User> findByUsername(String username);

}
