package org.hl.socialspherebackend.application.authorization;

import org.hl.socialspherebackend.api.dto.authorization.request.LoginRequest;
import org.hl.socialspherebackend.api.dto.authorization.request.UserTokenRequest;
import org.hl.socialspherebackend.api.dto.authorization.response.AuthorizationErrorCode;
import org.hl.socialspherebackend.api.dto.authorization.response.LoginResponse;
import org.hl.socialspherebackend.api.dto.authorization.response.LoginResult;
import org.hl.socialspherebackend.api.entity.user.Authority;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.infrastructure.security.jwt.JwtFacade;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthorizationFacade {

    private final UserRepository userRepository;
    private final JwtFacade jwtFacade;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;


    public AuthorizationFacade(UserRepository userRepository,
                               JwtFacade jwtFacade,
                               AuthenticationManager authenticationManager,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtFacade = jwtFacade;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }


    public LoginResult createLogin(LoginRequest request) {
        if(userRepository.existsByUsername(request.username())) {
            return LoginResult.failure(AuthorizationErrorCode.USER_ALREADY_EXISTS,
                    "Username: %s exists in database!".formatted(request.username()));
        }
        LoginRequest encodedRequest = new LoginRequest(request.username(), passwordEncoder.encode(request.password()));
        User user = AuthorizationMapper.fromRequestToEntity(encodedRequest);
        Authority authority = new Authority(user, "USER");
        user.appendAuthority(authority);
        userRepository.save(user);

        String jwt = jwtFacade.generateToken(user);

        LoginResponse response = AuthorizationMapper.fromEntityToResponse(user, jwt);
        return LoginResult.success(response);
    }

    public LoginResult login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserDetails userDetails = null;
        if (authentication.isAuthenticated()) {
            try {
                userDetails = userRepository.findByUsername(request.username())
                        .orElseThrow(() -> new UsernameNotFoundException("There is no user with this username : %s".formatted(request.username())));
            } catch (UsernameNotFoundException e) {
                return LoginResult.failure(AuthorizationErrorCode.USERNAME_DOES_NOT_EXISTS,
                        "username: %s doesn't exists in database!".formatted(request.username()));
            }
        }

        String jwt = jwtFacade.generateToken(userDetails);
        LoginResponse response = AuthorizationMapper.fromEntityToResponse((User) userDetails, jwt);
        return LoginResult.success(response);
    }

    public LoginResult validateUserToken(UserTokenRequest request) {
        UserDetails userDetails = null;
        try {
            userDetails = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> new UsernameNotFoundException("There is no user with this username : %s".formatted(request.username())));
        } catch (UsernameNotFoundException e) {
            return LoginResult.failure(AuthorizationErrorCode.USERNAME_DOES_NOT_EXISTS,
                    "username: %s doesn't exists in database!".formatted(request.username()));
        }


        if(jwtFacade.validateToken(request.jwt(), userDetails)) {
            LoginResponse response = AuthorizationMapper.fromEntityToResponse((User) userDetails, request.jwt());
            return LoginResult.success(response);
        } else {
            return LoginResult.failure(AuthorizationErrorCode.NOT_VALID_USER, "token is not valid!");
        }
    }

    public LoginResult refreshUserToken(UserTokenRequest request) {
        if(validateUserToken(request).isFailure()) {
            return LoginResult.failure(AuthorizationErrorCode.NOT_VALID_USER_TOKEN, "UserToken is invalid");
        }

        UserDetails userDetails = userDetails = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("There is no user with this username : %s".formatted(request.username())));

        String jwt = jwtFacade.generateToken(userDetails);

        LoginResponse response = AuthorizationMapper.fromEntityToResponse((User) userDetails, jwt);
        return LoginResult.success(response);
    }

}
