package org.hl.socialspherebackend.api.entity.chat;

import jakarta.persistence.*;
import org.hl.socialspherebackend.api.entity.user.User;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chat")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime2")
    private Instant createdAt;

    @Column(name = "last_message", columnDefinition = "datetime2")
    private Instant lastMessage;

    @OneToMany(mappedBy = "chat", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<ChatMessage> chatMessages = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "chat_room",
            joinColumns = @JoinColumn(name = "chat_id", nullable = false, referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    )
    private Set<User> users = new HashSet<>();

    protected Chat() {

    }

    public Chat(Instant createdAt) {
        this.createdAt = createdAt;
    }


    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public void addChatMessage(ChatMessage chatMessage) {
        chatMessages.add(chatMessage);
    }

    public void removeChatMessage(ChatMessage chatMessage) {
        chatMessages.remove(chatMessage);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Instant lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Set<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(Set<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", lastMessage=" + lastMessage +
                ", chatMessages=" + chatMessages +
                ", users=" + users +
                '}';
    }
}
