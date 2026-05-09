package com.abcm0018.sai.auth.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthServiceException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;
    private final String details;

    public AuthServiceException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }

    public AuthServiceException(String errorCode, String message, HttpStatus httpStatus, String details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }

    public AuthServiceException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }
}