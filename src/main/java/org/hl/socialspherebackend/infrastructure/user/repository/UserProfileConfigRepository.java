package org.hl.socialspherebackend.infrastructure.user.repository;

import org.hl.socialspherebackend.api.entity.user.UserProfileConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserProfileConfigRepository extends JpaRepository<UserProfileConfig, Long> {

    @Query(value = "select * from user_profile_config where user_id = :userId", nativeQuery = true)
    Optional<UserProfileConfig> findByUserId(Long userId);

    @Query(value = "select * from user_profile_config where user_id = :userId", nativeQuery = true)
    boolean existsByUserId(Long userId);
}
