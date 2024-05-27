package org.hl.socialspherebackend.api.dto.post.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PostLikeResult {

    @JsonProperty
    private final Long postId;

    @JsonProperty
    private final Long userId;

    @JsonProperty
    private final Code code;

    @JsonProperty
    private final String message;

    public enum Code { USER_NOT_FOUND, POST_NOT_FOUND, USER_ALREADY_LIKES_POST, LIKE_ADDED}

    public PostLikeResult(Long postId, Long userId, Code code, String message) {
        this.postId = postId;
        this.userId = userId;
        this.code = code;
        this.message = message;
    }

    public static PostLikeResult success(Long postId, Long userId) {
        return new PostLikeResult(postId, userId, Code.LIKE_ADDED, null);
    }

    public static PostLikeResult failure(Code code, String message) {
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
