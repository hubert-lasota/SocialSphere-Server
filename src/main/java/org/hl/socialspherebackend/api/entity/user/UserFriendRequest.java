package org.hl.socialspherebackend.api.entity.user;

import jakarta.persistence.*;

@Entity
@Table(name = "user_friend_request")
public class UserFriendRequest {

    @EmbeddedId
    private UserFriendRequestId id;

    @MapsId(value = "senderId")
    @OneToOne
    @JoinColumn(name = "sender_id", nullable = false, referencedColumnName = "id")
    private User sender;

    @MapsId(value = "receiverId")
    @OneToOne
    @JoinColumn(name = "receiver_id", nullable = false, referencedColumnName = "id")
    private User receiver;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserFriendRequestStatus status;

    public UserFriendRequestId getId() {
        return id;
    }

    public void setId(UserFriendRequestId id) {
        this.id = id;
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

}
