package com.abcm0018.sai.productos.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con detalles completos del inventario de un producto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventoryDetailsDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private ProductResponseDTO product;

	@JsonProperty("current_stock")
	private Long currentStock;

	@JsonProperty("pack_level_details")
	private List<ProductPackLevelResponseDTO> packLevelDetails;

	@JsonProperty("total_inventory_weight")
	private BigDecimal totalInventoryWeight;

	@JsonProperty("stock_status")
	private String stockStatus;

	@JsonProperty("is_available")
	private Boolean isAvailable;

	@JsonProperty("needs_replenishment")
	private Boolean needsReplenishment;
}
