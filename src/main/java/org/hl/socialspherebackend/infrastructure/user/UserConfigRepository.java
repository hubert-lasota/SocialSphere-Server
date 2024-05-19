package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.api.entity.user.UserConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserConfigRepository extends JpaRepository<UserConfig, Long> {

    @Query(value = "select * from user_config where user_id = :userId", nativeQuery = true)
    Optional<UserConfig> findByUserId(Long userId);

}
