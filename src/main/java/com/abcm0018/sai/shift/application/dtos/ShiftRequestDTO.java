package com.abcm0018.sai.shift.application.dtos;

import java.time.LocalTime;

import com.abcm0018.sai.shift.domain.enums.ShiftType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShiftRequestDTO {
	@NotNull(message = "El tipo de turno es obligatorio")
	private ShiftType shiftType;

	@NotNull(message = "La hora de inicio es obligatoria")
	private LocalTime startTime;

	@NotNull(message = "La hora de fin es obligatoria")
	private LocalTime endTime;

	@Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
	private String description;

	private Boolean active;
}
