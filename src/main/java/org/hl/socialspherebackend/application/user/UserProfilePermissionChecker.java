package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.user.response.RelationshipStatus;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.api.entity.user.UserProfileConfig;
import org.hl.socialspherebackend.api.entity.user.UserProfilePrivacyLevel;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;

public class UserProfilePermissionChecker {

    private final UserRepository userRepository;

    public UserProfilePermissionChecker(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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

        RelationshipStatus relationshipStatus = getRelationshipStatusFromUser(currentUser, userToCheck);

        if(userToCheckPrivacyLevel.equals(UserProfilePrivacyLevel.FRIENDS) && !relationshipStatus.equals(RelationshipStatus.FRIEND)) {
            return new UserPermissionCheckResult(false,
                    "User with id = %d have profile access only for friends!".formatted(userToCheck.getId()));
        }

        return new UserPermissionCheckResult(true, null);
    }

    private RelationshipStatus getRelationshipStatusFromUser(User currentUser, User user) {
        if(user.equals(currentUser)) {
            return RelationshipStatus.YOU;
        }


        RelationshipStatus relationshipStatus;
        boolean isFriend = userRepository.findUserFriends(currentUser.getId())
                .stream()
                .anyMatch(u -> u.equals(user));

        if(isFriend) {
            relationshipStatus = RelationshipStatus.FRIEND;
        } else {
            relationshipStatus = RelationshipStatus.STRANGER;
        }

        return relationshipStatus;
    }

}
