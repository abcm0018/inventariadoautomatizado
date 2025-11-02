package com.abcm0018.sai.productos.application.bulk.exceptions;

import java.io.Serial;

/**
 * Excepción lanzada cuando el GTIN tiene un formato inválido.
 * <p>
 * Causas típicas:
 * - GTIN-13 usado para PALET (debe ser GTIN-14)
 * - GTIN-14 usado para UNIDAD/CAJA (debe ser GTIN-13)
 * - Contiene caracteres no numéricos
 * - Longitud fuera del rango 8-14
 */
public class InvalidGtinFormatException extends ProductNormalizationException {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor con GTIN y tipo de nivel
	 */
	public InvalidGtinFormatException(String gtin, String packingLevel) {
		super(String.format("Formato GTIN inválido para nivel %s: %s (debe ser %s)",
				packingLevel, gtin, getExpectedFormat(packingLevel)));
	}

	/**
	 * Constructor simple con mensaje
	 */
	public InvalidGtinFormatException(String message) {
		super(message);
	}

	/**
	 * Constructor con número de línea
	 */
	public InvalidGtinFormatException(String message, Integer lineNumber) {
		super(message, lineNumber);
	}

	private static String getExpectedFormat(String packingLevel) {
		if ("PALET".equalsIgnoreCase(packingLevel)) {
			return "GTIN-14 (14 dígitos)";
		}
		return "GTIN-13 (13 dígitos)";
	}
}
