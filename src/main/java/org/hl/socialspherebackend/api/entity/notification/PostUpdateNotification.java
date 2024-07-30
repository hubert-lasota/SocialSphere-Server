package org.hl.socialspherebackend.api.entity.notification;

import jakarta.persistence.*;
import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.user.User;

import java.time.Instant;

@Entity
@Table(name = "post_notification")
public class PostUpdateNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id", nullable = false, referencedColumnName = "id")
    private Post updatedPost;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User updatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "update_type", nullable = false)
    private PostUpdateType updateType;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime2")
    private Instant createdAt;

    protected PostUpdateNotification() {

    }

    public PostUpdateNotification(Post updatedPost, User updatedBy, PostUpdateType updateType, Instant createdAt) {
        this.updatedPost = updatedPost;
        this.updatedBy = updatedBy;
        this.updateType = updateType;
        this.createdAt = createdAt;
    }



    @Override
    public String toString() {
        return "PostUpdateNotification{" +
                "id=" + id +
                ", updatedPost=" + updatedPost +
                ", updatedBy=" + updatedBy +
                ", updateType=" + updateType +
                '}';
    }

}
