package org.hl.socialspherebackend.infrastructure.security.authorization;

import org.hl.socialspherebackend.api.dto.authorization.request.LoginRequest;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.application.authorization.AuthorizationFacade;
import org.hl.socialspherebackend.application.authorization.AuthorizationRequestValidator;
import org.hl.socialspherebackend.application.authorization.AuthorizationValidateResult;
import org.hl.socialspherebackend.application.validator.RequestValidator;
import org.hl.socialspherebackend.infrastructure.security.jwt.JwtFacade;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
public class AuthorizationConfig {

    @Bean
    public AuthorizationFacade authorizationFacade(UserRepository userRepository,
                                                   RequestValidator<LoginRequest, AuthorizationValidateResult> authorizationValidator,
                                                   JwtFacade jwtFacade,
                                                   AuthenticationManager authenticationManager,
                                                   PasswordEncoder passwordEncoder) {
        return new AuthorizationFacade(userRepository, authorizationValidator, jwtFacade, authenticationManager, passwordEncoder);
    }

    @Bean
    public AuthorizationRequestValidator authorizationValidator() {
        return new AuthorizationRequestValidator();
    }

    @Bean
    public UserDetailsService userDetailsService(final UserRepository userRepository) {
        return username -> {
            Optional<User> user = userRepository.findByUsername(username);
            if(user.isPresent()) {
                return user.get();
            }
            throw new UsernameNotFoundException("Invalid credentials");
        };
    }

}
