package com.abcm0018.sai.productos.application.bulk.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando hay error al parsear el archivo CSV.
 * <p>
 * Causas típicas:
 * - Encoding incorrecto
 * - Formato CSV inválido
 * - Headers faltantes
 * - Caracteres especiales no soportados
 * - Archivo vacío
 */
public class CsvParseException extends ProductBulkImportException {

	@Serial
	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "CSV_PARSE_ERROR";

	/**
	 * Constructor simple con mensaje
	 */
	public CsvParseException(String message) {
		super(ERROR_CODE, message, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Constructor con causa raíz (para wrappear excepciones de Apache Commons CSV)
	 */
	public CsvParseException(String message, Throwable cause) {
		super(ERROR_CODE, message, HttpStatus.BAD_REQUEST, cause);
	}

	/**
	 * Constructor con número de línea (útil para identificar la fila problemática)
	 */
	public CsvParseException(String message, Integer lineNumber) {
		super(ERROR_CODE, String.format("Error en línea %d del CSV: %s", lineNumber, message), HttpStatus.BAD_REQUEST);
	}
}