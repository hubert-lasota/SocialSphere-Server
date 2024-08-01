package org.hl.socialspherebackend.application.user;

import org.hl.socialspherebackend.api.dto.user.request.UserProfileRequest;
import org.hl.socialspherebackend.api.dto.user.response.UserErrorCode;
import org.hl.socialspherebackend.application.validator.Validator;

public class UserProfileValidator extends Validator<UserProfileRequest, UserValidateResult> {

    public UserProfileValidator() {
        super();
    }


    @Override
    public UserValidateResult validate(UserProfileRequest userProfile) {
        if(userProfile == null) {
            return new UserValidateResult(false, UserErrorCode.USER_PROFILE_REQUEST_IS_NULL,
                    "user profile request is null");
        }

        String firstName = userProfile.firstName();
        String lastName = userProfile.lastName();
        String city = userProfile.city();
        String country = userProfile.country();
        setAcceptFirstCharUppercase(false);

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

        if(Character.isUpperCase(firstName.charAt(0)) && !acceptFirstCharUppercase) {
            return new UserValidateResult(false, UserErrorCode.FIRST_NAME_DOES_NOT_STARTS_UPPERCASE,
                    "First name \"%s\" first character is not uppercase".formatted(firstName));
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

        if(Character.isUpperCase(lastName.charAt(0)) && !acceptFirstCharUppercase) {
            return new UserValidateResult(false, UserErrorCode.LAST_NAME_DOES_NOT_STARTS_UPPERCASE,
                    "Last name \"%s\" first character is not uppercase".formatted(lastName));
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

        if(Character.isUpperCase(city.charAt(0)) && !acceptFirstCharUppercase) {
            return new UserValidateResult(false, UserErrorCode.CITY_DOES_NOT_STARTS_UPPERCASE,
                    "First name \"%s\" first character is not uppercase".formatted(city));
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

        if(Character.isUpperCase(country.charAt(0)) && !acceptFirstCharUppercase) {
            return new UserValidateResult(false, UserErrorCode.COUNTRY_DOES_NOT_STARTS_UPPERCASE,
                    "Country \"%s\" first character is not uppercase".formatted(country));
        }


        return new UserValidateResult(true, null, null);
    }

}
