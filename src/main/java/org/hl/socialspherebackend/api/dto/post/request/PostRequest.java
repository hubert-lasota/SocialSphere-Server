package org.hl.socialspherebackend.api.dto.post.request;

import java.util.Set;

public record PostRequest(Long userId, String content, Set<byte[]> images) {

}