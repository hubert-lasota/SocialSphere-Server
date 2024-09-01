package org.hl.socialspherebackend.infrastructure.chat;

import org.hl.socialspherebackend.api.dto.chat.request.ChatMessageRequest;
import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.application.chat.ChatFacade;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketEndpoint {

    private final ChatFacade chatFacade;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketEndpoint(ChatFacade chatFacade, SimpMessagingTemplate messagingTemplate) {
        this.chatFacade = chatFacade;
        this.messagingTemplate = messagingTemplate;
    }


    @MessageMapping("/chat")
    public void sendMessageToUser(@Payload ChatMessageRequest request) {
        DataResult<?> result = chatFacade.sendMessage(request);
        if(result.isFailure()) {
            return;
        }

        messagingTemplate.convertAndSendToUser(request.receiverId().toString(), "/queue/messages", result);
    }

}
