package org.hl.socialspherebackend.api.dto.notification.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class PostUpdateNotificationResult {

    @JsonProperty
    private final Set<PostUpdateNotificationResponse> postNotifications;

    @JsonProperty
    private final NotificationErrorCode code;

    @JsonProperty
    private final String message;

    private PostUpdateNotificationResult(Set<PostUpdateNotificationResponse> postNotifications,
                                         NotificationErrorCode code,
                                         String message) {
        this.postNotifications = postNotifications;
        this.code = code;
        this.message = message;
    }


    public static PostUpdateNotificationResult success(Set<PostUpdateNotificationResponse> postNotifications) {
        return new PostUpdateNotificationResult(postNotifications, null, null);
    }

    public static PostUpdateNotificationResult failure(NotificationErrorCode code, String message) {
        return new PostUpdateNotificationResult(null, code, message);
    }

    public boolean isSuccess() {
        return postNotifications != null && !postNotifications.isEmpty();
    }

}
