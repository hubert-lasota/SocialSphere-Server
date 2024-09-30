package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.api.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    @Query(value = "select * from dbo.users where username = :username", nativeQuery = true)
    Optional<User> findByUsername(String username);

    @Query(value = """
           select u.*
           from users u
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
           where u.id in
           (select *
		   from (
			   select inverse_friend_id as friend_id
			   from user_friend_list
			   where friend_id = 1
			   union
			   select friend_id as friend_id
			   from user_friend_list
			   where inverse_friend_id = 1
		   ) friends
		   where friends.friend_id not in (
            select chbu.user_id from chat_bound_users chbu
            where chbu.user_id <> 1
            and
            chbu.chat_id in (
            select chbu.chat_id from chat_bound_users chbu
            where chbu.user_id = 1
				)
		   )
		   )
        """, nativeQuery = true)
    List<User> findUserFriendsWithNoSharedChat(Long userId);

	@Query(value = """
        select u.* from users u
        join post_liked_by pl
        on u.id = pl.user_id
        where pl.post_id = :postId
    """, nativeQuery = true)
	List<User> findUsersLikedPost(Long postId);
	@Query(value = """
		select u.* 
		from users u
		join user_friend_list ufl
		on u.id = ufl.friend_id
		where ufl.friend_id = :userId
	""", nativeQuery = true)
	Set<User> findFriendsField(Long userId);

	@Query(value = """
		select u.* 
		from users u
		join user_friend_list ufl
		on u.id = ufl.inverse_friend_id
		where ufl.inverse_friend_id = :userId
	""", nativeQuery = true)
	Set<User> findInverseFriendsField(Long userId);

	@Modifying
	@Transactional
	@Query(value = """
	delete from user_friend_list 
	where friend_id = :userIdOne and inverse_friend_id = :userIdTwo or 
	      friend_id = :userIdTwo and inverse_friend_id = :userIdOne
	""", nativeQuery = true)
	void removeFromFriends(Long userIdOne, Long userIdTwo);

}
