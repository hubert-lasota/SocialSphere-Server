package org.hl.socialspherebackend.api.dto.post.request;

import java.util.Set;

public record PostRequest(String content, Set<byte[]> images) {

}