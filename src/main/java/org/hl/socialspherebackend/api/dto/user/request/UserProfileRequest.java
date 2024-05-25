package org.hl.socialspherebackend.api.dto.user.request;

public record UserProfileRequest(String firstName,
                                 String lastName,
                                 String city,
                                 String country) {
}
