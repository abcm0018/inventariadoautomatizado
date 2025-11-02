package com.abcm0018.sai.productos.application.bulk.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando hay error al persistir los datos en BD.
 * <p>
 * Causas típicas:
 * - Constraint violation (GTIN duplicado a pesar de validación)
 * - Foreignkey violation
 * - Timeout en BD
 * - Conexión perdida
 * <p>
 * NOTA: Si llega aquí, significa que las validaciones previas no funcionaron.
 * Esto garantiza rollback transaccional.
 */
public class ProductBulkPersistenceException extends ProductBulkImportException {

	@Serial
	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "PERSISTENCE_ERROR";

	/**
	 * Constructor con mensaje y causa
	 */
	public ProductBulkPersistenceException(String message, Throwable cause) {
		super(ERROR_CODE, message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
	}

	/**
	 * Constructor simple con mensaje
	 */
	public ProductBulkPersistenceException(String message) {
		super(ERROR_CODE, message, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
