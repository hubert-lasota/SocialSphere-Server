package org.hl.socialspherebackend.infrastructure.post.repository;

import org.hl.socialspherebackend.api.entity.post.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
}
