package com.abcm0018.sai.email.exception;

import org.springframework.http.HttpStatus;

import com.abcm0018.sai.shared.constants.CustomErrorCode;

import lombok.Getter;

@Getter
public class EmailServiceException extends RuntimeException {

	private final String errorCode;
	private final HttpStatus httpStatus;

	public EmailServiceException(String message, String errorCode, HttpStatus httpStatus) {
		super(message);
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
	}

	public EmailServiceException(String message, String errorCode, Throwable cause, HttpStatus httpStatus) {
		super(message, cause);
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
	}

	/**
	 * Error al enviar un email
	 */
	public static EmailServiceException sendingError(String recipient, Throwable cause) {
		return new EmailServiceException(
				String.format("Error al enviar email a %s: %s", recipient, cause.getMessage()),
				CustomErrorCode.INTERNAL_SERVER_ERROR, cause, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Plantilla de email no encontrada
	 */
	public static EmailServiceException templateNotFound(String templateName) {
		return new EmailServiceException(String.format("Plantilla de email no encontrada: %s", templateName),
				CustomErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND);
	}

	/**
	 * Error procesando la plantilla de email
	 */
	public static EmailServiceException templateProcessingError(String templateName, Throwable cause) {
		return new EmailServiceException(String.format("Error procesando plantilla %s: %s", templateName, cause.getMessage()),
				CustomErrorCode.INTERNAL_SERVER_ERROR, cause, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Validación de email fallida
	 */
	public static EmailServiceException validationError(String message) {
		return new EmailServiceException(message, CustomErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Parámetro requerido faltante
	 */
	public static EmailServiceException missingParameter(String emailType, String parameter) {
		return new EmailServiceException(String.format("Parámetro requerido '%s' faltante para el tipo de email: %s", parameter, emailType),
				CustomErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Destinatario inválido
	 */
	public static EmailServiceException invalidRecipient(String recipient) {
		return new EmailServiceException(String.format("Destinatario inválido: %s", recipient),
				CustomErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
	}
}
