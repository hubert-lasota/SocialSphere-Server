package org.hl.socialspherebackend.api.dto.user.response;

public record UserProfileResponse(String firstName,
                                  String lastName,
                                  String city,
                                  String country) {
}
