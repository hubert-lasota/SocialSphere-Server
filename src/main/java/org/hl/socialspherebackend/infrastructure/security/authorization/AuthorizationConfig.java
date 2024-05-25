package org.hl.socialspherebackend.infrastructure.security.authorization;

import org.hl.socialspherebackend.application.authorization.AuthorizationFacade;
import org.hl.socialspherebackend.application.user.UserFacade;
import org.hl.socialspherebackend.infrastructure.security.jwt.JwtFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

@Configuration
public class AuthorizationConfig {

    @Bean
    public AuthorizationFacade authorizationFacade(UserFacade userFacade,
                                                   JwtFacade jwtFacade,
                                                   AuthenticationManager authenticationManager) {
        return new AuthorizationFacade(userFacade, jwtFacade, authenticationManager);
    }

}
