package org.hl.socialspherebackend.infrastructure.notification;

import org.hl.socialspherebackend.api.entity.notification.PostUpdateNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostUpdateNotificationRepository extends JpaRepository<PostUpdateNotification, Long> {

}
