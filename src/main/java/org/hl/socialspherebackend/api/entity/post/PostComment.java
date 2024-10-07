package org.hl.socialspherebackend.api.entity.post;

import jakarta.persistence.*;
import org.hl.socialspherebackend.api.entity.user.User;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "post_comment")
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User commentAuthor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id", nullable = false, referencedColumnName = "id")
    private Post post;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime2")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "datetime2")
    private Instant updatedAt;


    protected PostComment() {

    }

    public PostComment(User commentAuthor, Post post, String content, Instant createdAt, Instant updatedAt) {
        this.commentAuthor = commentAuthor;
        this.post = post;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCommentAuthor() {
        return commentAuthor;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostComment that = (PostComment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PostComment{" +
                "id=" + id +
                ", commentAuthor=" + commentAuthor +
                ", post=" + post +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

}
