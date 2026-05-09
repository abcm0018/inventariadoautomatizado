package com.abcm0018.sai.productos.application.dtos;

import com.abcm0018.sai.productos.domain.enums.ProductStatus;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar un producto existente
 * <p>
 * Restricciones:
 * - name NO es actualizable (identidad del producto)
 * - brand NO es actualizable (marca es inmutable)
 * - formatCode NO es actualizable (código de formato es inmutable)
 * - manufacturedIn NO es actualizable (origen es inmutable)
 * <p>
 * Solo permite actualizar:
 * - description (puede cambiar)
 * - status (cambiar ACTIVE → DISCONTINUED, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequestDTO {

	@Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
	private String description;

	private ProductStatus status;

}
