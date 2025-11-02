package com.abcm0018.sai.workshift.application.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar una asignación de turno
 * NOTA: La actualización manual es EXCEPCIONAL y solo para correcciones administrativas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkshiftRequestDTO {

	@Positive(message = "El ID del usuario debe ser positivo")
	private Long userId;

	@Positive(message = "El ID del turno debe ser positivo")
	private Long shiftId;

	@PastOrPresent(message = "La fecha no puede ser futura")
	private LocalDate date;

	@NotBlank(message = "El motivo de la actualización manual es obligatorio")
	@Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
	private String reason;
}