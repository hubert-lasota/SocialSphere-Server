package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.post.response.PostCommentResponse;
import org.hl.socialspherebackend.api.dto.post.response.PostResponse;
import org.hl.socialspherebackend.api.dto.post.response.PostUpdateDetails;
import org.hl.socialspherebackend.api.dto.post.response.PostUpdateNotificationResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserProfileResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserWrapperResponse;
import org.hl.socialspherebackend.api.entity.post.*;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.api.entity.user.UserProfile;
import org.hl.socialspherebackend.application.user.UserMapper;
import org.hl.socialspherebackend.application.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class PostMapper {

    private static Logger log = LoggerFactory.getLogger(PostMapper.class);

    private PostMapper() { }


    public static PostResponse fromEntityToResponse(Post post, Boolean isLiked) {
        Set<byte[]> decompressedPostImages = decompressPostImages(post);
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

    public static PostCommentResponse fromEntityToResponse(PostComment postComment) {
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

    public static PostUpdateNotificationResponse fromEntityToResponse(PostUpdateNotification postUpdateNotification) {
        Post updatedPost = postUpdateNotification.getUpdatedPost();
        PostUpdateType postUpdateType = postUpdateNotification.getUpdateType();
        User updatedBy = postUpdateNotification.getUpdatedBy();
        Instant updatedAt = postUpdateNotification.getUpdatedAt();
        PostUpdateDetails postUpdateDetails = fromEntitiesToResponse(updatedPost, postUpdateType, updatedBy, updatedAt);
        boolean checked = postUpdateNotification.getIsChecked();
        Long id = postUpdateNotification.getId();
        return new PostUpdateNotificationResponse(id, postUpdateDetails, checked);
    }

    public static PostUpdateDetails fromEntitiesToResponse(Post updatedPost,
                                                           PostUpdateType updateType,
                                                           User updatedBy,
                                                           Instant updatedAt) {

        PostResponse postResponse = PostMapper.fromEntityToResponse(updatedPost, null);
        UserWrapperResponse userWrapperResponse = UserMapper.fromEntityToUserWrapperResponse(updatedBy);

        return new PostUpdateDetails(postResponse, updateType, userWrapperResponse, updatedAt);
    }

    public static Set<PostImage> fromRequestToEntities(Set<byte[]> postImages) {
        return postImages.stream()
                .map(image -> {
                    PostImage postImage = new PostImage();
                    postImage.setImage(image);
                    return postImage;
                })
                .collect(Collectors.toSet());
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

    private static Set<byte[]> decompressPostImages(Post post) {
        if(post.getImages() == null || post.getImages().isEmpty()) {
            log.debug("Post with id = {} has no images", post.getId());
            return null;
        }

        return post.getImages()
                .stream()
                .map(PostImage::getImage)
                .map(FileUtils::decompressFile)
                .collect(toSet());
    }

}
