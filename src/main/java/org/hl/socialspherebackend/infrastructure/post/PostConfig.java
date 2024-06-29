package org.hl.socialspherebackend.infrastructure.post;

import org.hl.socialspherebackend.application.post.PostFacade;
import org.hl.socialspherebackend.application.user.UserFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PostConfig {

    @Bean
    public PostFacade postFacade(PostRepository postRepository, UserFacade userFacade) {
        return new PostFacade(postRepository, userFacade);
    }

    @Profile("dev")
    @Bean
    public PostInitData postInitData(PostFacade postFacade, UserFacade userFacade) {
        return new PostInitData(postFacade, userFacade);
    }

}
