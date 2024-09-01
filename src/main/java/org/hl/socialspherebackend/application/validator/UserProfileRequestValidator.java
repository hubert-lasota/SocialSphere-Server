package org.hl.socialspherebackend.application.validator;

import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;

public class UserProfileRequestValidator extends RequestValidatorChain {

    public UserProfileRequestValidator(RequestValidatorChain next) {
        super(next);
    }


    @Override
    protected RequestValidateResult doValidate(Object request) {
        UserProfileRequest r = (UserProfileRequest) request;
        String firstName = r.firstName();
        String lastName = r.lastName();
        String city = r.city();
        String country = r.country();
        setForceFirstCharUppercase(true);

        RequestValidateResult firstNameResult = validateString(firstName, "First name");
        if(!firstNameResult.valid()) {
            return firstNameResult;
        }

        RequestValidateResult lastNameResult = validateString(lastName, "Last name");
        if(!lastNameResult.valid()) {
            return lastNameResult;
        }

        RequestValidateResult cityResult = validateString(city, "City");
        if(!cityResult.valid()) {
            return cityResult;
        }

        RequestValidateResult countryResult = validateString(country, "Country");
        if(!countryResult.valid()) {
            return countryResult;
        }

        return new RequestValidateResult(true, null, null);
    }

    @Override
    protected boolean isRequestValidInstance(Object request) {
        return request instanceof UserProfileRequest;
    }


    private RequestValidateResult validateString(String field, String fieldNameInErrorMessage) {
        if(field == null) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_NULL,
                    "%s is null".formatted(fieldNameInErrorMessage));
        }

        if(containsWhitespace(field) && !acceptWhitespace) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_CONTAINS_WHITESPACE,
                    "%s \"%s\" contains whitespace characters".formatted(fieldNameInErrorMessage, field));
        }

        if(field.isBlank() && !acceptBlankText) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_BLANK,
                    "%s contains only white characters".formatted(fieldNameInErrorMessage));
        }

        if(field.length() > textMaxSize) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_TOO_LONG,
                    "%s contains more than %d characters".formatted(fieldNameInErrorMessage, textMaxSize));
        }

        if(field.length() < textMinSize) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_IS_TOO_SHORT,
                    "%s contains less than %d characters".formatted(fieldNameInErrorMessage, textMinSize));
        }

        if(!Character.isUpperCase(field.charAt(0)) && forceFirstCharUppercase) {
            return new RequestValidateResult(false, RequestValidateErrorCode.CONTENT_DOES_NOT_STARTS_UPPERCASE,
                    "%s \"%s\" first character is not uppercase".formatted(fieldNameInErrorMessage, field));
        }

        return new RequestValidateResult(true, null, null);
    }

}
