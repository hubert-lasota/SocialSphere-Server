package org.hl.socialspherebackend.api.dto.user.response;

public record UserHeaderResponse(Long userId,
                                 String firstName,
                                 String lastName,
                                 byte[] profilePicture,
                                 RelationshipStatus relationshipStatus) {
}
