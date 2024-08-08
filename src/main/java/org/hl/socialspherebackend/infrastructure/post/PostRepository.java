package org.hl.socialspherebackend.infrastructure.post;

import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {


    @Query(value = """
        select case when count(*) > 0 then cast(1 as bit) else cast(0 as bit) end as result 
        from dbo.post_liked_by 
        where post_id = :postId and user_id = :userId
    """, nativeQuery = true)
    Boolean existsPostLikedBy(Long postId, Long userId);

    boolean existsPostsByUser(User user);

    Page<Post> findByUser(Pageable pageable, User user);

    @Query(value = """
            select * 
            from post
            order by created_at desc
            """, nativeQuery = true
    )
    List<Post> findAllSortedByCreatedAtDesc();

}
