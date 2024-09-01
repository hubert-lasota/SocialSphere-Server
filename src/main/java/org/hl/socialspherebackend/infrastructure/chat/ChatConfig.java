package org.hl.socialspherebackend.infrastructure.chat;

import org.hl.socialspherebackend.application.chat.ChatFacade;
import org.hl.socialspherebackend.application.validator.RequestValidatorChain;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ChatConfig {

    @Bean
    public ChatFacade chatFacade(ChatRepository chatRepository,
                                 UserRepository userRepository,
                                 RequestValidatorChain requestValidatorChain,
                                 Clock clock) {
        return new ChatFacade(chatRepository, userRepository, requestValidatorChain, clock);
    }

}
