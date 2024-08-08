package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.dto.post.response.PostErrorCode;
import org.hl.socialspherebackend.api.dto.post.response.PostUpdateDetails;
import org.hl.socialspherebackend.api.dto.post.response.PostUpdateNotificationResponse;
import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.post.PostUpdateNotification;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.infrastructure.post.PostRepository;
import org.hl.socialspherebackend.infrastructure.post.PostUpdateNotificationRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class PostNotificationFacade {

    private final static Logger log = LoggerFactory.getLogger(PostNotificationFacade.class);

    private final PostUpdateNotificationRepository notificationRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostNotificationFacade(PostUpdateNotificationRepository notificationRepository,
                                  PostRepository postRepository,
                                  UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public PostUpdateNotification savePostUpdateNotification(PostUpdateDetails postUpdateDetails) {
        Long postId = postUpdateDetails.updatedPost().id();
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            log.debug("There is no post with id {} in database", postId);
            return null;
        }
        Post post = postOpt.get();

        Long userId = postUpdateDetails.updatedBy().user().id();
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.debug("There is no user with id {} in database", userId);
            return null;
        }
        User user = userOpt.get();

        PostUpdateNotification postUpdateNotification =
                new PostUpdateNotification(post, user, postUpdateDetails.updateType(), Instant.now(), false);
        return notificationRepository.save(postUpdateNotification);
    }


    public DataResult<Set<PostUpdateNotificationResponse>, PostErrorCode> findPostUpdateNotifications(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.USER_NOT_FOUND,
                    "User with id = %d does not exists in database".formatted(userId));
        }
        User user = userOpt.get();

        if (!postRepository.existsPostsByUser(user)) {
            return DataResult.failure(PostErrorCode.USER_HAS_NO_POST,
                    "User with id = %d never created post".formatted(userId));
        }

        List<PostUpdateNotification> notifications =
                notificationRepository.findPostUpdateNotificationsByUserId(user.getId());

        if(notifications.isEmpty()) {
            return DataResult.failure(PostErrorCode.USER_HAS_NO_POST_NOTIFICATION,
                    "There is no post notification in database for user with id = %d ".formatted(userId));
        }

        Set<PostUpdateNotificationResponse> response = notifications.stream()
                .map(PostMapper::fromEntityToResponse)
                .collect(toSet());
        return DataResult.success(response);
    }


}
