package org.hl.socialspherebackend.infrastructure.user.repository;

import org.hl.socialspherebackend.api.entity.user.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {

}
