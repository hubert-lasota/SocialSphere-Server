package org.hl.socialspherebackend.application.authorization;

import org.hl.socialspherebackend.api.dto.authorization.request.LoginRequest;
import org.hl.socialspherebackend.api.dto.authorization.request.UserTokenRequest;
import org.hl.socialspherebackend.api.dto.authorization.response.AuthorizationErrorCode;
import org.hl.socialspherebackend.api.dto.authorization.response.LoginResponse;
import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.entity.user.Authority;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.application.validator.RequestValidateResult;
import org.hl.socialspherebackend.application.validator.RequestValidatorChain;
import org.hl.socialspherebackend.infrastructure.security.jwt.JwtFacade;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;

public class AuthorizationFacade {

    private final UserRepository userRepository;
    private final RequestValidatorChain requestValidator;
    private final JwtFacade jwtFacade;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;


    public AuthorizationFacade(UserRepository userRepository,
                               RequestValidatorChain requestValidator,
                               JwtFacade jwtFacade,
                               AuthenticationManager authenticationManager,
                               PasswordEncoder passwordEncoder,
                               Clock clock) {
        this.userRepository = userRepository;
        this.requestValidator = requestValidator;
        this.jwtFacade = jwtFacade;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }


    public DataResult<LoginResponse> createLogin(LoginRequest request) {
        if(userRepository.existsByUsername(request.username())) {
            return DataResult.failure(AuthorizationErrorCode.USER_ALREADY_EXISTS,
                    "Username: %s exists in database!".formatted(request.username()),
                    HttpStatus.BAD_REQUEST);
        }
        LoginRequest encodedRequest = new LoginRequest(request.username(), passwordEncoder.encode(request.password()));

        RequestValidateResult validateResult = requestValidator.validate(request);
        if(!validateResult.valid()) {
            return DataResult.failure(validateResult.errorCode(), validateResult.errorMessage(), HttpStatus.BAD_REQUEST);
        }

        Instant now = Instant.now(clock);
        User user = AuthorizationMapper.fromRequestToEntity(encodedRequest, now);
        Authority authority = new Authority(user, "USER");
        user.appendAuthority(authority);
        userRepository.save(user);

        String jwt = jwtFacade.generateToken(user);

        LoginResponse response = AuthorizationMapper.fromEntityToResponse(user, jwt);
        return DataResult.success(response);
    }

    public DataResult<LoginResponse> login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User userDetails = null;
        if (authentication.isAuthenticated()) {
            try {
                userDetails = userRepository.findByUsername(request.username())
                        .orElseThrow(() -> new UsernameNotFoundException("There is no user with this username : %s".formatted(request.username())));
            } catch (UsernameNotFoundException e) {
                return DataResult.failure(AuthorizationErrorCode.USERNAME_DOES_NOT_EXISTS,
                        "username: %s doesn't exists in database!".formatted(request.username()),
                        HttpStatus.BAD_REQUEST);
            }
        } else {
            return DataResult.failure(AuthorizationErrorCode.USERNAME_DOES_NOT_EXISTS,
                    "username: %s doesn't exists in database!".formatted(request.username()),
                    HttpStatus.BAD_REQUEST);
        }


        String jwt = jwtFacade.generateToken(userDetails);
        userDetails.setOnline(true);
        Instant now = Instant.now(clock);
        userDetails.setLastOnlineAt(now);
        userRepository.save(userDetails);

        LoginResponse response = AuthorizationMapper.fromEntityToResponse(userDetails, jwt);
        return DataResult.success(response);
    }


    public DataResult<?> logout(UserTokenRequest request) {
        User userDetails = null;
        try {
            userDetails = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> new UsernameNotFoundException("There is no user with this username : %s".formatted(request.username())));
        } catch (UsernameNotFoundException e) {
            return DataResult.failure(AuthorizationErrorCode.USERNAME_DOES_NOT_EXISTS,
                    "username: %s doesn't exists in database!".formatted(request.username()));
        }

        userDetails.setOnline(false);
        userRepository.save(userDetails);
        return DataResult.success(null);
    }

    public DataResult<LoginResponse> validateUserToken(UserTokenRequest request) {
        UserDetails userDetails = null;
        try {
            userDetails = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> new UsernameNotFoundException("There is no user with this username : %s".formatted(request.username())));
        } catch (UsernameNotFoundException e) {
            return DataResult.failure(AuthorizationErrorCode.USERNAME_DOES_NOT_EXISTS,
                    "username: %s doesn't exists in database!".formatted(request.username()));
        }


        if(jwtFacade.validateToken(request.jwt(), userDetails)) {
            LoginResponse response = AuthorizationMapper.fromEntityToResponse((User) userDetails, request.jwt());
            return DataResult.success(response);
        } else {
            return DataResult.failure(AuthorizationErrorCode.NOT_VALID_USER,"Token is not valid!");
        }
    }

    public DataResult<LoginResponse> refreshUserToken(UserTokenRequest request) {
        if(validateUserToken(request).isFailure()) {
            return DataResult.failure(AuthorizationErrorCode.NOT_VALID_USER_TOKEN,
                    "UserToken is invalid", HttpStatus.BAD_REQUEST);
        }

        UserDetails userDetails = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("There is no user with this username : %s".formatted(request.username())));

        String jwt = jwtFacade.generateToken(userDetails);

        LoginResponse response = AuthorizationMapper.fromEntityToResponse((User) userDetails, jwt);
        return DataResult.success(response);
    }

}
