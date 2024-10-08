package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.common.FileDetails;
import org.hl.socialspherebackend.api.dto.post.response.PostCommentResponse;
import org.hl.socialspherebackend.api.dto.post.response.PostResponse;
import org.hl.socialspherebackend.api.dto.post.response.PostUpdateDetails;
import org.hl.socialspherebackend.api.dto.post.response.PostUpdateNotificationResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserHeaderResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserProfileResponse;
import org.hl.socialspherebackend.api.entity.post.*;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.api.entity.user.UserProfile;
import org.hl.socialspherebackend.application.user.UserMapper;
import org.hl.socialspherebackend.application.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

class PostMapper {

    private static final Logger log = LoggerFactory.getLogger(PostMapper.class);

    private PostMapper() { }


    static PostResponse fromEntityToResponse(Post post, Boolean isLiked) {
        Set<FileDetails> decompressedPostImages = null;
        Set<PostImage> images = post.getImages();
        if(images != null && !images.isEmpty()) {
            decompressedPostImages = images.stream()
                    .map(PostMapper::fromEntityToResponse)
                    .collect(toSet());
        } else {
            log.debug("Post with id = {} has no images", post.getId());
        }
        UserProfileResponse userProfileResponse = getUserProfileResponse(post);

        return new PostResponse(
                post.getId(),
                post.getUser().getId(),
                userProfileResponse,
                post.getContent(),
                decompressedPostImages,
                post.getLikeCount(),
                post.getCommentCount(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                isLiked
        );
    }

    static PostCommentResponse fromEntityToResponse(PostComment postComment) {
        UserProfileResponse userProfileResponse = getUserProfileResponse(postComment);

        return new PostCommentResponse(
                postComment.getId(),
                postComment.getPost().getId(),
                postComment.getCommentAuthor().getId(),
                userProfileResponse,
                postComment.getContent(),
                postComment.getCreatedAt(),
                postComment.getUpdatedAt()
        );
    }

    static PostUpdateNotificationResponse fromEntityToResponse(PostUpdateNotification postUpdateNotification) {
        Post updatedPost = postUpdateNotification.getUpdatedPost();
        PostUpdateType postUpdateType = postUpdateNotification.getUpdateType();
        User updatedBy = postUpdateNotification.getUpdatedBy();
        Instant updatedAt = postUpdateNotification.getUpdatedAt();
        PostUpdateDetails postUpdateDetails = fromEntitiesToResponse(updatedPost, postUpdateType, updatedBy, updatedAt);
        boolean checked = postUpdateNotification.isChecked();
        Long id = postUpdateNotification.getId();
        return new PostUpdateNotificationResponse(id, postUpdateDetails, checked);
    }

    static FileDetails fromEntityToResponse(PostImage postImage) {
        String name = postImage.getName();
        String type = postImage.getType();
        byte[] content = FileUtils.decompressFile(postImage.getImage());
        return new FileDetails(name, type, content);
    }

    static PostUpdateDetails fromEntitiesToResponse(Post updatedPost,
                                                           PostUpdateType updateType,
                                                           User updatedBy,
                                                           Instant updatedAt) {

        PostResponse postResponse = PostMapper.fromEntityToResponse(updatedPost, null);
        UserHeaderResponse userHeaderResponse = UserMapper.fromEntityToUserHeaderResponse(updatedBy, null);

        return new PostUpdateDetails(postResponse, updateType, userHeaderResponse, updatedAt);
    }

    private static UserProfileResponse getUserProfileResponse(Post post) {
        UserProfile userProfile = post.getUser().getUserProfile();
        if(userProfile == null) {
            log.debug("User with id = {} has no profile", post.getUser().getId());
            return null;
        }
        return UserMapper.fromEntityToResponse(userProfile);
    }

    private static UserProfileResponse getUserProfileResponse(PostComment postComment) {
        UserProfile userProfile = postComment.getCommentAuthor().getUserProfile();
        if(userProfile == null) {
            log.debug("User with id = {} has no profile", postComment.getCommentAuthor().getId());
            return null;
        }
        return UserMapper.fromEntityToResponse(userProfile);
    }

}
