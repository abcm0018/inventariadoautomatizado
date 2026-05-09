package com.abcm0018.sai.productos.application.dtos;

import java.io.Serializable;
import java.math.BigDecimal;

import com.abcm0018.sai.productos.domain.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO resumido para listados de productos
 * Solo incluye información esencial
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDTO implements Serializable {
	private Long id;
	private String ean;
	private String name;
	private String brand;

	@JsonProperty("format_code")
	private String formatCode;

	private BigDecimal weight;

	@JsonProperty("weight_unit")
	private String weightUnit;

	private ProductStatus status;

	@JsonProperty("full_name")
	private String fullName;

	@JsonProperty("is_available")
	private Boolean isAvailable;
}
