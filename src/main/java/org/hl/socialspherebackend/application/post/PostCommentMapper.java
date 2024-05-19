package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.post.response.PostCommentResponse;
import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.post.PostComment;
import org.hl.socialspherebackend.api.entity.user.User;

public class PostCommentMapper {

    private PostCommentMapper() { }

    public static PostComment fromRequestToEntity(Post post, User commentAuthor, String content) {
        PostComment postComment = new PostComment();
        postComment.setPost(post);
        postComment.setCommentAuthor(commentAuthor);
        postComment.setContent(content);
        return postComment;
    }

    public static PostCommentResponse fromEntityToResponse(PostComment entity) {
        return new PostCommentResponse(
                entity.getId(),
                entity.getPost().getId(),
                entity.getCommentAuthor().getId(),
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

}
