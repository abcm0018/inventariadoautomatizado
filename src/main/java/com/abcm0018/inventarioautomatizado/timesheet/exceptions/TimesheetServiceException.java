package com.abcm0018.inventarioautomatizado.timesheet.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TimesheetServiceException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final String details;

    public TimesheetServiceException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }

    public TimesheetServiceException(String errorCode, String message, HttpStatus httpStatus, String details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }

    public TimesheetServiceException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }
}
