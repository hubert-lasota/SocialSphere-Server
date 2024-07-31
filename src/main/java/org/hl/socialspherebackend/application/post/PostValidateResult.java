package org.hl.socialspherebackend.application.post;

import org.hl.socialspherebackend.api.dto.post.response.PostErrorCode;

record PostValidateResult(boolean isValid, PostErrorCode code, String message) {}
