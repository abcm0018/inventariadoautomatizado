package com.abcm0018.sai.workshift.domain.specifications;

import org.springframework.data.jpa.domain.Specification;

/**
 * Interfaz genérica para construir especificaciones desde un DTO de filtros
 * @param <T> La entidad JPA
 * @param <D> El DTO de filtros
 */
public interface SpecificationBuilder<T, D> {
	Specification<T> buildFrom(D filterDto);
}
