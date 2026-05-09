package com.abcm0018.sai.productos.application.bulk.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO que representa el resultado de una persistencia exitosa.
 * <p>
 * DIFERENCIA CON BulkImportResponse:
 * - BulkImportResult: Salida interna de ProductBulkPersistenceService (solo éxito)
 * - BulkImportResponse: Salida final al cliente (success, partial_success o error)
 * <p>
 * FLUJO:
 * ValidationResult (válidos)
 *   ↓ ProductBulkPersistenceService
 * BulkImportResult (persistidos)
 *   ↓ ProductBulkImportService (orquestador)
 * BulkImportResponse (respuesta al cliente)
 * <p>
 * USADO EN:
 * - Retorno de ProductBulkPersistenceService.persist()
 * - Conversión a BulkImportResponse en ProductBulkImportService
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkImportResultDTO {

	/**
	 * Número de Products creados exitosamente
	 */
	@JsonProperty("products_created")
	private Integer productsCreated;

	/**
	 * Número de ProductPackLevel creados exitosamente
	 * <p>
	 * Típicamente: productsCreated × 3 (cada product tiene 3 niveles)
	 */
	@JsonProperty("pack_levels_created")
	private Integer packLevelsCreated;

	/**
	 * Timestamp de cuándo se completó la persistencia
	 */
	@JsonProperty("timestamp")
	private LocalDateTime timestamp;

	/**
	 * Flag indicando que la persistencia fue exitosa
	 * <p>
	 * Siempre true para BulkImportResult (si falla, lanza excepción)
	 */
	@JsonProperty("success")
	private Boolean success;
}
