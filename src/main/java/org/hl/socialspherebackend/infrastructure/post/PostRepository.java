package org.hl.socialspherebackend.infrastructure.post;

import org.hl.socialspherebackend.api.entity.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {


    @Query(value = "select * from dbo.post where user_id = :userId", nativeQuery = true)
    Page<Post> findPostsByUserId(Pageable pageable, Long userId);

    @Query(value = "select * from dbo.post_liked_by where post_id = :postId and user_id = :userId", nativeQuery = true)
    Boolean existsPostLikedBy(Long postId, Long userId);


    @Query(value = "insert into dbo.post_liked_by(post_id, user_id) values (:postId, :userId)", nativeQuery = true)
    void savePostLikedBy(Long postId, Long userId);

    @Query(value = "delete from dbo.post_liked_by where post_id = :postId and user_id = :userId", nativeQuery = true)
    void deletePostLikedBy(Long postId, Long userId);

    @Query(value = """
            select p.id, p.user_id, p.content, p.like_count, p.comment_count, p.created_at, p.updated_at
            from dbo.post as p 
            join dbo.user_friend_list as ufl 
            on p.user_id = ufl.user_friend_id and ufl.user_id = :userId 
            join dbo.user_profile_config as upc
            on ufl.user_friend_id = upc.user_id
            where upc.profile_privacy_level in ('FRIENDS', 'PUBLIC')
            """, nativeQuery = true)
    Page<Post> findRecentPostsAvailableForUser(Pageable pageable, Long userId);

}