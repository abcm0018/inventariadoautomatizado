package com.abcm0018.sai.workshift.exceptions;

import java.util.List;

import com.abcm0018.sai.shared.response.ResponseBuilder;
import com.abcm0018.sai.shared.response.ResponseError;
import com.abcm0018.sai.shared.response.StandardResponse;
import com.abcm0018.sai.shared.utils.ValidationErrorExtractor;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@Order(5)
@ControllerAdvice(basePackages = "com.abcm0018.sai.workshift")
public class WorkshiftExceptionControllerAdvice {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Object>> handleException(Exception ex){
        log.error(ex.getMessage(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String info = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
        return new ResponseEntity<>(ResponseBuilder.withError(status, null, info), status);
    }

    @ExceptionHandler(WorkshiftServiceException.class)
    public ResponseEntity<StandardResponse<Object>> handleUserException(WorkshiftServiceException ex){
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

	/**
	 * Handles exceptions of type {@link PropertyReferenceException} that are thrown during
	 * the sorting process, typically due to an invalid property reference in a query or request.
	 * Logs the error and constructs a response with appropriate error information.
	 *
	 * @param ex the {@link PropertyReferenceException} instance containing details of the error.
	 * @return a {@link StandardResponse} of type Void containing error details and HTTP status information.
	 */
	@ExceptionHandler(PropertyReferenceException.class)
	public StandardResponse<Void> handleSortError(PropertyReferenceException ex){
		String cleanMessage = String.format("Solicitud incorrecta: El campo de ordenamiento '%s' no es válido para la consulta", ex.getPropertyName());
		return ResponseBuilder.withError(HttpStatus.BAD_REQUEST, null, cleanMessage);
	}
}
