package com.abcm0018.sai.productos.application.bulk.service.validator.rules.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.abcm0018.sai.productos.application.bulk.dtos.BulkImportErrorDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ProductWithPackLevelsDTO;

/**
 * Estrategia de validación para el país de fabricación
 */
@Order(3) // Se ejecuta después de los otros validadores
@Component
public class CountryValidationRule implements ProductValidationRule {
	// TODO: Podría ser inyectado desde properties
	private static final Set<String> VALID_COUNTRIES = Set.of("ES", "PT", "FR", "IT", "DE");

	@Override
	public List<BulkImportErrorDTO> validate(ProductWithPackLevelsDTO product) {
		String country = product.getManufacturedIn();

		if (country == null || country.isBlank()) {
			return List.of(buildError(product, "El país de fabricación es requerido."));
		}

		if (!VALID_COUNTRIES.contains(country.toUpperCase().trim())) {
			return List.of(buildError(product, "País '" + country + "' no soportado."));
		}

		return Collections.emptyList();
	}

	private BulkImportErrorDTO buildError(ProductWithPackLevelsDTO product, String message) {
		return BulkImportErrorDTO.builder().errorMessage(message).type("VALIDATION_ERROR").build();
	}
}
