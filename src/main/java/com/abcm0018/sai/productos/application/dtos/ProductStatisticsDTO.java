package com.abcm0018.sai.productos.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatisticsDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	@JsonProperty("total_products")
	private Long totalProducts;

	@JsonProperty("active_products")
	private Long activeProducts;

	@JsonProperty("discontinued_products")
	private Long discontinuedProducts;

	@JsonProperty("products_with_stock")
	private Long productsWithStock;

	@JsonProperty("products_without_stock")
	private Long productsWithoutStock;

	@JsonProperty("low_stock_products")
	private Long lowStockProducts;

	@JsonProperty("critical_stock_products")
	private Long criticalStockProducts;

	@JsonProperty("products_with_pack_levels")
	private Long productsWithPackLevels;

	@JsonProperty("total_inventory_weight")
	private BigDecimal totalInventoryWeight;

	@JsonProperty("products_by_status")
	private Map<String, Long> productsByStatus;

	@JsonProperty("products_by_brand")
	private Map<String, Long> productsByBrand;

	@JsonProperty("products_by_country")
	private Map<String, Long> productsByCountry;
}
