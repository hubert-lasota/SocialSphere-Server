package org.hl.socialspherebackend.api.dto.chat.response;

public enum ChatErrorCode {
    CHAT_NOT_FOUND,
    CHAT_MESSAGES_NOT_FOUND,
    SENDER_NOT_FOUND,
    RECEIVER_NOT_FOUND,
    USER_NOT_FOUND,
    USER_HAS_NO_CHATS,
    USERS_ALREADY_HAVE_CHAT,
    SERVER_ERROR,
    NO_NEW_MESSAGE
}
