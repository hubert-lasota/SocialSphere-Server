package org.hl.socialspherebackend.api.entity.user;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class UserProfileConfig {

    @Id
    private Long id;

    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_privacy_level")
    private UserProfilePrivacyLevel userProfilePrivacyLevel;

    protected UserProfileConfig() {

    }

    public UserProfileConfig(UserProfilePrivacyLevel userProfilePrivacyLevel, User user) {
        this.userProfilePrivacyLevel = userProfilePrivacyLevel;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUserProfilePrivacyLevel(UserProfilePrivacyLevel userProfilePrivacyLevel) {
        this.userProfilePrivacyLevel = userProfilePrivacyLevel;
    }

    public UserProfilePrivacyLevel getUserPrivacyLevel() {
        return userProfilePrivacyLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfileConfig that = (UserProfileConfig) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserProfileConfig{" +
                "id=" + id +
                ", user=" + user +
                ", userProfilePrivacyLevel=" + userProfilePrivacyLevel +
                '}';
    }

}
