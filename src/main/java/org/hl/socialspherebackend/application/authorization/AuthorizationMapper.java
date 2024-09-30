package org.hl.socialspherebackend.application.authorization;

import org.hl.socialspherebackend.api.dto.authorization.request.LoginRequest;
import org.hl.socialspherebackend.api.dto.authorization.response.LoginResponse;
import org.hl.socialspherebackend.api.entity.user.User;

import java.time.Instant;

class AuthorizationMapper {

    private AuthorizationMapper() { }

    static User fromRequestToEntity(LoginRequest request, Instant createdAt) {
        return new User(request.username(), request.password(), createdAt);
    }

    static LoginResponse fromEntityToResponse(User user, String jwt) {
        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                jwt
        );
    }

}
