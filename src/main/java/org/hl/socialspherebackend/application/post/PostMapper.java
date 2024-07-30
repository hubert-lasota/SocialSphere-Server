package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.post.response.PostCommentResponse;
import org.hl.socialspherebackend.api.dto.post.response.PostResponse;
import org.hl.socialspherebackend.api.dto.user.response.UserProfileResponse;
import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.post.PostComment;
import org.hl.socialspherebackend.api.entity.post.PostImage;
import org.hl.socialspherebackend.api.entity.user.UserProfile;
import org.hl.socialspherebackend.application.user.UserMapper;
import org.hl.socialspherebackend.application.util.FileUtils;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class PostMapper {

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
        return UserMapper.fromEntityToResponse(userProfile);
    }

    private static UserProfileResponse getUserProfileResponse(PostComment postComment) {
        UserProfile userProfile = postComment.getCommentAuthor().getUserProfile();
        return UserMapper.fromEntityToResponse(userProfile);
    }

    private static Set<byte[]> decompressPostImages(Post post) {
        return post.getImages()
                .stream()
                .map(PostImage::getImage)
                .map(FileUtils::decompressFile)
                .collect(toSet());
    }

}
