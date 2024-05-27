package org.hl.socialspherebackend.infrastructure.user.repository;

import org.hl.socialspherebackend.api.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "select * from dbo.user_friend_list where user_id = :userId and user_friend_id = :friendId", nativeQuery = true)
    boolean areUsersFriends(Long userId, Long friendId);

    @Query(value = "select * from dbo.users where username = :username", nativeQuery = true)
    boolean existsByUsername(String username);

    @Query(value = "select id, username, password from dbo.users where username = :username", nativeQuery = true)
    Optional<User> findByUsername(String username);

    @Query(value = "select * from user_friend_list where user_id = :userId", nativeQuery = true)
    Optional<List<User>> findUserFriends(Long userId);

    @Query(value = "select * from user_friend_list where user_id = :userId", nativeQuery = true)
    Page<User> findUserFriends(Long userId, Pageable pageable);
}
