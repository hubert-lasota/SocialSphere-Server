package org.hl.socialspherebackend.infrastructure.chat;

import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.application.chat.ChatFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/chat")
public class ChatEndpoint {

    private final ChatFacade chatFacade;

    public ChatEndpoint(ChatFacade chatFacade) {
        this.chatFacade = chatFacade;
    }


    @GetMapping
    public ResponseEntity<DataResult<?, ?>> findUserChats(@RequestParam Long userId) {
        DataResult<?, ?> result = chatFacade.findUserChats(userId);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/message")
    public ResponseEntity<?> findChatMessages(@RequestParam Long chatId) {
        DataResult<?, ?> result = chatFacade.findChatMessages(chatId);

        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.notFound().build();
    }

}
