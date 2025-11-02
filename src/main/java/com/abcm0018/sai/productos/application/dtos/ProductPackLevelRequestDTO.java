package com.abcm0018.sai.productos.application.dtos;

import java.math.BigDecimal;

import com.abcm0018.sai.productos.domain.enums.PackingLevel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear un nuevo ProductPackLevel
 * Contiene solo los datos necesarios del cliente para crear un nivel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPackLevelRequestDTO {

	@NotNull(message = "El ID del producto es obligatorio")
	private Long productId;

	@NotBlank(message = "El GTIN es obligatorio")
	private String gtin;

	@NotNull(message = "El nivel de embalaje es obligatorio")
	private PackingLevel packingLevel;

	@NotNull(message = "El peso neto es obligatorio")
	@Positive(message = "El peso debe ser mayor a 0")
	@Max(value = 1000000, message = "El peso no puede exceder 1,000,000 kg")
	private BigDecimal netWeight;

	@NotNull(message = "La altura es obligatoria")
	@Positive(message = "La altura debe ser mayor a 0 mm")
	@Max(value = 3000, message = "La altura no puede exceder 3000 mm")
	private BigDecimal heightMM;

	@NotNull(message = "El ancho es obligatorio")
	@Positive(message = "El ancho debe ser mayor a 0 mm")
	@Max(value = 3000, message = "El ancho no puede exceder 3000 mm")
	private BigDecimal widthMM;

	@NotNull(message = "Unidades por nivel es obligatorio")
	@Min(value = 1, message = "Debe haber al menos 1 unidad por nivel")
	@Max(value = 10000, message = "No pueden haber más de 10,000 unidades por nivel")
	private Integer unitsInLevel;

	@NotNull(message = "Límite de apilado es obligatorio")
	@Min(value = 1, message = "El límite de apilado debe ser al menos 1")
	@Max(value = 20, message = "El límite de apilado no puede exceder 20")
	private Integer stackingLimit;

	@NotNull(message = "Cajas por palet es obligatorio")
	@Min(value = 0, message = "No pueden haber cajas negativas")
	@Max(value = 10000, message = "No pueden haber más de 10,000 cajas por palet")
	private Integer boxesPerPalet;
}