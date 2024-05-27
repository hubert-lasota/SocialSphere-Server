package org.hl.socialspherebackend.api.entity.user;

import jakarta.persistence.*;

@Entity
public class UserProfileConfig {

    @Id
    private Long id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_privacy_level")
    private UserProfilePrivacyLevel userProfilePrivacyLevel;

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

}
