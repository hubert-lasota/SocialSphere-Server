package org.hl.socialspherebackend.api.entity.post;

import jakarta.persistence.*;
import org.hl.socialspherebackend.api.entity.user.User;

import java.time.Instant;

@Entity
@Table(name = "post_comment")
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User commentAuthor;

    @OneToOne
    @JoinColumn(name = "post_id", nullable = false, referencedColumnName = "id")
    private Post post;

    @Column(name = "content", nullable = false)
    private String content;

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

    public User getCommentAuthor() {
        return commentAuthor;
    }

    public void setCommentAuthor(User commentAuthor) {
        this.commentAuthor = commentAuthor;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}
