package org.hl.socialspherebackend.infrastructure.security.authorization;

import org.hl.socialspherebackend.api.dto.authorization.request.LoginRequest;
import org.hl.socialspherebackend.api.dto.authorization.request.UsersTokenRequest;
import org.hl.socialspherebackend.api.dto.authorization.response.LoginResult;
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
    public ResponseEntity<LoginResult> createLogin(@RequestBody LoginRequest request) {
        LoginResult response = authorizationFacade.createLogin(request);

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResult> login(@RequestBody LoginRequest request) {
        LoginResult response = authorizationFacade.login(request);

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<LoginResult> validateUsersToken(@RequestBody UsersTokenRequest request) {
        LoginResult response = authorizationFacade.validateUsersToken(request);

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }



}
