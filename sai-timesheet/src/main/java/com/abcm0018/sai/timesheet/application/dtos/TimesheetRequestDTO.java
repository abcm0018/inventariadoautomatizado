package com.abcm0018.sai.timesheet.application.dtos;


import java.time.LocalDateTime;

import com.abcm0018.sai.timesheet.domain.enums.TimesheetStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetRequestDTO {
	@NotNull(message = "El ID del workshift es obligatorio")
	@Positive(message = "El ID del workshift debe ser positivo")
	private Long workshiftId;

	@NotNull(message = "La hora de entrada es obligatoria")
	@PastOrPresent(message = "La hora de entrada no puede ser futura")
	private LocalDateTime checkInAt;

	@PastOrPresent(message = "La hora de salida no puede ser futura")
	private LocalDateTime checkOutAt;

	@Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
	private String notes;

	private TimesheetStatus status;
}
