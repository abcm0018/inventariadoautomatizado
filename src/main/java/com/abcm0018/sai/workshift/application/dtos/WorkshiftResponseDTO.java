package com.abcm0018.sai.workshift.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO ÚNICO para respuestas de Workshift.
 * Se usa tanto para listados (Page) como para detalles individuales (findById).
 * Diseño optimizado: Ligero pero estructurado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkshiftResponseDTO implements Serializable {

	@Serial private static final long serialVersionUID = 1L;

	private Long id;

	@JsonProperty("date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	@JsonProperty("shift")
	private ShiftInfo shift;

	@JsonProperty("user")
	private EmployeeInfo user;

	// Obligatorio para Optimistic Locking
	@JsonProperty("version")
	private Long version;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ShiftInfo implements Serializable {
		@Serial private static final long serialVersionUID = 1L;

		private Long id;

		@JsonProperty("name")
		private String name;

		@JsonProperty("start_time")
		@JsonFormat(pattern = "HH:mm")
		private LocalTime startTime;

		@JsonProperty("end_time")
		@JsonFormat(pattern = "HH:mm")
		private LocalTime endTime;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class EmployeeInfo implements Serializable {
		@Serial private static final long serialVersionUID = 1L;

		private Long id;

		@JsonProperty("employee_number")
		private String employeeNumber;

		@JsonProperty("full_name")
		private String fullName;
	}
}