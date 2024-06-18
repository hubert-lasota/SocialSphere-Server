package org.hl.socialspherebackend.api.dto.authorization.request;

public record UserTokenRequest(String username, String jwt) {
}
