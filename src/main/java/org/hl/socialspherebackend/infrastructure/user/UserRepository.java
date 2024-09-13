package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.api.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    @Query(value = "select * from dbo.users where username = :username", nativeQuery = true)
    Optional<User> findByUsername(String username);

    @Query(value = """
        select u.*, up.*
        from dbo.users u
        left join dbo.user_profile up
        on u.id = up.user_id

    """, nativeQuery = true)
    List<User> findUserHeaders();

    @Query(value = """
           select u.*, up.*, upc.*
           from users u
           left join user_profile up
           on u.id = up.user_id
           left join user_profile_config upc
           on u.id = upc.user_id
           where u.id IN
           (select inverse_friend_id as friend_id
           from user_friend_list
           where friend_id = :userId
           union
           select friend_id as friend_id
           from user_friend_list
           where inverse_friend_id = :userId)
        """, nativeQuery = true)
    List<User> findUserFriends(Long userId);


    @Query(value = """
        select u.*
        from users u
        join chat_room chr
        on chr.user_id = u.id
        where chr.chat_id = :chatId and chr.user_id <> :firstUserId
    """, nativeQuery = true)
    Optional<User> findSecondUserInChat(Long chatId, Long firstUserId);

}
