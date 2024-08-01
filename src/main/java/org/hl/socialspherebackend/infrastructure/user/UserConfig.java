package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.application.user.UserFacade;
import org.hl.socialspherebackend.application.user.UserProfileValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserConfig {

    @Bean
    public UserFacade userFacade(UserRepository userRepository, UserProfileValidator userProfileValidator) {
        return new UserFacade(userRepository, userProfileValidator);
    }

    @Bean
    public UserProfileValidator userValidator() {
        return new UserProfileValidator();
    }

    @Profile("dev")
    @Bean
    public UserInitData userInitData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return new UserInitData(userRepository, passwordEncoder);
    }

}
