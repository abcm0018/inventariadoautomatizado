package com.abcm0018.sai.palets.application.validation.rules;

import org.springframework.core.Ordered;

import com.abcm0018.sai.palets.application.validation.PaletValidationContext;
import com.abcm0018.sai.palets.exceptions.PaletValidationException;

/**
 * Interfaz para una regla en la Cadena de Responsabilidad
 * de validación de palets.
 */
public interface PaletValidationRule extends Ordered {
	/**
	 * Valida o enriquece el contexto.
	 * @param context El contexto que se pasa entre reglas.
	 * @throws PaletValidationException si la regla falla.
	 */
	void validate(PaletValidationContext context) throws PaletValidationException;
}
