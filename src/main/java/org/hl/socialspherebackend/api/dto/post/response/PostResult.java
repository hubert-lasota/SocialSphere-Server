package org.hl.socialspherebackend.api.dto.post.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PostResult {

    @JsonProperty
    private final PostResponse post;

    @JsonProperty
    private final PostErrorCode code;

    @JsonProperty
    private final String message;

    private PostResult(PostResponse post, PostErrorCode code, String message) {
        this.post = post;
        this.code = code;
        this.message = message;
    }


    public static PostResult success(PostResponse post) {
        return new PostResult(post, null, null);
    }

    public static PostResult failure(PostErrorCode code, String message) {
        return new PostResult(null, code, message);
    }

    public boolean isSuccess() {
        return post != null;
    }

    @JsonIgnore
    public boolean isFailure() {
        return !isSuccess();
    }

    @Override
    public String toString() {
        return "PostResult{" +
                "post=" + post +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

}
