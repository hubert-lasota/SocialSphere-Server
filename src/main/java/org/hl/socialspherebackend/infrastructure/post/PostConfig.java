package org.hl.socialspherebackend.infrastructure.post;

import org.hl.socialspherebackend.api.dto.post.response.PostUpdateDetails;
import org.hl.socialspherebackend.application.common.Observer;
import org.hl.socialspherebackend.application.post.PostFacade;
import org.hl.socialspherebackend.application.post.PostNotificationFacade;
import org.hl.socialspherebackend.application.post.PostNotificationManager;
import org.hl.socialspherebackend.application.user.UserProfilePermissionChecker;
import org.hl.socialspherebackend.application.validator.RequestValidatorChain;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.util.Set;

@Configuration
public class PostConfig {

    @Bean
    public PostFacade postFacade(PostRepository postRepository,
                                 PostCommentRepository postCommentRepository,
                                 UserRepository userRepository,
                                 UserProfilePermissionChecker permissionChecker,
                                 RequestValidatorChain requestValidatorChain,
                                 Set<Observer<PostUpdateDetails>> observers,
                                 Clock clock) {

        return new PostFacade(postRepository, postCommentRepository, userRepository, permissionChecker, requestValidatorChain, observers, clock);
    }

    @Bean
    public PostNotificationManager postNotificationManager(PostNotificationFacade postNotificationFacade) {
        return new PostNotificationManager(postNotificationFacade);
    }

    @Bean
    public PostNotificationFacade postNotificationFacade(PostUpdateNotificationRepository repository, PostRepository postRepository, UserRepository userRepository) {
        return new PostNotificationFacade(repository, postRepository, userRepository);
    }

    @Profile("dev")
    @Bean
    public PostInitData postInitData(PostRepository postRepository, UserRepository userRepository) {
        return new PostInitData(postRepository, userRepository);
    }

}
