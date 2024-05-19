package org.hl.socialspherebackend.api.entity.post;

import jakarta.persistence.*;
import org.hl.socialspherebackend.api.entity.user.User;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;

    @Column(name = "content")
    private String content;

    @OneToMany
    private Set<PostImage> images;

    @ManyToMany
    @JoinTable(
            name = "post_liked_by",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedBy;

    @ManyToMany
    @JoinTable(
            name = "post_commented_by",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> commentedBy;

    @Column(name = "like_count", nullable = false)
    private Long likeCount;

    @Column(name = "comment_count", nullable = false)
    private Long commentCount;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime2")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime2")
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<PostImage> getImages() {
        return images;
    }

    public void setImages(Set<PostImage> images) {
        this.images = images;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
