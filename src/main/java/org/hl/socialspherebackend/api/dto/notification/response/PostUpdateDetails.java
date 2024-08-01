package org.hl.socialspherebackend.api.dto.notification.response;

import org.hl.socialspherebackend.api.dto.post.response.PostResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserWrapperResponse;
import org.hl.socialspherebackend.api.entity.notification.PostUpdateType;

import java.time.Instant;

public record PostUpdateDetails(PostResponse updatedPost,
                                PostUpdateType updateType,
                                UserWrapperResponse updatedBy,
                                Instant updatedAt) { }

