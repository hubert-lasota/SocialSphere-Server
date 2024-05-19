package org.hl.socialspherebackend.api.entity.user;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;

@Entity
public class Authority implements GrantedAuthority {

    @Id
    private Long id;

    @MapsId
    @ManyToOne
    @JoinColumn(name="user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "authority", nullable = false)
    private String authority;

    @Override
    public String getAuthority() {
        return authority;
    }

}
