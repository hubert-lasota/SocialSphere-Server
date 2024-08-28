package org.hl.socialspherebackend.api.entity.post;

import jakarta.persistence.*;

import java.util.Arrays;

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

    @Column(name = "name", nullable = false)
    private String name;

    @Lob
    @Column(name = "image", columnDefinition = "VARBINARY")
    private byte[] image;

    protected PostImage() {

    }

    public PostImage(byte[] image, String type, String name, Post post) {
        this.image = image;
        this.type = type;
        this.name = name;
        this.post = post;
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

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "PostImage{" +
                "id=" + id +
                ", post=" + post +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", image=" + Arrays.toString(image) +
                '}';
    }

}
