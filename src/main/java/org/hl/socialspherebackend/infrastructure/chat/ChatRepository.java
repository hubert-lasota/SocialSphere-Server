package org.hl.socialspherebackend.infrastructure.chat;

import org.hl.socialspherebackend.api.entity.chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query(value = """
        select *
        from chat ch
        join chat_bound_users chbu
        on ch.id = chbu.chat_id
        where chbu.user_id = :userId
        """, nativeQuery = true)
    List<Chat> findChatsByUserId(Long userId);

    @Query(value =
            """
            select ch.id\s
            from chat ch
            join chat_bound_users chbu
            on ch.id = chbu.chat_id
            join chat_message chm
            on ch.id = chm.chat_id
            where chm.seen = 1;
            """, nativeQuery = true)
    List<Long> findChatsIdWithNewMessage(Long userId);

}
