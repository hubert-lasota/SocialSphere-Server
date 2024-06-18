package org.hl.socialspherebackend.api.entity.user;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

@Entity
public class Authority implements GrantedAuthority {

    @Id
    private Long id;

    @MapsId
    @ManyToOne(optional = false)
    @JoinColumn(name="user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "authority", nullable = false)
    private String authority;

    protected Authority() {

    }

    public Authority(User user, String authority) {
        this.user = user;
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Authority authority = (Authority) o;
        return Objects.equals(id, authority.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Authority{" +
                "id=" + id +
                ", user=" + user +
                ", authority='" + authority + '\'' +
                '}';
    }

}
