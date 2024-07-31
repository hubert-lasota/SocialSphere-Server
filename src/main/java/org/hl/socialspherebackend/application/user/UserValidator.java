package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
import org.hl.socialspherebackend.api.dto.user.response.UserErrorCode;
import org.hl.socialspherebackend.application.validator.Validator;

public class UserValidator extends Validator<UserProfileRequest, UserValidateResult> {

    public UserValidator() {
        super();
    }


    public UserValidateResult validate(UserProfileRequest userProfile) {
        if(userProfile == null) {
            return new UserValidateResult(false, UserErrorCode.USER_PROFILE_REQUEST_IS_NULL,
                    "user profile request is null");
        }

        String firstName = userProfile.firstName();
        String lastName = userProfile.lastName();
        String city = userProfile.city();
        String country = userProfile.country();

        if(firstName == null) {
            return new UserValidateResult(false, UserErrorCode.FIRST_NAME_IS_NULL,
                    "First name is null");
        }

        if(containsWhitespace(firstName) && !acceptWhitespace) {
            return new UserValidateResult(false, UserErrorCode.FIRST_NAME_CONTAINS_WHITESPACE,
                    "First name \"%s\" contains whitespace characters".formatted(firstName));
        }

        if(firstName.isBlank() && !acceptBlankText) {
            return new UserValidateResult(false, UserErrorCode.FIRST_NAME_IS_BLANK,
                    "First name contains only white characters");
        }

        if(firstName.length() > textMaxSize) {
            return new UserValidateResult(false, UserErrorCode.FIRST_NAME_LENGTH_IS_TOO_LONG,
                    "First name contains more than %d characters".formatted(textMaxSize));
        }

        if(firstName.length() < textMinSize) {
            return new UserValidateResult(false, UserErrorCode.FIRST_NAME_LENGTH_IS_TOO_SHORT,
                    "First name contains less than %d characters".formatted(textMinSize));
        }


        if(lastName == null) {
            return new UserValidateResult(false, UserErrorCode.LAST_NAME_IS_NULL,
                    "Last name is null");
        }

        if(lastName.isBlank() && !acceptBlankText) {
            return new UserValidateResult(false, UserErrorCode.LAST_NAME_IS_BLANK,
                    "Last name contains only white characters");
        }

        if(containsWhitespace(lastName) && !acceptWhitespace) {
            return new UserValidateResult(false, UserErrorCode.LAST_NAME_CONTAINS_WHITESPACE,
                    "Last name \"%s\" contains whitespace characters".formatted(lastName));
        }

        if(lastName.length() > textMaxSize) {
            return new UserValidateResult(false, UserErrorCode.LAST_NAME_LENGTH_IS_TOO_LONG,
                    "Last name contains more than %d characters".formatted(textMaxSize));
        }

        if(lastName.length() < textMinSize) {
            return new UserValidateResult(false, UserErrorCode.LAST_NAME_LENGTH_IS_TOO_SHORT,
                    "Last name contains less than %d characters".formatted(textMinSize));
        }


        if(city == null) {
            return new UserValidateResult(false, UserErrorCode.CITY_IS_NULL,
                    "City is null");
        }

        if(city.isBlank() && !acceptBlankText) {
            return new UserValidateResult(false, UserErrorCode.CITY_IS_BLANK,
                    "City contains only white characters");
        }

        if(containsWhitespace(city) && !acceptWhitespace) {
            return new UserValidateResult(false, UserErrorCode.CITY_CONTAINS_WHITESPACE,
                    "City \"%s\" contains whitespace characters".formatted(city));
        }

        if(city.length() > textMaxSize) {
            return new UserValidateResult(false, UserErrorCode.CITY_LENGTH_IS_TOO_LONG,
                    "City contains more than %d characters".formatted(textMaxSize));
        }

        if(city.length() < textMinSize) {
            return new UserValidateResult(false, UserErrorCode.CITY_LENGTH_IS_TOO_SHORT,
                    "City contains less than %d characters".formatted(textMinSize));
        }

        if(country == null) {
            return new UserValidateResult(false, UserErrorCode.COUNTRY_IS_NULL,
                    "Country is null");
        }

        if(country.isBlank() && !acceptBlankText) {
            return new UserValidateResult(false, UserErrorCode.COUNTRY_IS_BLANK,
                    "Country contains only white characters");
        }

        if(containsWhitespace(country) && !acceptWhitespace) {
            return new UserValidateResult(false, UserErrorCode.COUNTRY_CONTAINS_WHITESPACE,
                    "Country \"%s\" contains whitespace characters".formatted(country));
        }

        if(country.length() > textMaxSize) {
            return new UserValidateResult(false, UserErrorCode.COUNTRY_LENGTH_IS_TOO_LONG,
                    "Country contains more than %d characters".formatted(textMaxSize));
        }

        if(country.length() < textMinSize) {
            return new UserValidateResult(false, UserErrorCode.COUNTRY_LENGTH_IS_TOO_SHORT,
                    "Country contains less than %d characters".formatted(textMinSize));
        }

        return new UserValidateResult(true, null, null);
    }

}
