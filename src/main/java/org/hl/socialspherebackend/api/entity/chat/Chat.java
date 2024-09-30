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

    @JoinColumn(name = "created_by_id", nullable = false, referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private User createdBy;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime2")
    private Instant createdAt;

    @JoinColumn(name = "last_message_id", referencedColumnName = "id")
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private ChatMessage lastMessage;


    @OneToMany(mappedBy = "chat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ChatMessage> chatMessages = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "chat_bound_users",
            joinColumns = @JoinColumn(name = "chat_id", nullable = false, referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    )
    private Set<User> users = new HashSet<>();

    protected Chat() {

    }

    public Chat(User createdBy, Instant createdAt) {
        this.createdBy = createdBy;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public ChatMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(ChatMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Set<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "Chat{" + "id=" + id + "}";
    }
}
