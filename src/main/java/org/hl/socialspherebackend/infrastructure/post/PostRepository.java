package org.hl.socialspherebackend.infrastructure.post;

import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "select * from dbo.post where user_id = :userId", nativeQuery = true)
    Page<Post> findPostsByUserId(Pageable pageable, Long userId);

    @Query(value = "select case when count(*) > 0 then 1 else 0 end as result from dbo.post_liked_by where post_id = :postId and user_id = :userId", nativeQuery = true)
    int existsPostLikedBy(Long postId, Long userId);

    Page<Post> findByUser(Pageable pageable, User user);

    @Query(value = """
            select p.id, p.user_id, p.content, p.like_count, p.comment_count, p.created_at, p.updated_at
            from dbo.post as p 
            join dbo.user_friend_list as ufl 
            on p.user_id = ufl.inverse_friend_id and ufl.friend_id = :userId 
            join dbo.user_profile_config as upc
            on ufl.inverse_friend_id = upc.user_id
            where upc.profile_privacy_level in ('FRIENDS', 'PUBLIC')
            """, nativeQuery = true)
    Page<Post> findRecentPostsAvailableForUser(Pageable pageable, Long userId);

}
