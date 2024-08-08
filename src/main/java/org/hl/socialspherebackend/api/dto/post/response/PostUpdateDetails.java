package org.hl.socialspherebackend.api.dto.post.response;

import org.hl.socialspherebackend.api.dto.user.response.UserWrapperResponse;
import org.hl.socialspherebackend.api.entity.post.PostUpdateType;

import java.time.Instant;

public record PostUpdateDetails(PostResponse updatedPost,
                                PostUpdateType updateType,
                                UserWrapperResponse updatedBy,
                                Instant updatedAt) { }

