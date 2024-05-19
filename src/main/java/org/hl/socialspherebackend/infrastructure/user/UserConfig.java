package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.application.user.UserFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {

    @Bean
    public UserFacade userFacade(UserRepository userRepository) {
        return new UserFacade(userRepository);
    }



}
