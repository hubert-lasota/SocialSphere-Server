package org.hl.socialspherebackend.api.entity.chat;

import jakarta.persistence.*;
import org.hl.socialspherebackend.api.entity.user.User;

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


    protected UserFriendRequest() {

    }

    public UserFriendRequest(User sender, User receiver, UserFriendRequestStatus status) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = status;
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
                '}';
    }

}
