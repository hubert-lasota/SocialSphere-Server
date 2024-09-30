package org.hl.socialspherebackend.infrastructure.user;

import org.hl.socialspherebackend.api.dto.user.response.UserFriendRequestResponse;
import org.hl.socialspherebackend.application.common.Observer;
import org.hl.socialspherebackend.application.user.UserFacade;
import org.hl.socialspherebackend.application.user.UserFriendRequestNotificationManager;
import org.hl.socialspherebackend.application.user.UserProfilePermissionChecker;
import org.hl.socialspherebackend.application.validator.RequestValidatorChain;
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
                                 UserFriendRequestRepository userFriendRequestRepository,
                                 RequestValidatorChain requestValidatorChain,
                                 UserProfilePermissionChecker profilePermissionChecker,
                                 Clock clock,
                                 Set<Observer<UserFriendRequestResponse>> observers) {
        return new UserFacade(userRepository, userFriendRequestRepository, profilePermissionChecker, requestValidatorChain, clock, observers);
    }

    @Bean
    public UserProfilePermissionChecker userProfilePermissionChecker(UserRepository userRepository) {
        return new UserProfilePermissionChecker(userRepository);
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
