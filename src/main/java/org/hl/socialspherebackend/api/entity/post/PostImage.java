package org.hl.socialspherebackend.api.entity.post;

import jakarta.persistence.*;

import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "post_image")
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false, referencedColumnName = "id")
    private Post post;

    @Column(name = "type", nullable = false)
    private String type;

    @Lob
    @Column(name = "image", columnDefinition = "VARBINARY")
    private byte[] image;

    public PostImage() {

    }

    public PostImage(Post post, String type, byte[] image) {
        this.post = post;
        this.type = type;
        this.image = image;
    }


    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostImage postImage = (PostImage) o;
        return Objects.equals(id, postImage.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PostImage{" +
                "id=" + id +
                ", post=" + post +
                ", type='" + type + '\'' +
                ", image=" + Arrays.toString(image) +
                '}';
    }

}
