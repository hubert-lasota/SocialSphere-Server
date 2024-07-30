package org.hl.socialspherebackend.application.notification;

import org.hl.socialspherebackend.api.dto.notification.response.PostUpdateDetails;
import org.hl.socialspherebackend.api.entity.notification.PostUpdateNotification;
import org.hl.socialspherebackend.infrastructure.notification.PostUpdateNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostNotificationFacade {

    private final static Logger log = LoggerFactory.getLogger(PostNotificationFacade.class);

    private final PostUpdateNotificationRepository repository;

    public PostNotificationFacade(PostUpdateNotificationRepository repository) {
        this.repository = repository;
    }

    public PostUpdateNotification savePostUpdateNotification(PostUpdateDetails postUpdateDetails) {
        return null;
    }

}
