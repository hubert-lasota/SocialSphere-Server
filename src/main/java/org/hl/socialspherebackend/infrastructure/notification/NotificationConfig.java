package org.hl.socialspherebackend.infrastructure.notification;

import org.hl.socialspherebackend.application.notification.PostNotificationFacade;
import org.hl.socialspherebackend.application.notification.PostNotificationManager;
import org.hl.socialspherebackend.application.notification.UserFriendRequestFacade;
import org.hl.socialspherebackend.infrastructure.post.PostRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfig {

    @Bean
    public PostNotificationManager postNotificationManager(PostNotificationFacade postNotificationFacade) {
        return new PostNotificationManager(postNotificationFacade);
    }

    @Bean
    public PostNotificationFacade postNotificationFacade(PostUpdateNotificationRepository repository, PostRepository postRepository, UserRepository userRepository) {
        return new PostNotificationFacade(repository, postRepository, userRepository);
    }

    @Bean
    public UserFriendRequestFacade userFriendRequestFacade(UserRepository userRepository) {
        return new UserFriendRequestFacade(userRepository);
    }

}