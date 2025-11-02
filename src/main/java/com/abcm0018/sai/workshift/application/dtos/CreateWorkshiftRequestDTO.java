package com.abcm0018.sai.workshift.application.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear una asignación de turno excepcional
 * NOTA: La creación manual de turnos es EXCEPCIONAL
 * La planificación normal se hace automáticamente con el cron
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkshiftRequestDTO {

	@NotNull(message = "El ID del usuario es obligatorio")
	@Positive(message = "El ID del usuario debe ser positivo")
	private Long userId;

	@NotNull(message = "El ID del turno (shift) es obligatorio")
	@Positive(message = "El ID del turno debe ser positivo")
	private Long shiftId;

	@NotNull(message = "La fecha es obligatoria")
	@PastOrPresent(message = "La fecha no puede ser futura")
	private LocalDate date;

	@NotBlank(message = "El motivo de la creación manual es obligatorio")
	@Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
	private String reason;
}