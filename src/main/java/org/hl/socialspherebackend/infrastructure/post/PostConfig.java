package org.hl.socialspherebackend.infrastructure.post;

import org.hl.socialspherebackend.api.dto.notification.response.PostUpdateDetails;
import org.hl.socialspherebackend.application.pattern.behavioral.Observer;
import org.hl.socialspherebackend.application.post.PostCommentValidator;
import org.hl.socialspherebackend.application.post.PostFacade;
import org.hl.socialspherebackend.application.post.PostValidator;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Set;

@Configuration
public class PostConfig {

    @Bean
    public PostFacade postFacade(PostRepository postRepository,
                                 PostCommentRepository postCommentRepository,
                                 PostValidator postValidator,
                                 PostCommentValidator postCommentValidator,
                                 UserRepository userRepository,
                                 Set<Observer<PostUpdateDetails>> observers) {

        return new PostFacade(postRepository, postCommentRepository, postValidator, postCommentValidator,userRepository, observers);
    }

    @Bean
    public PostValidator postValidator() {
        return new PostValidator();
    }

    @Bean
    PostCommentValidator postCommentValidator() {
        return new PostCommentValidator();
    }

    @Profile("dev")
    @Bean
    public PostInitData postInitData(PostRepository postRepository, UserRepository userRepository) {
        return new PostInitData(postRepository, userRepository);
    }

}
