package org.hl.socialspherebackend.api.dto.post.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PostResult {

    @JsonProperty
    private final PostResponse post;

    @JsonProperty
    private final Code code;

    @JsonProperty
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
