package org.hl.socialspherebackend.api.dto.post.response;

import org.hl.socialspherebackend.api.dto.user.response.UserProfileResponse;

import java.time.Instant;
import java.util.Set;

public record PostResponse(Long id,
                           Long userId,
                           UserProfileResponse userProfile,
                           String content,
                           Set<byte[]> images,
                           Long likeCount,
                           Long commentCount,
                           Instant createdAt,
                           Instant updatedAt) {
}
