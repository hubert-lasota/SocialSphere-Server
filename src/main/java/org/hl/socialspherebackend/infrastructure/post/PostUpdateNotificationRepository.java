package org.hl.socialspherebackend.infrastructure.post;

import org.hl.socialspherebackend.api.entity.post.PostUpdateNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostUpdateNotificationRepository extends JpaRepository<PostUpdateNotification, Long> {

    @Query(value = """
        select pn.*
        from post_notification pn
        join post p
        on pn.post_id = p.id
        join users u
        on p.user_id = u.id
        where u.id = :userId
    """, nativeQuery = true)
    List<PostUpdateNotification> findPostUpdateNotificationsByUserId(Long userId);

}
