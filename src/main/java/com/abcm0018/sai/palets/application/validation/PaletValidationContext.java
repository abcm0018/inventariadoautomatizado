package com.abcm0018.sai.palets.application.validation;

import com.abcm0018.sai.palets.infrastructure.messaging.dtos.PaletLecturaMessageDTO;
import com.abcm0018.sai.productos.domain.entity.ProductPackLevel;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

import lombok.Data;

/**
 * Objeto "contenedor" que viaja a través de la cadena de Responsabilidad.
 * Contiene el mensaje original y se enriquece con los datos
 * recuperados por las reglas.
 */
@Data
public class PaletValidationContext {

	// Datos de entrada (el mensaje)
	private final PaletLecturaMessageDTO message;

	// --- Datos de enriquecimiento ---
	// Los datos irán rellenando estos campos
	private Workshift workshift;
	private ProductPackLevel packLevel;

	/**
	 * El contexto se inicializa con el mensaje original
	 */
	public PaletValidationContext(PaletLecturaMessageDTO message) {
		this.message = message;
	}
}
