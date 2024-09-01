package org.hl.socialspherebackend.application.util;

import org.hl.socialspherebackend.api.dto.user.response.RelationshipStatus;
import org.hl.socialspherebackend.api.entity.user.User;

public class UserUtils {

    private UserUtils() { }

    public static RelationshipStatus getRelationshipStatusFromUser(User currentUser, User user) {
        RelationshipStatus relationshipStatus;

        boolean isFriend = currentUser.getFriends()
                .stream()
                .anyMatch(u -> u.equals(user));

        if(user.equals(currentUser)) {
            relationshipStatus = RelationshipStatus.YOU;
        } else if(isFriend) {
            relationshipStatus = RelationshipStatus.FRIEND;
        } else {
            relationshipStatus = RelationshipStatus.STRANGER;
        }

        return relationshipStatus;
    }

}
