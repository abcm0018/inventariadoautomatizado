package com.abcm0018.sai.productos.application.bulk.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando hay error al normalizar los datos parseados del CSV.
 * <p>
 * Causas típicas:
 * - Falta alguno de los 3 niveles (UNIDAD, CAJA, PALET)
 * - Orden incorrecto de niveles
 * - Quantities inválidas para el tipo de nivel
 * - GTIN con formato incorrecto
 * - Dimensiones fuera de rango
 */
public class ProductNormalizationException extends ProductBulkImportException {

	@Serial
	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "NORMALIZATION_ERROR";

	/**
	 * Constructor simple con mensaje
	 */
	public ProductNormalizationException(String message) {
		super(ERROR_CODE, message, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Constructor con número de línea
	 */
	public ProductNormalizationException(String message, Integer lineNumber) {
		super(ERROR_CODE, String.format("Error de normalización en línea %d: %s", lineNumber, message), HttpStatus.BAD_REQUEST);
	}

	/**
	 * Constructor con referencia a producto (brand-formatCode)
	 */
	public ProductNormalizationException(String productKey, String message) {
		super(ERROR_CODE, String.format("Error normalizando producto '%s': %s", productKey, message), HttpStatus.BAD_REQUEST);
	}
}
