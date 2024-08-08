package org.hl.socialspherebackend.application.chat;

import org.hl.socialspherebackend.api.dto.chat.request.ChatMessageRequest;
import org.hl.socialspherebackend.api.dto.chat.response.ChatErrorCode;
import org.hl.socialspherebackend.application.validator.RequestValidator;

public class ChatMessageRequestValidator extends RequestValidator<ChatMessageRequest, ChatValidateResult> {

    public ChatMessageRequestValidator() {
        super();
        setTextMinSize(1);
        setTextMaxSize(100);
        setAcceptWhitespace(true);
    }

    @Override
    public ChatValidateResult validate(ChatMessageRequest requestToValidate) {
        if(requestToValidate == null) {
            return new ChatValidateResult(false, ChatErrorCode.CHAT_MESSAGE_REQUEST_IS_NULL, "Request is null");
        }

        String content = requestToValidate.content();

        if(content == null) {
            return new ChatValidateResult(false, ChatErrorCode.MESSAGE_IS_NULL, "Content is null");
        }

        if(content.isBlank() && !acceptBlankText) {
            return new ChatValidateResult(false, ChatErrorCode.MESSAGE_IS_BLANK, "Content is blank");
        }

        if(content.length() < textMinSize) {
            return new ChatValidateResult(false, ChatErrorCode.MESSAGE_LENGTH_IS_TOO_SHORT,
                    "Content length is too short. Minimum length is %d".formatted(textMinSize));
        }

        if(content.length() > textMaxSize) {
            return new ChatValidateResult(false, ChatErrorCode.MESSAGE_LENGTH_IS_TOO_LONG,
                    "Content length is too long. Maximum length is %d".formatted(textMaxSize));
        }

        return new ChatValidateResult(true, null, null);
    }


}
