package org.hl.socialspherebackend.application.validator;

import org.hl.socialspherebackend.api.dto.post.request.PostCommentRequest;

public class PostCommentRequestValidator extends RequestValidatorChain {

    public PostCommentRequestValidator(RequestValidatorChain next) {
        super(next);
        setTextMinSize(1);
    }


    @Override
    protected RequestValidateResult doValidate(Object request) {
        PostCommentRequest r = (PostCommentRequest) request;
        String content = r.content();

        if(content == null) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_NULL,
                    "Post Comment content is null");
        }

        if(content.isBlank() && !acceptBlankText) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_BLANK,
                    "Post Comment content is blank");
        }

        if(content.length() > textMaxSize) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_TOO_LONG,
                    "Comment content max length is %d".formatted(textMaxSize));
        }

        if(content.length() < textMinSize) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_TOO_SHORT,
                    "Comment content min length is %d".formatted(textMinSize));
        }

        return new RequestValidateResult(true, null, null);
    }

    @Override
    protected boolean isRequestValidInstance(Object request) {
        return request instanceof PostCommentRequest;
    }

}
