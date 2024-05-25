package org.hl.socialspherebackend.infrastructure.user.repository;

import org.hl.socialspherebackend.api.entity.user.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    @Query(value = "select * from dbo.user_profile where user_id = :userId", nativeQuery = true)
    Optional<UserProfile> findByUserId(Long userId);

    @Query(value = "select * from dbo.user_profile where user_id = :userId", nativeQuery = true)
    boolean existsByUserId(Long userId);

}
