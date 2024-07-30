package org.hl.socialspherebackend.api.dto.post.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class PostCommentsResult {

    @JsonProperty
    private final Set<PostCommentResponse> comments;

    @JsonProperty
    private final PostErrorCode code;

    @JsonProperty
    private final String message;

    private PostCommentsResult(Set<PostCommentResponse> comments, PostErrorCode code, String message) {
        this.comments = comments;
        this.code = code;
        this.message = message;
    }


    public static PostCommentsResult success(Set<PostCommentResponse> comments) {
        return new PostCommentsResult(comments, null, null);
    }

    public static PostCommentsResult failure(PostErrorCode code, String message) {
        return new PostCommentsResult(null, code, message);
    }

    public boolean isSuccess() {
        return comments != null && !comments.isEmpty();
    }

    @JsonIgnore
    public boolean isFailure() {
        return !isSuccess();
    }

    @Override
    public String toString() {
        return "PostCommentsResult{" +
                "comments=" + comments +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

}
