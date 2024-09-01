package org.hl.socialspherebackend.api.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataResult<T> {

    @JsonProperty(value = "data", index = 0)
    private final T data;

    @JsonProperty(value = "errorCode", index = 2)
    private final Enum<?> errorCode;

    @JsonProperty(value = "errorMessage", index = 3)
    private final String errorMessage;

    private DataResult(T data, Enum<?> errorCode, String errorMessage) {
        this.data = data;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }


    public static <T> DataResult<T> success(T data) {
        return new DataResult<>(data, null, null);
    }

    public static <Void> DataResult<Void> failure(Enum<?> errorCode, String errorMessage) {
        return new DataResult<>(null, errorCode, errorMessage);
    }

    @JsonProperty(value = "success", index = 1)
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
    public Enum<?> getErrorCode() {
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
