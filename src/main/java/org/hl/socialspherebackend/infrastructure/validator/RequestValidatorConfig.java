package org.hl.socialspherebackend.infrastructure.validator;

import org.hl.socialspherebackend.application.validator.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestValidatorConfig {

    @Bean
    public RequestValidatorChain requestValidatorChain() {
        ChatMessageRequestValidator chatMessageRequestValidator = new ChatMessageRequestValidator(null);
        PostCommentRequestValidator postCommentRequestValidator = new PostCommentRequestValidator(chatMessageRequestValidator);
        PostRequestValidator postRequestValidator = new PostRequestValidator(postCommentRequestValidator);
        UserProfileRequestValidator userProfileRequestValidator = new UserProfileRequestValidator(postRequestValidator);
        return new AuthorizationRequestValidator(userProfileRequestValidator);
    }

}
