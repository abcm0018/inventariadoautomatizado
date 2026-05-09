package com.abcm0018.sai.productos.application.bulk.service.validator.rules;

import java.util.Collections;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.abcm0018.sai.productos.application.bulk.dtos.BulkImportErrorDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ProductWithPackLevelsDTO;
import com.abcm0018.sai.productos.application.bulk.service.validator.rules.impl.ProductValidationRule;
import com.abcm0018.sai.productos.domain.enums.ProductStatus;

/**
 * Estrategia de validación para el campo 'status'.
 * Comprueba que el status no sea nulo/vacío y que
 * sea un valor de Enum válido.
 */
@Component
@Order(1)
public class StatusValidationRule implements ProductValidationRule {
	@Override
	public List<BulkImportErrorDTO> validate(ProductWithPackLevelsDTO product) {
		String status = product.getStatus();

		if (status == null || status.isBlank()) {
			// Permitido, el mapper usará el default (ACTIVE)
			return Collections.emptyList();
		}

		try {
			// Intentamos convertir usando la nueva lógica flexible
			ProductStatus.fromDisplayNameOrName(status);
			return Collections.emptyList();

		} catch (IllegalArgumentException e) {
			// Si fromDisplayNameOrName falla (ej. "Activado"), es un error
			return List.of(buildError(
					"Status '" + status + "' no es válido. (Valores esperados: ACTIVE, Activo, DISCONTINUED, Discontinuado, etc.)"
			));
		}
	}

	private BulkImportErrorDTO buildError(String message) {
		return BulkImportErrorDTO.builder().type("VALIDATION_ERROR").errorMessage(message).build();
	}
}
