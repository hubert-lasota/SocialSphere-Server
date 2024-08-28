package org.hl.socialspherebackend.api.entity.post;

import jakarta.persistence.*;
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
    @JoinColumn(name = "updated_by", nullable = false, referencedColumnName = "id")
    private User updatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "update_type", nullable = false)
    private PostUpdateType updateType;

    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime2")
    private Instant updatedAt;

    @Column(name = "checked", nullable = false, columnDefinition = "BIT")
    private boolean isChecked;

    protected PostUpdateNotification() {

    }

    public PostUpdateNotification(Post updatedPost, User updatedBy, PostUpdateType updateType, Instant updatedAt, boolean isChecked) {
        this.updatedPost = updatedPost;
        this.updatedBy = updatedBy;
        this.updateType = updateType;
        this.updatedAt = updatedAt;
        this.isChecked = isChecked;
    }


    public void setIsChecked(boolean checked) {
        this.isChecked = checked;
    }

    public Long getId() {
        return id;
    }

    public Post getUpdatedPost() {
        return updatedPost;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public PostUpdateType getUpdateType() {
        return updateType;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean getIsChecked() {
        return isChecked;
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
