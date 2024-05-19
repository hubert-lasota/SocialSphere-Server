package org.hl.socialspherebackend.api.entity.user;

import jakarta.persistence.*;

@Entity
public class UserConfig {

    @Id
    private Long id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "privacy_level")
    private UserPrivacyLevel userPrivacyLevel;

    public UserPrivacyLevel getUserPrivacyLevel() {
        return userPrivacyLevel;
    }
}
