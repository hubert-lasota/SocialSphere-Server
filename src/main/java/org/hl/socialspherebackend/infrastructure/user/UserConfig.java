package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.application.user.UserFacade;
import org.hl.socialspherebackend.infrastructure.user.repository.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {

    @Bean
    public UserFacade userFacade(UserRepository userRepository,
                                 UserProfileConfigRepository userProfileConfigRepository,
                                 UserProfileRepository userProfileRepository,
                                 UserFriendRequestRepository userFriendRequestRepository,
                                 AuthorityRepository authorityRepository) {

        return new UserFacade(userRepository,
                userProfileConfigRepository,
                userProfileRepository,
                userFriendRequestRepository,
                authorityRepository);
    }



}
