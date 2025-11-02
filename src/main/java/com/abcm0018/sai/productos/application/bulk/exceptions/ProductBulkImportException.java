package com.abcm0018.sai.productos.application.bulk.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Excepción padre para todas las excepciones del módulo de importación masiva de productos.
 * <p>
 * Proporciona:
 * - Código de error personalizado
 * - Mensaje descriptivo
 * - Status HTTP asociado
 * - Causa raíz (Throwable)
 * <p>
 * IMPORTANTE: Esta es la excepción base. Las excepciones específicas heredan de esta.
 */
@Getter
public class ProductBulkImportException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	private final String errorCode;
	private final HttpStatus httpStatus;

	/**
	 * Constructor completo
	 *
	 * @param errorCode código de error personalizado (ej: PARSE_ERROR, DUPLICATE_GTIN)
	 * @param message mensaje descriptivo del error
	 * @param httpStatus status HTTP asociado
	 */
	public ProductBulkImportException(String errorCode, String message, HttpStatus httpStatus) {
		super(message);
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
	}

	/**
	 * Constructor con causa raíz
	 *
	 * @param errorCode código de error personalizado
	 * @param message mensaje descriptivo del error
	 * @param httpStatus status HTTP asociado
	 * @param cause excepción que causó este error
	 */
	public ProductBulkImportException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
	}
}