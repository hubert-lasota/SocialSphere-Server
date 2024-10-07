package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.common.DataResult;
import org.hl.socialspherebackend.api.dto.post.response.PostErrorCode;
import org.hl.socialspherebackend.api.dto.post.response.PostUpdateDetails;
import org.hl.socialspherebackend.api.dto.post.response.PostUpdateNotificationResponse;
import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.post.PostUpdateNotification;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.application.util.AuthUtils;
import org.hl.socialspherebackend.infrastructure.post.PostRepository;
import org.hl.socialspherebackend.infrastructure.post.PostUpdateNotificationRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

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

        Long userId = postUpdateDetails.updatedBy().userId();
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.debug("There is no user with id {} in database", userId);
            return null;
        }
        User user = userOpt.get();

        PostUpdateNotification postUpdateNotification =
                new PostUpdateNotification(post, user, postUpdateDetails.updateType(), postUpdateDetails.updatedAt(), false);
        return notificationRepository.save(postUpdateNotification);
    }


    public DataResult<Set<PostUpdateNotificationResponse>> findCurrentUserPostUpdateNotifications() {
        Optional<User> userOpt = AuthUtils.getCurrentUser();
        if (userOpt.isEmpty()) {
            return DataResult.failure(PostErrorCode.USER_NOT_FOUND, "Could not find current user");
        }
        User user = userOpt.get();

        if (!postRepository.existsPostsByUser(user)) {
            return DataResult.failure(PostErrorCode.USER_HAS_NO_POST,
                    "Current user never created post");
        }

        List<PostUpdateNotification> notifications =
                notificationRepository.findPostUpdateNotificationsByUserId(user.getId());

        if(notifications.isEmpty()) {
            return DataResult.failure(PostErrorCode.USER_HAS_NO_POST_NOTIFICATION,
                    "There is no post notification in database for current user");
        }

        Set<PostUpdateNotificationResponse> response = notifications.stream()
                .filter(notification -> !notification.getUpdatedBy().equals(user))
                .sorted(Comparator.comparing(PostUpdateNotification::getUpdatedAt))
                .map(PostMapper::fromEntityToResponse)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return DataResult.success(response);
    }


}
