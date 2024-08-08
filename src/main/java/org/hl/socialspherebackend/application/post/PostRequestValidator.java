package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.post.request.PostRequest;
import org.hl.socialspherebackend.api.dto.post.response.PostErrorCode;
import org.hl.socialspherebackend.application.validator.RequestValidator;

public class PostRequestValidator extends RequestValidator<PostRequest, PostValidateResult> {

    public PostRequestValidator() {
        super();
    }

    @Override
    public PostValidateResult validate(PostRequest requestToValidate) {
        if(requestToValidate == null) {
            return new PostValidateResult(false, PostErrorCode.POST_REQUEST_IS_NULL,
                    "Post request is null");
        }

        String content = requestToValidate.content();

        if(content == null) {
            return new PostValidateResult(false, PostErrorCode.CONTENT_IS_NULL,
                    "Post content is null");
        }

        if(content.isBlank() && !acceptBlankText) {
            return new PostValidateResult(false, PostErrorCode.CONTENT_IS_BLANK,
                    "Post content is blank");
        }

        if(content.length() > textMaxSize) {
            return new PostValidateResult(false, PostErrorCode.CONTENT_LENGTH_IS_TOO_LONG,
                    "Content max length is %d".formatted(textMaxSize));
        }

        if(content.length() < textMinSize) {
            return new PostValidateResult(false, PostErrorCode.CONTENT_LENGTH_IS_TOO_SHORT,
                    "Content min length is %d".formatted(textMinSize));
        }

        return new PostValidateResult(true, null, null);
    }

}
