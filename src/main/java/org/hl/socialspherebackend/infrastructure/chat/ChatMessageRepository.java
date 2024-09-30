package org.hl.socialspherebackend.infrastructure.chat;

import org.hl.socialspherebackend.api.entity.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

}
