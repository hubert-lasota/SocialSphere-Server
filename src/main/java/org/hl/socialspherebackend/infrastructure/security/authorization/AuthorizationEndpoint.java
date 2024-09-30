package org.hl.socialspherebackend.infrastructure.security.authorization;

import org.hl.socialspherebackend.api.dto.authorization.request.LoginRequest;
import org.hl.socialspherebackend.api.dto.authorization.request.UserTokenRequest;
import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.application.authorization.AuthorizationFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthorizationEndpoint {

    private final AuthorizationFacade authorizationFacade;

    public AuthorizationEndpoint(AuthorizationFacade authorizationFacade) {
        this.authorizationFacade = authorizationFacade;
    }


    @PostMapping("/create")
    public ResponseEntity<?> createLogin(@RequestBody LoginRequest request) {
        DataResult<?> result = authorizationFacade.createLogin(request);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        DataResult<?> result = authorizationFacade.login(request);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateUserToken(@RequestBody UserTokenRequest request) {
        DataResult<?> result = authorizationFacade.validateUserToken(request);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshUserToken(@RequestBody UserTokenRequest request) {
        DataResult<?> result = authorizationFacade.refreshUserToken(request);

        return new ResponseEntity<>(result, result.getHttpStatus());
    }

}
