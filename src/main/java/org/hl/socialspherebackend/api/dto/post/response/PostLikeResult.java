package org.hl.socialspherebackend.api.dto.post.response;

public class PostLikeResult {

    private final Long postId;
    private final Long userId;
    private final Code code;
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

    public boolean isFailure() {
        return !isSuccess();
    }

}
