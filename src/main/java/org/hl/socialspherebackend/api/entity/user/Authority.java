package org.hl.socialspherebackend.api.entity.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.springframework.security.core.GrantedAuthority;

@Entity
public class Authority implements GrantedAuthority {

    @Id
    @ManyToOne
    @JoinColumn(
            name="user_id",
            referencedColumnName = "id"
    )
    private User user;

    @JoinColumn(
            name = "authority",
            nullable = false
    )
    private String authority;

    @Override
    public String getAuthority() {
        return authority;
    }

}
