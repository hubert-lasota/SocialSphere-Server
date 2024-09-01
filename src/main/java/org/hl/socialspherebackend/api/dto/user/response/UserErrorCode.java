package org.hl.socialspherebackend.api.dto.user.response;

public enum UserErrorCode {
    USER_NOT_FOUND,
    USERS_NOT_FOUND,
    USER_PROFILE_NOT_FOUND,
    USER_PROFILE_PICTURE_NOT_FOUND,
    USER_PROFILE_CONFIG_NOT_FOUND,
    SEARCH_USERS_NOT_FOUND,
    USER_HAS_NO_FRIENDS,
    USER_PROFILE_ALREADY_EXISTS,
    USER_PROFILE_CONFIG_ALREADY_EXISTS,
    FRIEND_REQUEST_NOT_FOUND,
    RECEIVER_NOT_FOUND,
    SENDER_NOT_FOUND,
    SENDER_ALREADY_SENT_FRIEND_REQUEST,
    USER_PROFILE_ACCESS_NOT_ALLOWED,
}
