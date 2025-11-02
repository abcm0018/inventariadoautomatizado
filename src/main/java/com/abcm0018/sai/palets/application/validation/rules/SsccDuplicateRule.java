package com.abcm0018.sai.palets.application.validation.rules;

import org.springframework.stereotype.Component;

import com.abcm0018.sai.palets.application.validation.PaletValidationContext;
import com.abcm0018.sai.palets.domain.repository.PaletRepository;
import com.abcm0018.sai.palets.exceptions.PaletValidationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REGLA 1: Comprueba si el SSCC ya existe en la base de datos.
 * Esta debe ser la primera regla (orden 10) para rechazar duplicados
 * lo más rápido posible.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SsccDuplicateRule implements PaletValidationRule {

	private final PaletRepository paletRepository;

	@Override
	public void validate(PaletValidationContext context) throws PaletValidationException {

		final String sscc = context.getMessage().getSscc();

		log.debug("Regla [SSCC]: Comprobando SSCC '{}'", sscc);

		if (paletRepository.existsBySscc(sscc)) {
			log.warn("Regla [SSCC] fallida: SSCC '{}' ya existe en el sistema", sscc);

			// Lanza la excepción. Esto detendrá la cadena y
			// hará que el consumidor de RabbitMQ rechace el mensaje (NACK).
			throw new PaletValidationException("SSCC Duplicado: " + sscc);
		}
	}

	/**
	 * Define el orden de ejecución.
	 * Le damos un número bajo (10) para que sea la primera en ejecutarse.
	 */
	@Override
	public int getOrder() {
		return 10;
	}
}
