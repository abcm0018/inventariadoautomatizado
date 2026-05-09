package com.abcm0018.sai.productos.application.bulk.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando hay error en las validaciones de negocio.
 * <p>
 * Causas típicas:
 * - Product (brand+formatCode) ya existe en BD
 * - GTIN ya existe en BD
 * - Status enum inválido
 * - Country no válido
 * - Configuración logística inválida
 */
public class ProductValidationException extends ProductBulkImportException {

	@Serial
	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "VALIDATION_ERROR";

	/**
	 * Constructor simple con mensaje
	 */
	public ProductValidationException(String message) {
		super(ERROR_CODE, message, HttpStatus.CONFLICT);
	}

	/**
	 * Constructor para errores de duplicación (Product o GTIN)
	 */
	public ProductValidationException(String entityType, String identifier) {
		super(ERROR_CODE,
				String.format("%s ya existe en la base de datos: %s", entityType, identifier),
				HttpStatus.CONFLICT);
	}

	/**
	 * Constructor con número de línea
	 */
	public ProductValidationException(String message, Integer lineNumber) {
		super(ERROR_CODE, String.format("Error de validación en línea %d: %s", lineNumber, message), HttpStatus.CONFLICT);
	}
}
