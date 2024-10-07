package org.hl.socialspherebackend.infrastructure.post;

import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.post.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    Set<PostComment> findByPost(Post post);

}
