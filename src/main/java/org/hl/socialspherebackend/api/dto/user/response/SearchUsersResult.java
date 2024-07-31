package org.hl.socialspherebackend.api.dto.user.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SearchUsersResult {

    @JsonProperty(value = "users")
    private final List<SearchUsersResponse> searchUsersListResponse;

    @JsonProperty
    private final UserErrorCode code;

    @JsonProperty
    private final String message;

    private SearchUsersResult(List<SearchUsersResponse> searchUsersResponse, UserErrorCode code, String message) {
        this.searchUsersListResponse = searchUsersResponse;
        this.code = code;
        this.message = message;
    }


    public static SearchUsersResult success(List<SearchUsersResponse> searchUsersListResponse) {
        return new SearchUsersResult(searchUsersListResponse, null, null);
    }

    public static SearchUsersResult failure(UserErrorCode code, String message) {
        return new SearchUsersResult(null, code, message);
    }


    public boolean isSuccess() {
        return searchUsersListResponse != null;
    }

    @JsonIgnore
    public boolean isFailure() {
        return !isSuccess();
    }

}
