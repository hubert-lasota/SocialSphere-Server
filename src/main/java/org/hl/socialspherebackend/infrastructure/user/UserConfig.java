package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
import org.hl.socialspherebackend.api.dto.user.response.UserFriendRequestResponse;
import org.hl.socialspherebackend.application.pattern.behavioral.Observer;
import org.hl.socialspherebackend.application.user.*;
import org.hl.socialspherebackend.application.validator.RequestValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.Set;

@Configuration
public class UserConfig {

    @Bean
    public UserFacade userFacade(UserRepository userRepository,
                                 RequestValidator<UserProfileRequest, UserValidateResult> userProfileValidator,
                                 UserProfilePermissionChecker profilePermissionChecker,
                                 Clock clock,
                                 Set<Observer<UserFriendRequestResponse>> observers) {
        return new UserFacade(userRepository, profilePermissionChecker, userProfileValidator, clock, observers);
    }

    @Bean
    public UserProfileRequestValidator userValidator() {
        return new UserProfileRequestValidator();
    }

    @Bean
    public UserProfilePermissionChecker userProfilePermissionChecker() {
        return new UserProfilePermissionChecker();
    }

    @Bean
    public UserFriendRequestNotificationManager userFriendRequestNotificationManager() {
        return new UserFriendRequestNotificationManager();
    }

    @Profile("dev")
    @Bean
    public UserInitData userInitData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return new UserInitData(userRepository, passwordEncoder);
    }

}
