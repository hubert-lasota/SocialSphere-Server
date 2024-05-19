package org.hl.socialspherebackend.api.dto.post.response;

import java.util.Set;

public class PostCommentsResult {

    private final Set<PostCommentResponse> comments;

    private final Code code;

    private final String message;

    public enum Code { NOT_FOUND, FOUND }

    private PostCommentsResult(Set<PostCommentResponse> comments, Code code, String message) {
        this.comments = comments;
        this.code = code;
        this.message = message;
    }


    public static PostCommentsResult success(Set<PostCommentResponse> comments) {
        return new PostCommentsResult(comments, Code.FOUND, null);
    }

    public static PostCommentsResult failure(Code code, String message) {
        return new PostCommentsResult(null, code, message);
    }

    public boolean isSuccess() {
        return comments != null && !comments.isEmpty();
    }

    public boolean isFailure() {
        return !isSuccess();
    }

}
