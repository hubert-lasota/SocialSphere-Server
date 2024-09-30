package org.hl.socialspherebackend.api.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

public class DataResult<T> {

    @JsonProperty(value = "data", index = 0)
    private final T data;

    @JsonProperty(value = "success", index = 1)
    private final boolean success;

    @JsonProperty(value = "errorCode", index = 2)
    private final Enum<?> errorCode;

    @JsonProperty(value = "errorMessage", index = 3)
    private final String errorMessage;


    @JsonIgnore
    private final HttpStatus httpStatus;

    private DataResult(T data, boolean success, Enum<?> errorCode, String errorMessage, HttpStatus httpStatus) {
        this.data = data;
        this.success = success;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }


    public static <T> DataResult<T> success(T data) {
        return new DataResult<>(data,true,null, null, HttpStatus.OK);
    }

    public static <NULL> DataResult<NULL> failure(Enum<?> errorCode, String errorMessage) {
        return failure(errorCode, errorMessage, HttpStatus.BAD_REQUEST);
    }

    public static <NULL> DataResult<NULL> failure(Enum<?> errorCode, String errorMessage, HttpStatus httpStatus) {
        return new DataResult<>(null, false, errorCode, errorMessage, httpStatus);
    }


    @JsonIgnore
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

    @JsonIgnore
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    @JsonIgnore
    public String toString() {
        return "DataResult{" +
                "data=" + data +
                ", success=" + success +
                ", errorCode=" + errorCode +
                ", errorMessage='" + errorMessage + '\'' +
                ", httpStatus=" + httpStatus +
                '}';
    }

}
