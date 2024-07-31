package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.application.user.UserFacade;
import org.hl.socialspherebackend.application.user.UserValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserConfig {

    @Bean
    public UserFacade userFacade(UserRepository userRepository, UserValidator userValidator) {
        return new UserFacade(userRepository, userValidator);
    }

    @Bean
    public UserValidator userValidator() {
        return new UserValidator();
    }

    @Profile("dev")
    @Bean
    public UserInitData userInitData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return new UserInitData(userRepository, passwordEncoder);
    }

}
