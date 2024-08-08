package org.hl.socialspherebackend.infrastructure.post;

import org.hl.socialspherebackend.api.dto.post.request.PostCommentRequest;
import org.hl.socialspherebackend.api.dto.post.request.PostRequest;
import org.hl.socialspherebackend.api.dto.post.response.PostUpdateDetails;
import org.hl.socialspherebackend.application.pattern.behavioral.Observer;
import org.hl.socialspherebackend.application.post.*;
import org.hl.socialspherebackend.application.user.UserProfilePermissionChecker;
import org.hl.socialspherebackend.application.validator.RequestValidator;
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
                                 RequestValidator<PostRequest, PostValidateResult> postValidator,
                                 RequestValidator<PostCommentRequest, PostValidateResult> postCommentValidator,
                                 Set<Observer<PostUpdateDetails>> observers,
                                 Clock clock) {

        return new PostFacade(postRepository, postCommentRepository, userRepository, permissionChecker, postValidator, postCommentValidator, observers, clock);
    }

    @Bean
    public PostRequestValidator postValidator() {
        return new PostRequestValidator();
    }

    @Bean
    public PostCommentRequestValidator postCommentValidator() {
        return new PostCommentRequestValidator();
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
