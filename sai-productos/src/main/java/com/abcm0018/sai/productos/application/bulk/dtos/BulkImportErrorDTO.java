package com.abcm0018.sai.productos.application.bulk.dtos;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportErrorDTO implements Serializable {
	/**
	 * Número de fila en el CSV (basado en 1, no en 0)
	 * <p>
	 * Nota: La fila 1 es el header, así que los datos comienzan en fila 2
	 * Si un error ocurre en la primera fila de datos, rowNumber será 2
	 */
	private Integer rowNumber;

	/**
	 * Mensaje descriptivo del error en lenguaje natural
	 * <p>
	 * Ejemplos:
	 * - "GTIN debe contener 13-14 dígitos"
	 * - "Producto duplicado: ACME-COLA-2L"
	 * - "Falta nivel de embalaje: PALET"
	 * - "Cantidad inválida para nivel UNIDAD: 2 (esperado: 1)"
	 */
	private String errorMessage;

	/**
	 * Tipo/categoría del error para facilitar clasificación
	 * <p>
	 * Tipos posibles:
	 * - CSV_PARSE_ERROR: Error parseando el CSV
	 * - INVALID_GTIN_FORMAT: GTIN con formato incorrecto
	 * - INVALID_QUANTITY: Cantidad inválida para el nivel
	 * - MISSING_PACK_LEVEL: Falta alguno de los 3 niveles
	 * - DUPLICATE_PRODUCT: Product (brand+formatCode) ya existe
	 * - DUPLICATE_GTIN: GTIN ya existe
	 * - NORMALIZATION_ERROR: Error general de normalización
	 * - VALIDATION_ERROR: Error general de validación
	 * - PERSISTENCE_ERROR: Error al guardar en BD
	 */
	private String type;
}
