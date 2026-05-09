package com.abcm0018.sai.shift.exceptions;

import java.util.List;

import com.abcm0018.sai.shared.response.ResponseBuilder;
import com.abcm0018.sai.shared.response.ResponseError;
import com.abcm0018.sai.shared.response.StandardResponse;
import com.abcm0018.sai.shared.utils.ValidationErrorExtractor;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@Order(3)
@ControllerAdvice(basePackages = "com.abcm0018.inventarioautomatizado.shift")
public class ShiftExceptionControllerAdvice {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Object>> handleException(Exception ex){
        log.error(ex.getMessage(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String info = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
        return new ResponseEntity<>(ResponseBuilder.withError(status, null, info), status);
    }

    @ExceptionHandler(ShiftServiceException.class)
    public ResponseEntity<StandardResponse<Object>> handleShiftException(ShiftServiceException ex){
		HttpStatus status = ex.getHttpStatus();
		String info = status.getReasonPhrase();

		ResponseError error = new ResponseError(ex.getHttpStatus().value(), ex.getMessage());
		return new ResponseEntity<>(ResponseBuilder.withError(status, error, info), status);
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

    @ExceptionHandler
    public ResponseEntity<StandardResponse<Object>> handleValidation(MethodArgumentNotValidException ex){
		log.error(ex.getMessage(), ex);

		List<ResponseError> errors = ValidationErrorExtractor.extract(ex.getBindingResult());

		HttpStatus status = HttpStatus.BAD_REQUEST;
		return new ResponseEntity<>(ResponseBuilder.withErrors(status, errors, status.getReasonPhrase()), status);
    }
}
