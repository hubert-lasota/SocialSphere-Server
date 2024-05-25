package org.hl.socialspherebackend.application.user.mapper;

import org.hl.socialspherebackend.api.dto.user.response.UserResponse;
import org.hl.socialspherebackend.api.entity.user.User;

public class UserMapper {

    private UserMapper() { }


    public static UserResponse fromEntityToResponse(User entity) {
        return new UserResponse(
                entity.getId(),
                entity.getUsername()
        );
    }

}
