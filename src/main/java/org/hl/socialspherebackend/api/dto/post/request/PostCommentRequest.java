package org.hl.socialspherebackend.api.dto.post.request;

public record PostCommentRequest(Long postId,
                                 Long authorId,
                                 String content) {
}
