package org.hl.socialspherebackend.api.dto.notification.response;

public enum NotificationErrorCode {
    NOT_SENT,
    SENDER_NOT_FOUND,
    RECEIVER_NOT_FOUND,
    FRIEND_REQUEST_NOT_FOUND,
    SENDER_ALREADY_SENT_FRIEND_REQUEST,
    USER_NOT_FOUND,
    USER_HAS_NO_POST,
    USER_HAS_NO_POST_NOTIFICATION,
}
