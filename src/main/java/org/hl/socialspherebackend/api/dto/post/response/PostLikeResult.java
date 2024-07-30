package org.hl.socialspherebackend.api.dto.post.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PostLikeResult {

    @JsonProperty
    private final Long postId;

    @JsonProperty
    private final Long userId;

    @JsonProperty
    private final PostErrorCode code;

    @JsonProperty
    private final String message;

    public PostLikeResult(Long postId, Long userId, PostErrorCode code, String message) {
        this.postId = postId;
        this.userId = userId;
        this.code = code;
        this.message = message;
    }

    public static PostLikeResult success(Long postId, Long userId) {
        return new PostLikeResult(postId, userId, null, null);
    }

    public static PostLikeResult failure(PostErrorCode code, String message) {
        return new PostLikeResult(null, null, code, message);
    }

    public boolean isSuccess() {
        return postId != null && userId != null;
    }

    @JsonIgnore
    public boolean isFailure() {
        return !isSuccess();
    }

    @Override
    public String toString() {
        return "PostLikeResult{" +
                "postId=" + postId +
                ", userId=" + userId +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

}
