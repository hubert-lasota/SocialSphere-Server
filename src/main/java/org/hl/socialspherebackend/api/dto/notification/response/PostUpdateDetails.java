package org.hl.socialspherebackend.api.dto.notification.response;

import org.hl.socialspherebackend.api.dto.post.response.PostResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserWrapperResponse;
import org.hl.socialspherebackend.api.entity.notification.PostUpdateType;

public record PostUpdateDetails(PostResponse updatedPost,
                                PostUpdateType updateType,
                                UserWrapperResponse updatedBy) { }

