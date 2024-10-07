package org.hl.socialspherebackend.application.validator;

import org.hl.socialspherebackend.api.dto.user.request.SearchFriendsRelationshipStatus;
import org.hl.socialspherebackend.api.dto.user.request.SearchFriendsRequest;

public class SearchFriendsRequestValidator extends RequestValidatorChain {

    public SearchFriendsRequestValidator(RequestValidatorChain next) {
        super(next);
    }

    @Override
    protected RequestValidateResult doValidate(Object request) {
        SearchFriendsRequest r = (SearchFriendsRequest) request;
        String firstNamePattern = r.firstNamePattern();
        String lastNamePattern = r.lastNamePattern();
        String cityPattern = r.cityPattern();
        String countryPattern = r.countryPattern();

        if((firstNamePattern == null || firstNamePattern.isBlank()) &&
                (lastNamePattern == null || lastNamePattern.isBlank()) &&
                (cityPattern == null  || cityPattern.isBlank()) &&
                (countryPattern == null || countryPattern.isBlank())) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_NULL,
                    "All patterns are blank/null. You need to use at least 1 pattern string. " +
                            "Patterns: firstNamePattern, lastNamePattern, cityPattern, countryPattern");
        }

        SearchFriendsRelationshipStatus status = r.relationshipStatus();
        if(status == null) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_NULL,
                    "relationshipStatus is null");
        }

        return new RequestValidateResult(true, null, null);
    }

    @Override
    protected boolean isRequestValidInstance(Object request) {
        return request instanceof SearchFriendsRequest;
    }
}
