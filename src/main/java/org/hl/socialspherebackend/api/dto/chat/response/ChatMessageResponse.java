package org.hl.socialspherebackend.api.dto.chat.response;

import org.hl.socialspherebackend.api.dto.user.response.UserWrapperResponse;

import java.time.Instant;

public record ChatMessageResponse(Long chatId,
                                  UserWrapperResponse sender,
                                  Long messageId,
                                  String content,
                                  Instant sentAt) {

}
