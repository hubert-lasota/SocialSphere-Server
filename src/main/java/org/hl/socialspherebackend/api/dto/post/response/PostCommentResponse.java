package org.hl.socialspherebackend.api.dto.post.response;

import org.hl.socialspherebackend.api.dto.user.response.UserProfileResponse;

import java.time.Instant;

public record PostCommentResponse(Long id,
                                  Long postId,
                                  Long authorId,
                                  UserProfileResponse authorProfile,
                                  String content,
                                  Instant createdAt,
                                  Instant updatedAt) {
}
