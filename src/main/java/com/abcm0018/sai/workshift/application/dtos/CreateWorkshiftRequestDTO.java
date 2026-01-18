package com.abcm0018.sai.workshift.application.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateWorkshiftRequestDTO {
	@NotNull(message = "La fecha de inicio es obligatoria")
	@FutureOrPresent(message = "La fecha de inicio debe ser hoy o futura")
	private LocalDate fromDate;

	@NotNull(message = "La fecha fin es obligatoria")
	@FutureOrPresent(message = "La fecha de fin debe ser hoy o futura")
	private LocalDate toDate;
}