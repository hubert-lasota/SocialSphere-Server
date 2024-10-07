package org.hl.socialspherebackend.infrastructure.init_dev_data;

import org.hl.socialspherebackend.api.entity.chat.Chat;
import org.hl.socialspherebackend.api.entity.chat.ChatMessage;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.infrastructure.chat.ChatMessageRepository;
import org.hl.socialspherebackend.infrastructure.chat.ChatRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;

import java.time.Instant;
import java.util.List;

class ChatInitData {
    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    ChatInitData(ChatRepository chatRepository, ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    void initData() {
        if(chatRepository.count() > 0) {
            return;
        }
        createChatsWithMessages();
    }

    private void createChatsWithMessages() {
        User user1 = userRepository.findById(1L).orElseThrow();
        User user2 = userRepository.findById(2L).orElseThrow();
        User user3 = userRepository.findById(3L).orElseThrow();
        Instant now = Instant.now();
        Chat chat1 = new Chat(user1, now);
        chat1.getUsers().add(user1);
        chat1.getUsers().add(user2);

        Chat chat2 = new Chat(user1, now);
        chat2.getUsers().add(user1);
        chat2.getUsers().add(user3);

        Chat chat3 = new Chat(user2, now);
        chat3.getUsers().add(user2);
        chat3.getUsers().add(user3);
        chatRepository.saveAll(List.of(chat1, chat2, chat3));

        String mess1 = "hi";
        String mess2 = "how are you doing?";

        ChatMessage message1 = new ChatMessage(mess1, now, chat1, user1);
        ChatMessage message2 = new ChatMessage(mess2, now, chat1, user1);
        ChatMessage message3 = new ChatMessage(mess1, now.plusSeconds(300), chat1, user2);

        ChatMessage message4 = new ChatMessage(mess1, now, chat2, user1);
        ChatMessage message5 = new ChatMessage(mess2, now, chat2, user1);
        ChatMessage message6 = new ChatMessage(mess1, now.plusSeconds(300), chat2, user3);

        ChatMessage message7 = new ChatMessage(mess1, now, chat3, user2);
        ChatMessage message8 = new ChatMessage(mess2, now, chat3, user2);
        ChatMessage message9 = new ChatMessage(mess1, now.plusSeconds(300), chat3, user3);
        chatMessageRepository.saveAll(List.of(message1, message2, message3, message4, message5, message6, message7, message8, message9));
    }

}
