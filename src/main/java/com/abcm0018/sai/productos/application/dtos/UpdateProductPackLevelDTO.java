package com.abcm0018.sai.productos.application.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar un ProductPackLevel existente
 * <p>
 * Restricciones:
 * - GTIN no es actualizable (es la clave única de identificación)
 * - productId no es actualizable (la relación es fija)
 * - packingLevel no es actualizable (definir el tipo después es confuso)
 * <p>
 * Solo permite actualizar propiedades físicas que pueden cambiar (peso, dimensiones, límites)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductPackLevelDTO {

	private BigDecimal netWeight;

	@Positive(message = "La altura debe ser mayor a 0 mm")
	@Max(value = 3000, message = "La altura no puede exceder 3000 mm")
	private BigDecimal heightMM;

	@Positive(message = "El ancho debe ser mayor a 0 mm")
	@Max(value = 3000, message = "El ancho no puede exceder 3000 mm")
	private BigDecimal widthMM;

	@Min(value = 1, message = "Debe haber al menos 1 unidad por nivel")
	@Max(value = 10000, message = "No pueden haber más de 10,000 unidades por nivel")
	private Integer unitsInLevel;

	@Min(value = 1, message = "El límite de apilado debe ser al menos 1")
	@Max(value = 20, message = "El límite de apilado no puede exceder 20")
	private Integer stackingLimit;

	@Min(value = 0, message = "No pueden haber cajas negativas")
	@Max(value = 10000, message = "No pueden haber más de 10,000 cajas por palet")
	private Integer boxesPerPalet;

}
