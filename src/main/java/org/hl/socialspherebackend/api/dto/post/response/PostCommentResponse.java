package org.hl.socialspherebackend.api.dto.post.response;

import java.time.Instant;

public record PostCommentResponse(Long id,
                                  Long postId,
                                  Long authorId,
                                  String content,
                                  Instant createdAt,
                                  Instant updatedAt) {
}
