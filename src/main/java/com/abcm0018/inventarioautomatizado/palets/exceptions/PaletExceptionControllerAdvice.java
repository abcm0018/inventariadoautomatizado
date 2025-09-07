package com.abcm0018.inventarioautomatizado.palets.exceptions;

import com.abcm0018.inventarioautomatizado.shared.response.ResponseBuilder;
import com.abcm0018.inventarioautomatizado.shared.response.ResponseError;
import com.abcm0018.inventarioautomatizado.shared.response.StandardResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Order(121)
@Slf4j
public class PaletExceptionControllerAdvice {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Object>> handleException(Exception ex){
        log.error(ex.getMessage(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String info = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
        return new ResponseEntity<>(ResponseBuilder.withError(status, null, info), status);
    }

    @ExceptionHandler(PaletsServiceException.class)
    public ResponseEntity<StandardResponse<Object>> handlePaletException(PaletsServiceException ex){
        log.error(ex.getMessage(), ex);
        HttpStatus status = ex.getHttpStatus();
        String info = status.getReasonPhrase();
        if(HttpStatus.BAD_REQUEST.equals(status)){
            ResponseError error = new ResponseError(2000, ex.getMessage());
            return new ResponseEntity<>(ResponseBuilder.withError(status, error, info), status);
        }
        return new ResponseEntity<>(ResponseBuilder.withError(status, null, info), status);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<StandardResponse<Object>> handleEntityExists(EntityExistsException ex){
        log.error(ex.getMessage(), ex);
        HttpStatus status = HttpStatus.CONFLICT;
        String info = status.getReasonPhrase();
        return new ResponseEntity<>(ResponseBuilder.withError(status, null, info), status);
    }

    @ExceptionHandler({EntityNotFoundException.class, NoSuchMethodException.class})
    public ResponseEntity<StandardResponse<Object>> handleEntityNotFoundException(EntityNotFoundException ex){
        log.error(ex.getMessage(), ex);
        HttpStatus status = HttpStatus.NOT_FOUND;
        String info = status.getReasonPhrase();
        return new ResponseEntity<>(ResponseBuilder.withError(status, null, info), status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandardResponse<Object>> handleIllegalStateException(IllegalArgumentException ex){
        log.error(ex.getMessage(), ex);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String info = status.getReasonPhrase();
        ResponseError error = new ResponseError(2000, ex.getMessage());
        return new ResponseEntity<>(ResponseBuilder.withError(status, error, info), status);
    }

    @ExceptionHandler
    public ResponseEntity<StandardResponse<Object>> handleValidation(MethodArgumentNotValidException ex){
        log.error(ex.getMessage(), ex);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String info = status.getReasonPhrase();
        return new ResponseEntity<>(ResponseBuilder.withError(status, null, info), status);
    }
}
