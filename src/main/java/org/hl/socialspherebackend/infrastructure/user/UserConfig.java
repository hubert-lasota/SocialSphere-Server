package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
import org.hl.socialspherebackend.application.user.UserFacade;
import org.hl.socialspherebackend.application.user.UserProfilePermissionChecker;
import org.hl.socialspherebackend.application.user.UserProfileRequestValidator;
import org.hl.socialspherebackend.application.user.UserValidateResult;
import org.hl.socialspherebackend.application.validator.RequestValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

@Configuration
public class UserConfig {

    @Bean
    public UserFacade userFacade(UserRepository userRepository,
                                 RequestValidator<UserProfileRequest, UserValidateResult> userProfileValidator,
                                 UserProfilePermissionChecker profilePermissionChecker,
                                 Clock clock) {
        return new UserFacade(userRepository, profilePermissionChecker, userProfileValidator, clock);
    }

    @Bean
    public UserProfileRequestValidator userValidator() {
        return new UserProfileRequestValidator();
    }

    @Bean
    public UserProfilePermissionChecker userProfilePermissionChecker() {
        return new UserProfilePermissionChecker();
    }

    @Profile("dev")
    @Bean
    public UserInitData userInitData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return new UserInitData(userRepository, passwordEncoder);
    }

}
