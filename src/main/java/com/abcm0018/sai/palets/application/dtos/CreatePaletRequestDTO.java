package com.abcm0018.sai.palets.application.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear un nuevo palet
 * Solo contiene campos necesarios para la creación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaletRequestDTO {
	@NotBlank(message = "El código SSCC es obligatorio")
	@Size(min = 18, max = 18, message = "El SSCC debe tener exactamente 18 caracteres")
	@Pattern(regexp = "^[0-9]{18}$", message = "El SSCC solo debe contener dígitos")
	private String sscc;

	@NotBlank(message = "El número de lote es obligatorio")
	@Size(max = 50, message = "El número de lote no puede exceder 50 caracteres")
	private String batchNumber;

	@NotNull(message = "El ID del nivel de embalaje (ProductPackLevel) es obligatorio")
	@Positive(message = "El ID del nivel de embalaje debe ser positivo")
	private Long packLevelId;

	@NotNull(message = "El ID del turno (workshift) es obligatorio")
	@Positive(message = "El ID del turno debe ser positivo")
	private Long workshiftId;

	@NotNull(message = "La fecha de empaquetado es obligatoria")
	@PastOrPresent(message = "La fecha de empaquetado no puede ser futura")
	private LocalDate packagingDate;

	@NotNull(message = "La fecha de caducidad es obligatoria")
	@Future(message = "La fecha de caducidad debe ser futura")
	private LocalDate productUseByDate;

	@NotBlank(message = "La hora de producción es obligatoria")
	@Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "La hora debe tener formato HH:mm")
	private String productionTime;

	private Long userId;
}
