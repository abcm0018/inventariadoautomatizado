package com.abcm0018.sai.palets.application.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar un palet
 * <p>
 * NOTA: No se permite cambiar packLevelId (la relación con el nivel de embalaje es inmutable)
 * Solo se pueden actualizar: SSCC, lote, fechas y horario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaletRequestDTO {

	@Size(min = 18, max = 18, message = "El SSCC debe tener exactamente 18 caracteres")
	@Pattern(regexp = "^[0-9]{18}$", message = "El SSCC solo debe contener dígitos")
	private String sscc;

	@Size(max = 50, message = "El número de lote no puede exceder 50 caracteres")
	private String batchNumber;

	@PastOrPresent(message = "La fecha de empaquetado no puede ser futura")
	private LocalDateTime packagingDateTime;

	@Future(message = "La fecha de caducidad debe ser futura")
	private LocalDate productUseByDate;
}
