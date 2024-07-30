package org.hl.socialspherebackend.api.dto.post.response;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PostCommentResult {

    @JsonProperty
    private final PostCommentResponse comment;

    @JsonProperty
    private final PostErrorCode code;

    @JsonProperty
    private final String message;

    private PostCommentResult(PostCommentResponse comment, PostErrorCode code, String message) {
        this.comment = comment;
        this.code = code;
        this.message = message;
    }

    public static PostCommentResult success(PostCommentResponse comment) {
        return new PostCommentResult(comment, null, null);
    }

    public static PostCommentResult failure(PostErrorCode code, String message) {
        return new PostCommentResult(null, code, message);
    }

    public boolean isSuccess() {
        return comment != null;
    }

    @JsonIgnore
    public boolean isFailure() {
        return !isSuccess();
    }

    @Override
    public String toString() {
        return "PostCommentResult{" +
                "comment=" + comment +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

}
