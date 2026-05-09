package com.abcm0018.sai.productos.application.bulk.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que representa un Producto completo con sus 3 niveles de embalaje.
 * <p>
 * ESTRUCTURA JERÁRQUICA:
 * ProductWithPackLevelsDTO
 * ├─ productKey: "ACME-COLA-2L"
 * ├─ brand: "ACME"
 * ├─ formatCode: "COLA-2L"
 * ├─ name: "Refresco Cola 2L"
 * ├─ description: "Bebida refrescante"
 * ├─ manufacturedIn: "ES"
 * ├─ status: "ACTIVE"
 * └─ packLevels: [
 *      { packingLevel: "UNIDAD", gtin: "5901234123456", ... },
 *      { packingLevel: "CAJA", gtin: "5901234123463", ... },
 *      { packingLevel: "PALET", gtin: "15901234123470", ... }
 *    ]
 * <p>
 * IMPORTANTE:
 * - Esta es la salida del ProductBulkNormalizer
 * - Entrada del ProductBulkValidationService
 * - Validada estructuralmente (3 niveles, orden correcto, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductWithPackLevelsDTO {

	/**
	 * Clave única del producto (brand-formatCode)
	 * Útil para debugging y logs
	 */
	private String productKey;

	/**
	 * Información del producto base
	 */
	private String brand;
	private String formatCode;
	private String name;
	private String description;
	private String manufacturedIn;
	private String status;

	/**
	 * Lista de 3 ProductPackLevelDTO (UNIDAD, CAJA, PALET)
	 * Garantizado por el normalizer:
	 * - Tamaño exacto: 3
	 * - Orden: UNIDAD → CAJA → PALET
	 * - Cada uno validado estructuralmente
	 */
	private List<ProductPackLevelDTO> packLevels;

	@Override
	public String toString() {
		return String.format("Product{%s, %d niveles}", productKey, packLevels != null ? packLevels.size() : 0);
	}

	/**
	 * Obtiene el nivel UNIDAD
	 */
	public ProductPackLevelDTO getUnitLevel() {
		return packLevels != null && !packLevels.isEmpty() ? packLevels.get(0) : null;
	}

	/**
	 * Obtiene el nivel CAJA
	 */
	public ProductPackLevelDTO getBoxLevel() {
		return packLevels != null && packLevels.size() > 1 ? packLevels.get(1) : null;
	}

	/**
	 * Obtiene el nivel PALET
	 */
	public ProductPackLevelDTO getPaletLevel() {
		return packLevels != null && packLevels.size() > 2 ? packLevels.get(2) : null;
	}
}
