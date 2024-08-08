package org.hl.socialspherebackend.api.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataResult<T, E> {

    @JsonProperty
    private final T data;

    @JsonProperty
    private final E errorCode;

    @JsonProperty
    private final String errorMessage;

    private DataResult(T data, E errorCode, String errorMessage) {
        this.data = data;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }


    public static <T, E> DataResult<T, E> success(T data) {
        return new DataResult<>(data, null, null);
    }

    public static <T, E> DataResult<T, E> failure(E errorCode, String errorMessage) {
        return new DataResult<>(null, errorCode, errorMessage);
    }

    @JsonProperty
    public boolean isSuccess() {
        return data != null;
    }

    @JsonIgnore
    public boolean isFailure() {
        return !isSuccess();
    }

    @JsonIgnore
    public T getData() {
        return data;
    }

    @JsonIgnore
    public E getErrorCode() {
        return errorCode;
    }

    @JsonIgnore
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    @JsonIgnore
    public String toString() {
        return "DataResult{" +
                "data=" + data +
                ", errorCode=" + errorCode +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

}
