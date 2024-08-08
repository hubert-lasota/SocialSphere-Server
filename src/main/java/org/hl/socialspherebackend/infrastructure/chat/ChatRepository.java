package org.hl.socialspherebackend.infrastructure.chat;

import org.hl.socialspherebackend.api.entity.chat.Chat;
import org.hl.socialspherebackend.api.entity.chat.ChatMessage;
import org.hl.socialspherebackend.api.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query(value = """
        select *
        from chat ch
        join chat_room chr
        on ch.id = chr.chat_id
        where chr.user_id = :userId
        """, nativeQuery = true)
    List<Chat> findChatsByUserId(Long userId);

    @Query(value = """
        select u.*, up.*, upc.*
        from users u
        left join user_profile up
        on u.id = up.user_id
        left join user_profile_config upc
        on u.id = upc.user_id
        where u.id = (
        	select top 1 user_id from chat_room where chat_id = 1 and user_id <> 1
        )
    """, nativeQuery = true)
    User findSecondUserInChat(Long chatId, Long firstUserId);

    @Query(value = """
        select top 1 * from chat_message order by created_at
    """, nativeQuery = true)
    Optional<ChatMessage> findLastChatMessageByChatId(Long chatId);

}
