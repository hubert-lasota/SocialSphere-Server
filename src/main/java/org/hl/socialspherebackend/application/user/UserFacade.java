package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

public class UserFacade implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserFacade.class);
    private final UserRepository userRepository;

    public UserFacade(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isPresent()) {
            log.info("load user: {}", user.get());
            return user.get();
        }
        log.debug("There is no user with username: \"{}\" in database!", username);
        throw new UsernameNotFoundException("Invalid credentials");
    }

}
