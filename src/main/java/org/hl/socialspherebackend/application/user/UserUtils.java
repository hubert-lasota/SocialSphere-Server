package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.user.response.RelationshipStatus;
import org.hl.socialspherebackend.api.entity.user.User;

class UserUtils {

    private UserUtils() { }

    static RelationshipStatus getRelationshipStatusFromUser(User currentUser, User user) {
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
