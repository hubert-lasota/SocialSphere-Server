package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.application.user.UserFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserConfig {

    @Bean
    public UserFacade userFacade(UserRepository userRepository) {
        return new UserFacade(userRepository);
    }

    @Profile("dev")
    @Bean
    public UserInitData userInitData(UserFacade userFacade, PasswordEncoder passwordEncoder) {
        return new UserInitData(userFacade, passwordEncoder);
    }
}
