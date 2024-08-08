package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.post.request.PostCommentRequest;
import org.hl.socialspherebackend.api.dto.post.response.PostErrorCode;
import org.hl.socialspherebackend.application.validator.RequestValidator;

public class PostCommentRequestValidator extends RequestValidator<PostCommentRequest, PostValidateResult> {

    public PostCommentRequestValidator() {
        super();
    }

    @Override
    public PostValidateResult validate(PostCommentRequest requestToValidate) {
        if(requestToValidate == null) {
            return new PostValidateResult(false, PostErrorCode.POST_COMMENT_REQUEST_IS_NULL,
                    "Post Comment request is null");
        }

        String content = requestToValidate.content();

        if(content == null) {
            return new PostValidateResult(false, PostErrorCode.CONTENT_IS_NULL,
                    "Post Comment content is null");
        }

        if(content.isBlank() && !acceptBlankText) {
            return new PostValidateResult(false, PostErrorCode.CONTENT_IS_BLANK,
                    "Post Comment content is blank");
        }

        if(content.length() > textMaxSize) {
            return new PostValidateResult(false, PostErrorCode.CONTENT_LENGTH_IS_TOO_LONG,
                    "Comment content max length is %d".formatted(textMaxSize));
        }

        if(content.length() < textMinSize) {
            return new PostValidateResult(false, PostErrorCode.CONTENT_LENGTH_IS_TOO_SHORT,
                    "Comment content min length is %d".formatted(textMinSize));
        }

        return new PostValidateResult(true, null, null);
    }

}
