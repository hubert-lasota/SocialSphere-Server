package org.hl.socialspherebackend.api.dto.user.response;

import java.util.Set;

public record UserFriendSetResponse(Set<UserFriendResponse> friends) {
}
