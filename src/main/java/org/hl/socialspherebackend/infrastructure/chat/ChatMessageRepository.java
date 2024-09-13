package org.hl.socialspherebackend.infrastructure.chat;

import org.hl.socialspherebackend.api.entity.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query(value = """
        select top 1 * from chat_message where chat_id = :chatId order by created_at 
    """, nativeQuery = true)
    Optional<ChatMessage> findLastChatMessageByChatId(Long chatId);

}
