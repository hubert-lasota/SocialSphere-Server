package org.hl.socialspherebackend.application.post.mapper;

import org.hl.socialspherebackend.api.dto.post.request.PostRequest;
import org.hl.socialspherebackend.api.dto.post.response.PostResponse;
import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.post.PostImage;

import java.util.Set;
import java.util.stream.Collectors;

public class PostMapper {

    private PostMapper() { }

    public static Post fromRequestToEntity(PostRequest request) {
        Post post = new Post();
        post.setContent(request.content());
        return post;
    }

    public static PostResponse fromEntityToResponse(Post entity) {
        Set<byte[]> images = entity.getImages()
                .stream()
                .map(PostImage::getImage)
                .collect(Collectors.toSet());

        return new PostResponse(
                entity.getId(),
                entity.getContent(),
                images,
                entity.getLikeCount(),
                entity.getCommentCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }


    public static Set<PostImage> fromRequestToEntities(Set<byte[]> images) {
        return images.stream()
                .map(image -> {
                    PostImage postImage = new PostImage();
                    postImage.setImage(image);
                    return postImage;
                })
                .collect(Collectors.toSet());
    }
}
