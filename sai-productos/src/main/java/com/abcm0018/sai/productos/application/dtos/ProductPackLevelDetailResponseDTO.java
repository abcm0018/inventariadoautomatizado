package com.abcm0018.sai.productos.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.abcm0018.sai.productos.domain.enums.PackingLevel;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO detallado para ProductPackLevel
 * Expone información completa incluyendo cálculos de negocio
 * <p>
 * Uso: Respuestas GET /api/pack-levels/{id}
 * <p>
 * Incluye:
 * - Todos los datos de ProductPackLevelResponseDTO
 * - Cálculos de negocio (totalWeightPerPalet, descripción)
 * - Validación de configuración
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPackLevelDetailResponseDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonProperty("id")
	private Long id;

	@JsonProperty("product_id")
	private Long productId;

	@JsonProperty("gtin")
	private String gtin;

	@JsonProperty("packing_level")
	private PackingLevel packingLevel;

	@JsonProperty("net_weight")
	private BigDecimal netWeight;

	@JsonProperty("height_mm")
	private BigDecimal heightMM;

	@JsonProperty("width_mm")
	private BigDecimal widthMM;

	@JsonProperty("units_in_level")
	private Integer unitsInLevel;

	@JsonProperty("stacking_limit")
	private Integer stackingLimit;

	@JsonProperty("boxes_per_palet")
	private Integer boxesPerPalet;

	/**
	 * Peso total de un palet completamente lleno
	 * Calculado como: netWeight × boxesPerPalet
	 */
	@JsonProperty("total_weight_per_palet")
	private BigDecimal totalWeightPerPalet;

	/**
	 * Descripción legible del nivel
	 * Ejemplo: "PALET | GTIN: 12345678901234 | Peso: 500.00kg | Dims: 1200×1000mm | Stack: 5 | Cajas: 100"
	 */
	@JsonProperty("description")
	private String description;

	/**
	 * Indica si la configuración logística es válida
	 */
	@JsonProperty("is_valid_configuration")
	private boolean isValidConfiguration;

	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@JsonProperty("updated_at")
	private LocalDateTime updatedAt;

}
