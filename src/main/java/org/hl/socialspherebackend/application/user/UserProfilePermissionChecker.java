package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.user.response.RelationshipStatus;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.api.entity.user.UserProfileConfig;
import org.hl.socialspherebackend.api.entity.user.UserProfilePrivacyLevel;
import org.hl.socialspherebackend.application.util.UserUtils;

public class UserProfilePermissionChecker {

    public UserPermissionCheckResult checkUserProfileResourceAccess(User currentUser, User userToCheck) {
        UserProfileConfig userToCheckConfig = userToCheck.getUserProfileConfig();
        if(userToCheckConfig == null) {
            return new UserPermissionCheckResult(false,
                    "User with id = %d does not have profile!".formatted(userToCheck.getId()));
        }

        UserProfilePrivacyLevel userToCheckPrivacyLevel = userToCheckConfig.getUserPrivacyLevel();
        if(userToCheckPrivacyLevel.equals(UserProfilePrivacyLevel.PRIVATE)) {
            return new UserPermissionCheckResult(false,
                    "You are not allowed to see profile because user with id = %d have private profile".formatted(userToCheck.getId()));
        }

        RelationshipStatus relationshipStatus = UserUtils.getRelationshipStatusFromUser(currentUser, userToCheck);

        if(userToCheckPrivacyLevel.equals(UserProfilePrivacyLevel.FRIENDS) && !relationshipStatus.equals(RelationshipStatus.FRIEND)) {
            return new UserPermissionCheckResult(false,
                    "User with id = %d have profile access only for friends!".formatted(userToCheck.getId()));
        }

        return new UserPermissionCheckResult(true, null);
    }

}
