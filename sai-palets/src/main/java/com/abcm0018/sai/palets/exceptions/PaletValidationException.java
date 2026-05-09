package com.abcm0018.sai.palets.exceptions;

import java.io.Serial;

/**
 * Extensión personalizada para los fallos en la lógica de negocio
 * durante el procesamiento de los palets.
 */
public class PaletValidationException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public PaletValidationException(String message) {
		super(message);
	}
}
