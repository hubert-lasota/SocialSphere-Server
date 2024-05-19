package org.hl.socialspherebackend.api.dto.post.response;

import java.time.Instant;
import java.util.Set;

public record PostResponse(Long id,
                           String content,
                           Set<byte[]> images,
                           Long likeCount,
                           Long commentCount,
                           Instant createdAt,
                           Instant updatedAt) {
}
