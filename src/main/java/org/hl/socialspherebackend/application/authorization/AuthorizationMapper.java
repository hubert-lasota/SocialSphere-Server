package org.hl.socialspherebackend.application.authorization;

import org.hl.socialspherebackend.api.dto.authorization.request.LoginRequest;
import org.hl.socialspherebackend.api.dto.authorization.response.LoginResponse;
import org.hl.socialspherebackend.api.entity.user.User;

public class AuthorizationMapper {

    private AuthorizationMapper() { }

    public static User fromRequestToEntity(LoginRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(request.password());
        return user;
    }


    public static LoginResponse fromEntityToResponse(User user, String jwt) {
        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                jwt
        );
    }


}
