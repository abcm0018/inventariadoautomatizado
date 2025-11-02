package com.abcm0018.sai.productos.application.dtos;

import com.abcm0018.sai.productos.domain.enums.ProductStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cambiar el estado de un producto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusChangeDTO {
	@NotNull(message = "El nuevo estado es obligatorio")
	private ProductStatus newStatus;

	@Size(min = 10, max = 500, message = "La razón debe tener entre 10 y 500 caracteres")
	private String reason;
}
