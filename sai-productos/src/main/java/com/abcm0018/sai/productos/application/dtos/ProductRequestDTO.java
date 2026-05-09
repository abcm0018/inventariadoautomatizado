package com.abcm0018.sai.productos.application.dtos;
import com.abcm0018.sai.productos.domain.enums.ProductStatus;

import jakarta.validation.constraints.*;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {
	@NotBlank(message = "El nombre del producto es obligatorio")
	@Size(min = 1, max = 255, message = "El nombre debe tener entre 1 y 255 caracteres")
	private String name;

	@NotBlank(message = "La marca es obligatoria")
	@Size(min = 1, max = 200, message = "La marca debe tener entre 1 y 200 caracteres")
	private String brand;

	@Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
	private String description;

	@NotBlank(message = "El código de formato es obligatorio")
	@Size(min = 1, max = 50, message = "El código de formato debe tener entre 1 y 50 caracteres")
	private String formatCode;

	@NotNull(message = "El estado del producto es obligatorio")
	private ProductStatus status;

	@NotBlank(message = "El país de manufactura es obligatorio")
	@Size(min = 1, max = 100, message = "El país de manufactura debe tener entre 1 y 100 caracteres")
	private String manufacturedIn;
}
