package com.abcm0018.sai.productos.exceptions;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ProductPackLevelServiceException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;
    private final String details;

    public ProductPackLevelServiceException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }

    public ProductPackLevelServiceException(String errorCode, String message, HttpStatus httpStatus, String details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }

    public ProductPackLevelServiceException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }
}
