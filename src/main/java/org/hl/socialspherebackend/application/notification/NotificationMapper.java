package org.hl.socialspherebackend.application.notification;

import org.hl.socialspherebackend.api.dto.notification.response.PostUpdateDetails;
import org.hl.socialspherebackend.api.dto.post.response.PostResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserProfileConfigResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserProfileResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserWrapperResponse;
import org.hl.socialspherebackend.api.entity.notification.PostUpdateType;
import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.application.post.PostMapper;
import org.hl.socialspherebackend.application.user.UserMapper;

public class NotificationMapper {

    private NotificationMapper() { }

    public static PostUpdateDetails fromEntitiesToResponse(Post updatedPost,
                                                           PostUpdateType updateType,
                                                           User updatedBy) {

        PostResponse postResponse = PostMapper.fromEntityToResponse(updatedPost, null);
        UserWrapperResponse userWrapperResponse = getUserWrapperResponse(updatedBy);

        return new PostUpdateDetails(postResponse, updateType, userWrapperResponse);
    }

    private static UserWrapperResponse getUserWrapperResponse(User user) {
        UserResponse userResponse = UserMapper.fromEntityToResponse(user, null);
        UserProfileResponse userProfileResponse = UserMapper.fromEntityToResponse(user.getUserProfile());
        UserProfileConfigResponse userProfileConfigResponse = UserMapper.fromEntityToResponse(user.getUserProfileConfig());

        return new UserWrapperResponse(userResponse, userProfileResponse, userProfileConfigResponse);
    }
}
