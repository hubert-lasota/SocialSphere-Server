package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.post.response.PostCommentResponse;
import org.hl.socialspherebackend.api.dto.post.response.PostResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserProfileResponse;
import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.post.PostComment;
import org.hl.socialspherebackend.api.entity.post.PostImage;

import java.util.Set;
import java.util.stream.Collectors;

class PostMapper {

    private PostMapper() { }


    public static PostResponse fromPostEntityToResponse(Post entity, Set<byte[]> postImages, UserProfileResponse userProfileResponse, boolean isLiked) {
        return new PostResponse(
                entity.getId(),
                entity.getUser().getId(),
                userProfileResponse,
                entity.getContent(),
                postImages,
                entity.getLikeCount(),
                entity.getCommentCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                isLiked
        );
    }

    public static PostCommentResponse fromPostCommentEntityToResponse(PostComment entity, UserProfileResponse userProfileResponse) {
        return new PostCommentResponse(
                entity.getId(),
                entity.getPost().getId(),
                entity.getCommentAuthor().getId(),
                userProfileResponse,
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }


    public static Set<PostImage> fromRequestToPostImageEntities(Set<byte[]> images) {
        return images.stream()
                .map(image -> {
                    PostImage postImage = new PostImage();
                    postImage.setImage(image);
                    return postImage;
                })
                .collect(Collectors.toSet());
    }

}
