package com.abcm0018.sai.productos.application.bulk.service.validator.rules.impl;

import java.util.List;

import com.abcm0018.sai.productos.application.bulk.dtos.BulkImportErrorDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ProductWithPackLevelsDTO;

/**
 * Interfaz Strategy para una Regla de validación de Negocio
 * <p>
 * Cada implementación concreta se centrará en una única validación (SRP).
 */
public interface ProductValidationRule {
	/**
	 * Valida un producto contra una regla de negocio específica.
	 *
	 * @param product El DTO del producto normalizado
	 * @return Una lista de errores de validación.
	 * Si la lista está vacía, la validación fue exitosa.
	 */
	List<BulkImportErrorDTO> validate(ProductWithPackLevelsDTO product);
}
