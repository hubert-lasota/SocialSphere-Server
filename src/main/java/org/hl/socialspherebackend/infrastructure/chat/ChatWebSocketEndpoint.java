package org.hl.socialspherebackend.infrastructure.chat;

import org.hl.socialspherebackend.api.dto.chat.request.ChatMessageRequest;
import org.hl.socialspherebackend.application.chat.ChatFacade;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketEndpoint {

    private final ChatFacade chatFacade;

    public ChatWebSocketEndpoint(ChatFacade chatFacade) {
        this.chatFacade = chatFacade;
    }


    @MessageMapping("/chat/message")
    public void sendMessageToUser(@Payload ChatMessageRequest request, Principal principal) {
        chatFacade.sendMessage(request, principal);
    }

}
