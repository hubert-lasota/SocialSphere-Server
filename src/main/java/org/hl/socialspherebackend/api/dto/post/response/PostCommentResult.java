package org.hl.socialspherebackend.api.dto.post.response;


public class PostCommentResult {

    private final PostCommentResponse comment;

    private final Code code;

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

    public boolean isFailure() {
        return !isSuccess();
    }
}
