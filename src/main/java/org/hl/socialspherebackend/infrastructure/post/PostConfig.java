package org.hl.socialspherebackend.infrastructure.post;

import org.hl.socialspherebackend.application.post.PostFacade;
import org.hl.socialspherebackend.application.user.UserFacade;
import org.hl.socialspherebackend.infrastructure.post.repository.PostCommentRepository;
import org.hl.socialspherebackend.infrastructure.post.repository.PostImageRepository;
import org.hl.socialspherebackend.infrastructure.post.repository.PostRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PostConfig {

    @Bean
    public PostFacade postFacade(
            PostRepository postRepository,
            PostCommentRepository postCommentRepository,
            PostImageRepository postImageRepository,
            UserFacade userFacade
    ) {
        return new PostFacade(postRepository, postCommentRepository, postImageRepository, userFacade);
    }

}
