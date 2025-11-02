package com.abcm0018.sai.workshift.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con información completa y detallada de un Workshift
 * Incluye información anidada de Shift y User
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkshiftDetailResponseDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@JsonProperty(value = "date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	@JsonProperty(value = "shift")
	private ShiftInfo shift;

	@JsonProperty(value = "user")
	private UserInfo user;

	// Estadísticas
	@JsonProperty(value = "total_scanned_palets")
	private Integer totalScannedPalets;

	@JsonProperty(value = "total_timesheets")
	private Integer totalTimesheets;

	// Campos calculados
	@JsonProperty(value = "full_description")
	private String fullDescription;

	@JsonProperty(value = "is_today")
	private Boolean isToday;

	@JsonProperty(value = "is_past")
	private Boolean isPast;

	@JsonProperty(value = "is_future")
	private Boolean isFuture;

	@JsonProperty(value = "has_been_updated")
	private Boolean hasBeenUpdated;

	// Auditoría
	@JsonProperty(value = "created_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonProperty(value = "updated_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ShiftInfo implements Serializable {

		@Serial
		private static final long serialVersionUID = 1L;

		private Long id;

		@JsonProperty(value = "shift_type")
		private String shiftType;

		@JsonProperty(value = "start_time")
		@JsonFormat(pattern = "HH:mm")
		private LocalTime startTime;

		@JsonProperty(value = "end_time")
		@JsonFormat(pattern = "HH:mm")
		private LocalTime endTime;

		@JsonProperty(value = "description")
		private String description;

		@JsonProperty(value = "is_active")
		private Boolean isActive;

		@JsonProperty(value = "duration_hours")
		private Long durationHours;

		@JsonProperty(value = "crosses_midnight")
		private Boolean crossesMidnight;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserInfo implements Serializable {

		@Serial
		private static final long serialVersionUID = 1L;

		private Long id;

		@JsonProperty(value = "employee_number")
		private String employeeNumber;

		@JsonProperty(value = "full_name")
		private String fullName;

		@JsonProperty(value = "email")
		private String email;

		@JsonProperty(value = "job_position")
		private String jobPosition;

		@JsonProperty(value = "role")
		private String role;

		@JsonProperty(value = "is_active")
		private Boolean isActive;
	}
}