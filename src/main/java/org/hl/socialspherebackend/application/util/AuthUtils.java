package org.hl.socialspherebackend.application.util;

import org.hl.socialspherebackend.api.entity.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;


public class AuthUtils {

    private static final Logger log = LoggerFactory.getLogger(AuthUtils.class);

    public static Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            User user = (User) authentication.getPrincipal();
            return Optional.of(user);
        } catch (NullPointerException e) {
            log.debug("Error occurred while getting authentication object from SecurityContextHolder. Error message: {}"
                    , e.getMessage());
            return Optional.empty();

        } catch (ClassCastException e) {
            log.debug("Error occurred casting principal to User. Error message: {}", e.getMessage());
            return Optional.empty();
        }

    }

}
