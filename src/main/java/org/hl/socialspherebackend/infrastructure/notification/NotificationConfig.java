package org.hl.socialspherebackend.infrastructure.notification;

import org.hl.socialspherebackend.application.notification.PostNotificationFacade;
import org.hl.socialspherebackend.application.notification.PostNotificationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfig {

    @Bean
    public PostNotificationManager postNotificationManager(PostNotificationFacade postNotificationFacade) {
        return new PostNotificationManager(postNotificationFacade);
    }

    @Bean
    public PostNotificationFacade postNotificationFacade(PostUpdateNotificationRepository repository) {
        return new PostNotificationFacade(repository);
    }

}