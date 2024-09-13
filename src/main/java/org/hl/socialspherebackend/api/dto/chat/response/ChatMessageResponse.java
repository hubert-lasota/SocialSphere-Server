package org.hl.socialspherebackend.api.dto.chat.response;

import org.hl.socialspherebackend.api.dto.user.response.UserHeaderResponse;

import java.time.Instant;

public record ChatMessageResponse(Long id,
                                  Long chatId,
                                  UserHeaderResponse sender,
                                  String content,
                                  Instant sentAt) {

}
