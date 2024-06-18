package org.hl.socialspherebackend.infrastructure.post;

import org.hl.socialspherebackend.application.post.PostFacade;
import org.hl.socialspherebackend.application.user.UserFacade;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PostConfig {

    @Bean
    public PostFacade postFacade(UserRepository userRepository, PostRepository postRepository) {
        return new PostFacade(userRepository, postRepository);
    }

    @Profile("dev")
    @Bean
    public PostInitData postInitData(PostFacade postFacade, UserFacade userFacade) {
        return new PostInitData(postFacade, userFacade);
    }

}
