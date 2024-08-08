package org.hl.socialspherebackend.infrastructure.chat;

import org.hl.socialspherebackend.api.dto.chat.request.ChatMessageRequest;
import org.hl.socialspherebackend.application.chat.ChatFacade;
import org.hl.socialspherebackend.application.chat.ChatMessageRequestValidator;
import org.hl.socialspherebackend.application.chat.ChatValidateResult;
import org.hl.socialspherebackend.application.validator.RequestValidator;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ChatConfig {

    @Bean
    public ChatFacade chatFacade(ChatRepository chatRepository,
                                 UserRepository userRepository,
                                 RequestValidator<ChatMessageRequest, ChatValidateResult> chatMessageValidator,
                                 Clock clock) {
        return new ChatFacade(chatRepository, userRepository, chatMessageValidator, clock);
    }

    @Bean
    public ChatMessageRequestValidator chatMessageValidator() {
        return new ChatMessageRequestValidator();
    }

}
