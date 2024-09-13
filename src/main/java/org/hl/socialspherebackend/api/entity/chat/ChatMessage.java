package org.hl.socialspherebackend.api.entity.chat;

import jakarta.persistence.*;
import org.hl.socialspherebackend.api.entity.user.User;

import java.time.Instant;

@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime2")
    private Instant createdAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chat_id", nullable = false, referencedColumnName = "id")
    private Chat chat;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id", nullable = false, referencedColumnName = "id")
    private User sender;

    @Column(name = "seen", nullable = false)
    private boolean seen;

    protected ChatMessage() {

    }

    public ChatMessage(String content, Instant createdAt, Chat chat, User sender) {
        this.content = content;
        this.createdAt = createdAt;
        this.chat = chat;
        this.sender = sender;
        seen = false;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public User getSender() {
        return sender;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", chat=" + chat +
                ", sender=" + sender +
                ", seen=" + seen +
                '}';
    }

}
