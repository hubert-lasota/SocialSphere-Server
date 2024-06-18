package org.hl.socialspherebackend.infrastructure.security.authorization;

import org.hl.socialspherebackend.application.authorization.AuthorizationFacade;
import org.hl.socialspherebackend.application.user.UserFacade;
import org.hl.socialspherebackend.infrastructure.security.jwt.JwtFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthorizationConfig {

    @Bean
    public AuthorizationFacade authorizationFacade(UserFacade userFacade,
                                                   JwtFacade jwtFacade,
                                                   AuthenticationManager authenticationManager,
                                                   PasswordEncoder passwordEncoder) {
        return new AuthorizationFacade(userFacade, jwtFacade, authenticationManager, passwordEncoder);
    }

}
