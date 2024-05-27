package org.hl.socialspherebackend.api.dto.post.response;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PostCommentResult {

    @JsonProperty
    private final PostCommentResponse comment;

    @JsonProperty
    private final Code code;

    @JsonProperty
    private final String message;

    public enum Code { NOT_FOUND, CANNOT_CREATE, FOUND, CREATED }

    private PostCommentResult(PostCommentResponse comment, Code code, String message) {
        this.comment = comment;
        this.code = code;
        this.message = message;
    }

    public static PostCommentResult success(PostCommentResponse comment) {
        return new PostCommentResult(comment, Code.FOUND, null);
    }

    public static PostCommentResult failure(Code code, String message) {
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
