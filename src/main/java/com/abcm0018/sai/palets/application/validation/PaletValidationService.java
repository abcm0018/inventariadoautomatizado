package com.abcm0018.sai.palets.application.validation;

import java.util.Comparator;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import com.abcm0018.sai.palets.application.validation.rules.PaletValidationRule;
import com.abcm0018.sai.palets.exceptions.PaletValidationException;
import com.abcm0018.sai.palets.infrastructure.messaging.dtos.PaletLecturaMessageDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * Motor de Orquestación (Servicio de Validación).
 * Recoge todas las implementaciones de 'PaletValidationRule'
 * y las ejecuta en orden.
 */
@Slf4j
@Service
public class PaletValidationService {

	private final List<PaletValidationRule> validationRules;

	/**
	 * Inyectamos todas las implementaciones de PaletValidationRule.
	 * Spring las recoge automáticamente y las mete en una lista.
	 * Las ordenamos UNA SOLA VEZ en el constructor para eficiencia.
	 */
	public PaletValidationService(List<PaletValidationRule> rules) {
		this.validationRules = rules.stream()
				.sorted(Comparator.comparingInt(Ordered::getOrder))
				.toList();

		log.info("Cargadas {} reglas de validación de palets.", this.validationRules.size());
		this.validationRules.forEach(rule ->
				log.info("  -> Regla cargada: {} (Orden: {})", rule.getClass().getSimpleName(), rule.getOrder())
		);
	}

	public PaletValidationContext validateAndContextualize(PaletLecturaMessageDTO message) throws PaletValidationException {

		// 1. Creamos el "contenedor"
		PaletValidationContext context = new PaletValidationContext(message);

		// 2. Ejecutamos cada regla en el orden establecido
		validationRules.forEach(rule -> {
			log.debug("Ejecutando regla: {}", rule.getClass().getSimpleName());
			rule.validate(context);
		});

		// 3. Si se valida correctamente, devuelve el contexto

		log.debug("Validación completada. WorkshiftID: {}, PackLevelID: {}", context.getWorkshift().getId(), context.getPackLevel().getId());
		return context;
	}
}
