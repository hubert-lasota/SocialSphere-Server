package org.hl.socialspherebackend.infrastructure.chat;

import org.hl.socialspherebackend.api.dto.chat.request.ChatRequest;
import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.application.chat.ChatFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/chat")
public class ChatEndpoint {

    private final ChatFacade chatFacade;

    public ChatEndpoint(ChatFacade chatFacade) {
        this.chatFacade = chatFacade;
    }


    @PostMapping
    public ResponseEntity<?> createChat(@RequestBody ChatRequest chatRequest) {
        DataResult<?> result = chatFacade.createChat(chatRequest);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }


    @GetMapping
    public ResponseEntity<?> findUserChats() {
        DataResult<?> result = chatFacade.findCurrentUserChats();

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @GetMapping(value = "/message")
    public ResponseEntity<?> findChatMessages(@RequestParam Long chatId) {
        DataResult<?> result = chatFacade.findChatMessages(chatId);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @GetMapping(value = "/message/new")
    public ResponseEntity<?> findChatsWithNewMessages() {
        DataResult<?> result = chatFacade.findCurrentUserChatsWithNewMessage();

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @PatchMapping(value = "/message/seenAll")
    public ResponseEntity<?> updateChat(@RequestParam Long chatId) {
        DataResult<?> result = chatFacade.setSeenAllMessagesInChat(chatId);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }
}
