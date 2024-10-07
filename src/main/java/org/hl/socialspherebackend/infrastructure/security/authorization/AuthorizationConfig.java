package org.hl.socialspherebackend.infrastructure.security.authorization;

import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.application.authorization.AuthorizationFacade;
import org.hl.socialspherebackend.application.validator.RequestValidatorChain;
import org.hl.socialspherebackend.infrastructure.security.jwt.JwtFacade;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class AuthorizationConfig {

    @Bean
    public AuthorizationFacade authorizationFacade(UserRepository userRepository,
                                                   RequestValidatorChain requestValidatorChain,
                                                   JwtFacade jwtFacade,
                                                   AuthenticationManager authenticationManager,
                                                   PasswordEncoder passwordEncoder,
                                                   Clock clock) {
        return new AuthorizationFacade(userRepository, requestValidatorChain, jwtFacade, authenticationManager, passwordEncoder, clock);
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
