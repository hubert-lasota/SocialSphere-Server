package org.hl.socialspherebackend.api.dto.user.request;

public record SearchFriendsRequest(
        String firstNamePattern,
        String lastNamePattern,
        String cityPattern,
        String countryPattern,
        SearchFriendsRelationshipStatus relationshipStatus
) {
}
