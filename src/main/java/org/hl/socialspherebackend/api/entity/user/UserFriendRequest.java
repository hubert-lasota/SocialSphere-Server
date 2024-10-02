package org.hl.socialspherebackend.api.entity.user;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "user_friend_request")
public class UserFriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id", nullable = false, referencedColumnName = "id")
    private User sender;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_id", nullable = false, referencedColumnName = "id")
    private User receiver;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserFriendRequestStatus status;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(name = "replied_at")
    private Instant repliedAt;

    protected UserFriendRequest() {

    }

    public UserFriendRequest(User sender, User receiver, UserFriendRequestStatus status, Instant sentAt) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = status;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public UserFriendRequestStatus getStatus() {
        return status;
    }

    public void setStatus(UserFriendRequestStatus status) {
        this.status = status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getRepliedAt() {
        return repliedAt;
    }

    public void setRepliedAt(Instant replyAt) {
        this.repliedAt = replyAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserFriendRequest that = (UserFriendRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    @Override
    public String toString() {
        return "UserFriendRequest{" +
                "id=" + id +
                ", sender=" + sender +
                ", receiver=" + receiver +
                ", status=" + status +
                ", sentAt=" + sentAt +
                ", repliedAt=" + repliedAt +
                '}';
    }
}
