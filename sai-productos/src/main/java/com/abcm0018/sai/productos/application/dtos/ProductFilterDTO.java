package com.abcm0018.sai.productos.application.dtos;

import com.abcm0018.sai.productos.domain.enums.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para filtros de búsqueda de productos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterDTO {
	private ProductStatus status;
	private String brand;
	private String name;
	private String country;
	private String search;
	private String packLevelType;
}
