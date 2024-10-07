package org.hl.socialspherebackend.application.validator;

import org.hl.socialspherebackend.api.dto.chat.request.ChatMessageRequest;

public class ChatMessageRequestValidator extends RequestValidatorChain {

    public ChatMessageRequestValidator(RequestValidatorChain next) {
        super(next);
        setTextMinSize(1);
        setTextMaxSize(100);
        setAcceptWhitespace(true);
    }


    @Override
    protected RequestValidateResult doValidate(Object request) {
        ChatMessageRequest r = (ChatMessageRequest) request;
        String content = r.content();

        if(content.isBlank() && !acceptBlankText) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_BLANK,
                    "Message content is blank!");
        }

        if(content.length() < textMinSize) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_TOO_SHORT,
                    "Message content is too short. Min length is = %s".formatted(textMinSize));
        }

        if(content.length() > textMaxSize) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_TOO_LONG,
                    "Message content is too long. Max length is = %s".formatted(textMaxSize));
        }

        return new RequestValidateResult(true, null, null);
    }

    @Override
    protected boolean isRequestValidInstance(Object request) {
        return request instanceof ChatMessageRequest;
    }

}
