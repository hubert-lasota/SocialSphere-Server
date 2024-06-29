package org.hl.socialspherebackend.api.entity.user;

import jakarta.persistence.*;

import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "user_profile_picture")
public class UserProfilePicture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "upp_id")
    private Long id;

    @Column(name = "image_type", nullable = false)
    private String imageType;

    @Lob
    @Column(name = "image", columnDefinition = "VARBINARY")
    private byte[] image;

    protected UserProfilePicture() {

    }

    public UserProfilePicture(String imageType, byte[] image) {
        this.imageType = imageType;
        this.image = image;
    }

    public Long getId() {
        return id;
    }

    public String getImageType() {
        return imageType;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfilePicture that = (UserProfilePicture) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserProfilePicture{" +
                "id=" + id +
                ", imageType='" + imageType + '\'' +
                ", image=" + Arrays.toString(image) +
                '}';
    }

}
