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
 * DTO de respuesta para ProductPackLevel
 * Expone la información del nivel de embalaje al cliente
 * <p>
 * Incluye:
 * - Datos identificadores (GTIN, packingLevel)
 * - Datos físicos (peso, dimensiones)
 * - Datos logísticos (capacidad, apilado)
 * - Auditoría (createdAt, updatedAt)
 * <p>
 * No incluye:
 * - Producto completo (solo productId)
 * - Relación con palets (responsabilidad de PaletService)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPackLevelResponseDTO implements Serializable {

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

	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@JsonProperty("updated_at")
	private LocalDateTime updatedAt;

}
