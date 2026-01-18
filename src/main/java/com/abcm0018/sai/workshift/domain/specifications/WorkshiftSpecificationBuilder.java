package com.abcm0018.sai.workshift.domain.specifications;

import java.util.function.Function;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.abcm0018.sai.workshift.application.dtos.WorkshiftFilterDTO;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor // Injección de dependencias por constructor
public class WorkshiftSpecificationBuilder implements SpecificationBuilder<Workshift, WorkshiftFilterDTO> {

	@Override
	public Specification<Workshift> buildFrom(WorkshiftFilterDTO filterDto) {
		if (filterDto == null)
			return Specification.where(null);

		// Inicializamos la Spec
		Specification<Workshift> spec = Specification.where(null);

		// Encadenamiento fluido (Fluent Chaining)
		// Usamos un helper 'conditionally' para limpiar el ruido visual
		spec = spec.and(conditionally(filterDto.getUserId(), WorkshiftSpecifications::hasUserId));
		spec = spec.and(conditionally(filterDto.getShiftId(), WorkshiftSpecifications::hasShiftId));
		spec = spec.and(conditionally(filterDto.getShiftType(), WorkshiftSpecifications::hasShiftType));

		// Lógica condicional de fechas (Regla de negocio: exacta tiene prioridad sobre rango)
		if (filterDto.getExactDate() != null) {
			spec = spec.and(WorkshiftSpecifications.hasExactDate(filterDto.getExactDate()));
		} else {
			spec = spec.and(WorkshiftSpecifications.hasDateBetween(filterDto.getStartDate(), filterDto.getEndDate()));
		}

		// Filtramos booleanos especiales
		spec = spec.and(WorkshiftSpecifications.isPeriod(filterDto.getIsToday(), filterDto.getIsPast(), filterDto.getIsFuture()));

		return spec;
	}

	/**
	 * Helper Method
	 * Si el valor es nulo o vacío, devuelve null (que Specification.and() ignora)
	 * Si tiene valor, ejecuta la función de fábrica de la Specificación.
	 */
	private <V> Specification<Workshift> conditionally(V value, Function<V, Specification<Workshift>> specFactory) {
		if (value == null) return null;
		if (value instanceof String && ((String) value).isBlank()) return null;

		return specFactory.apply(value);
	}
}
