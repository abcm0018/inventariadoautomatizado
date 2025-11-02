package com.abcm0018.sai.productos.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.abcm0018.sai.productos.domain.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta estándar para Product (listados)
 * Información condensada sin detalles innecesarios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonProperty("id")
	private Long id;

	@JsonProperty("name")
	private String name;

	@JsonProperty("brand")
	private String brand;

	@JsonProperty("format_code")
	private String formatCode;

	@JsonProperty("status")
	private ProductStatus status;

	@JsonProperty("manufactured_in")
	private String manufacturedIn;

	@JsonProperty("pack_level_count")
	private Integer packLevelCount;

	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@JsonProperty("updated_at")
	private LocalDateTime updatedAt;

}
