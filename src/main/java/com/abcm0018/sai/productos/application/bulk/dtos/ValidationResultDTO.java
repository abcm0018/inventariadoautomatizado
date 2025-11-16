package com.abcm0018.sai.productos.application.bulk.dtos;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para encapsular el resultado de la fase de validación.
 * Contiene la lista de productos que pasaron la validación
 * y la lista de todos los errores encontrados.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResultDTO {
	/**
	 * Productos que pasaron TODAS las validaciones de negocio
	 * <p>
	 * Garantizado:
	 * - brand+formatCode no existen en BD
	 * - Todos los GTINs son únicos en BD
	 * - Status es válido
	 * - Listos para persistencia
	 */
	private List<ProductWithPackLevelsDTO> validProducts;

	/**
	 * Errores encontrados durante validación
	 * <p>
	 * Tipos de errores:
	 * - DUPLICATE_PRODUCT: brand+formatCode ya existe
	 * - DUPLICATE_GTIN: GTIN ya existe
	 * - INVALID_STATUS: Status enum no válido
	 * - INVALID_COUNTRY: País no válido
	 * <p>
	 * IMPORTANTE: La validación continúa incluso encontrando errores
	 * para reportar TODOS los problemas al usuario de una vez
	 */
	private List<BulkImportErrorDTO> errors;

	public void addErrors(List<BulkImportErrorDTO> errorList) {
		this.errors.addAll(errorList);
	}

	public void addError(BulkImportErrorDTO error) {
		this.errors.add(error);
	}

	public boolean hasErrors() {
		return this.errors != null && !this.errors.isEmpty();
	}

	public int getValidCount() {
		return this.validProducts != null ? this.validProducts.size() : 0;
	}

	public int getErrorCount() {
		return this.errors != null ? this.errors.size() : 0;
	}

	public void addValidProduct(ProductWithPackLevelsDTO product) {
		validProducts.add(product);
	}
}
