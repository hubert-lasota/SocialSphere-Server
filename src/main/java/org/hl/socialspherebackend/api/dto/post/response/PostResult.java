package org.hl.socialspherebackend.api.dto.post.response;

public class PostResult {

    private final PostResponse post;

    private final Code code;

    private final String message;

    public enum Code { NOT_FOUND, FOUND, CREATED, CANNOT_CREATE }

    private PostResult(PostResponse post, Code code, String message) {
        this.post = post;
        this.code = code;
        this.message = message;
    }

    public static PostResult success(PostResponse post, Code code) {
        return new PostResult(post,code, null);
    }

    public static PostResult failure(Code code, String message) {
        return new PostResult(null, code, null);
    }

    public boolean isSuccess() {
        return post != null;
    }

    public boolean isFailure() {
        return !isSuccess();
    }

}
