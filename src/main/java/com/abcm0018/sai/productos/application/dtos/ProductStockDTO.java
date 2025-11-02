package com.abcm0018.sai.productos.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import com.abcm0018.sai.productos.domain.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para información de inventario de productos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	@JsonProperty("product_id")
	private Long productId;

	@JsonProperty("product_name")
	private String productName;

	private String brand;

	@JsonProperty("format_code")
	private String formatCode;

	private ProductStatus status;

	@JsonProperty("current_stock")
	private Long currentStock;

	@JsonProperty("stock_status")
	private String stockStatus;

	@JsonProperty("needs_replenishment")
	private Boolean needsReplenishment;

	@JsonProperty("is_available")
	private Boolean isAvailable;

	@JsonProperty("total_inventory_weight")
	private BigDecimal totalInventoryWeight;

	@JsonProperty("palet_weight")
	private BigDecimal paletWeight;
}
