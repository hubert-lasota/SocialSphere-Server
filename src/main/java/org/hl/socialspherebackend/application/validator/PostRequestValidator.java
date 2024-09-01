package org.hl.socialspherebackend.application.validator;

import org.hl.socialspherebackend.api.dto.post.request.PostRequest;

public class PostRequestValidator extends RequestValidatorChain{

    public PostRequestValidator(RequestValidatorChain next) {
        super(next);
        setTextMinSize(1);
    }


    @Override
    protected RequestValidateResult doValidate(Object request) {
        PostRequest r = (PostRequest) request;
        String content = r.content();

        if(content == null) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_NULL,
                    "Post content is null");
        }

        if(content.isBlank() && !acceptBlankText) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_BLANK,
                    "Post content is blank");
        }

        if(content.length() > textMaxSize) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_TOO_LONG,
                    "Content max length is %d".formatted(textMaxSize));
        }

        if(content.length() < textMinSize) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_TOO_SHORT,
                    "Content min length is %d".formatted(textMinSize));
        }

        return new RequestValidateResult(true, null, null);
    }

    @Override
    protected boolean isRequestValidInstance(Object request) {
        return request instanceof PostRequest;
    }

}
