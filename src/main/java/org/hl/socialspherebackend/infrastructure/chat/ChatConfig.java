package org.hl.socialspherebackend.infrastructure.chat;

import org.hl.socialspherebackend.application.chat.ChatFacade;
import org.hl.socialspherebackend.application.validator.RequestValidatorChain;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Clock;

@Configuration
public class ChatConfig {

    @Bean
    public ChatFacade chatFacade(SimpMessagingTemplate messagingTemplate,
                                 ChatRepository chatRepository,
                                 ChatMessageRepository chatMessageRepository,
                                 UserRepository userRepository,
                                 RequestValidatorChain requestValidatorChain,
                                 Clock clock) {
        return new ChatFacade(messagingTemplate, chatRepository, chatMessageRepository, userRepository, requestValidatorChain, clock);
    }

}
