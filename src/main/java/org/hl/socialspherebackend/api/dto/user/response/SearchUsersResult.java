package org.hl.socialspherebackend.api.dto.user.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SearchUsersResult {

    @JsonProperty(value = "users")
    private final List<SearchUsersResponse> searchUsersListResponse;

    @JsonProperty
    private final Code code;

    @JsonProperty
    private final String message;

    public enum Code { FOUND, NOT_FOUND, USER_DOES_NOT_EXITS, USER_PROFILE_DOES_NOT_EXISTS, USERS_NOT_FOUND }


    private SearchUsersResult(List<SearchUsersResponse> searchUsersResponse, Code code, String message) {
        this.searchUsersListResponse = searchUsersResponse;
        this.code = code;
        this.message = message;
    }


    public static SearchUsersResult success(List<SearchUsersResponse> searchUsersListResponse, Code code) {
        return new SearchUsersResult(searchUsersListResponse, code, null);
    }

    public static SearchUsersResult failure(Code code, String message) {
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
