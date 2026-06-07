package com.abcm0018.sai.palets.application.validation.rules;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.abcm0018.sai.palets.application.validation.PaletValidationContext;
import com.abcm0018.sai.palets.exceptions.PaletValidationException;
import com.abcm0018.sai.workshift.application.service.WorkshiftService;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REGLA 2: Busca el turno de trabajo (Workshift) asociado al empleado y la fecha.
 * Esta regla es la principal consumidora de la caché de Workshift en Redis.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FindWorkshiftRule implements PaletValidationRule {

	// Inyectamos el servicio, que es el que sabe cómo buscar
	// (Ya sea en caché o en BBDD)
	private final WorkshiftService workshiftService;

	@Override
	public void validate(PaletValidationContext context) throws PaletValidationException {

		final String employeeNumber = context.getMessage().getEmployeeNumber();
		final LocalDateTime scanDateTime = context.getMessage().getScanDate();

		log.debug("Regla [Workshift]: Buscando turno para Empleado '{}' en fecha/hora '{}'", employeeNumber, scanDateTime);

		// Usamos la caché
		Optional<Workshift> foundWorkshift = workshiftService.findCachedWorkshiftByEmployeeAndDate(employeeNumber, scanDateTime);

		// Si no se encuentra (ni en la caché ni en BBDD)...
		if (foundWorkshift.isEmpty()) {
			log.warn("Regla [Workshift] Fallida: No se encontró turno para Empleado '{}' en fecha/hora '{}'", employeeNumber, scanDateTime);

			throw new PaletValidationException("Turno de trabajo no encontrado para el empleado " + employeeNumber + " en la fecha " + scanDateTime.toLocalDate());
		}

		// Si encontrado, guardamos el turno en el contexto
		// para que el paletService lo use al final.
		context.setWorkshift(foundWorkshift.get());
	}

	/**
	 * Define el orden de ejecución.
	 * 20 = Se ejecuta después de la regla de SSCC (10).
	 */
	@Override
	public int getOrder() {
		return 20;
	}
}
