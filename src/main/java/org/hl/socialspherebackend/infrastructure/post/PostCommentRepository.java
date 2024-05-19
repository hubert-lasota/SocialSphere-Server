package org.hl.socialspherebackend.infrastructure.post;

import org.hl.socialspherebackend.api.entity.post.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

}
