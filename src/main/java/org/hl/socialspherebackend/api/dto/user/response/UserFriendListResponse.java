package org.hl.socialspherebackend.api.dto.user.response;

import java.util.List;

public record UserFriendListResponse(List<UserFriendResponse> friends) {
}
