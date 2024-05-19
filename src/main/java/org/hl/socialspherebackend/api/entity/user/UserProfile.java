package org.hl.socialspherebackend.api.entity.user;

import jakarta.persistence.*;

@Entity
public class UserProfile {

    @Id
    private Long id;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String firstName;

    private String lastName;

    private String city;

    private String country;

}
