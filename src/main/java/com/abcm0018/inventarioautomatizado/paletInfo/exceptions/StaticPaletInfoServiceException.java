package com.abcm0018.inventarioautomatizado.paletInfo.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StaticPaletInfoServiceException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final String details;

    public StaticPaletInfoServiceException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }

    public StaticPaletInfoServiceException(String errorCode, String message, HttpStatus httpStatus, String details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }

    public StaticPaletInfoServiceException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }
}
