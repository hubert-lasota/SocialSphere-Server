package org.hl.socialspherebackend.infrastructure.init_dev_data;

import org.hl.socialspherebackend.infrastructure.chat.ChatMessageRepository;
import org.hl.socialspherebackend.infrastructure.chat.ChatRepository;
import org.hl.socialspherebackend.infrastructure.post.PostCommentRepository;
import org.hl.socialspherebackend.infrastructure.post.PostRepository;
import org.hl.socialspherebackend.infrastructure.post.PostUpdateNotificationRepository;
import org.hl.socialspherebackend.infrastructure.user.UserFriendRequestRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class InitDataConfig {

    @Bean
    UserInitData userInitData(UserRepository userRepository, UserFriendRequestRepository userFriendRequestRepository, PasswordEncoder passwordEncoder) {
        return new UserInitData(userRepository, userFriendRequestRepository, passwordEncoder);
    }

    @Bean
    PostInitData postInitData(PostRepository postRepository, PostCommentRepository postCommentRepository, PostUpdateNotificationRepository postUpdateNotificationRepository, UserRepository userRepository) {
        return new PostInitData(postRepository, postCommentRepository, postUpdateNotificationRepository, userRepository);
    }

    @Bean
    ChatInitData chatInitData(ChatRepository chatRepository, ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        return new ChatInitData(chatRepository, chatMessageRepository, userRepository);
    }

    @Bean
    InitData initData(UserInitData userInitData, PostInitData postInitData, ChatInitData chatInitData) {
        return new InitData(userInitData, postInitData, chatInitData);
    }

}
