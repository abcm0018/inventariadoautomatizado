package com.abcm0018.sai.productos.application.bulk.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkImportResponseDTO implements Serializable {
	/**
	 * Estado de la importación
	 * <p>
	 * Valores posibles:
	 * - SUCCESS: Importación completada sin errores
	 * - PARTIAL_SUCCESS: Algunos registros importados, otros con errores
	 * - ERROR: Importación fallida, ningún registro importado
	 */
	private String status;

	/**
	 * Mensaje descriptivo del resultado
	 * <p>
	 * Ejemplos:
	 * - "Importación completada exitosamente: 5 productos, 15 niveles de embalaje"
	 * - "Importación parcial: 4 productos, 12 niveles. Errores: 1"
	 * - "Error al parsear CSV: Encoding incorrecto"
	 */
	private String message;

	/**
	 * Número de productos creados exitosamente
	 * <p>
	 * En caso de SUCCESS: total de productos del CSV
	 * En caso de PARTIAL_SUCCESS: productos que se lograron crear
	 * En caso de ERROR: 0
	 */
	@JsonProperty("products_created")
	private Integer productsCreated;

	/**
	 * Número de ProductPackLevel creados exitosamente
	 * <p>
	 * En caso de SUCCESS: total de niveles del CSV (products × 3)
	 * En caso de PARTIAL_SUCCESS: niveles que se lograron crear
	 * En caso de ERROR: 0
	 */
	@JsonProperty("pack_levels_created")
	private Integer packLevelsCreated;

	/**
	 * Lista de errores detallados (si aplica)
	 * <p>
	 * Nulo en caso de SUCCESS
	 * Contiene errores en caso de PARTIAL_SUCCESS o ERROR
	 */
	private List<BulkImportErrorDTO> errors;

	/**
	 * Timestamp de cuando se procesó la importación
	 */
	private LocalDateTime timestamp;

	/**
	 * Identificador único del fichero de errores generado, si lo hay.
	 * El frontend puede usar esto para construir la URL de descarga.
	 * Ejemplo: "errores_job_xyz.csv"
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String errorFileId;

	/**
	 * Retorna true si la importación fue exitosa (sin errores)
	 */
	public boolean isSuccess() {
		return "SUCCESS".equals(status);
	}

	/**
	 * Retorna true si hubo éxito parcial (algunos errores)
	 */
	public boolean isPartialSuccess() {
		return "PARTIAL_SUCCESS".equals(status);
	}

	/**
	 * Retorna true si falló completamente
	 */
	public boolean isError() {
		return "ERROR".equals(status);
	}

	/**
	 * Retorna el número total de errores
	 */
	public Integer getErrorCount() {
		return errors != null ? errors.size() : 0;
	}
}
