package org.hl.socialspherebackend.api.dto.common;

public record FileDetails(
        String name,
        String type,
        byte[] content
) { }
