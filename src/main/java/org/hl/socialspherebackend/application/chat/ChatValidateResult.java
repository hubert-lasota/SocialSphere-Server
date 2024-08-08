package org.hl.socialspherebackend.application.chat;

import org.hl.socialspherebackend.api.dto.chat.response.ChatErrorCode;

public record ChatValidateResult(boolean isValid, ChatErrorCode code, String message) {
}
