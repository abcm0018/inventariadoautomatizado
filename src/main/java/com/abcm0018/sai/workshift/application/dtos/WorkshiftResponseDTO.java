package com.abcm0018.sai.workshift.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta básica para Workshift
 * Incluye información principal sin anidar entidades completas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkshiftResponseDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@JsonProperty(value = "date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	// Información del turno (shift)
	@JsonProperty(value = "shift_id")
	private Long shiftId;

	@JsonProperty(value = "shift_type")
	private String shiftType;

	@JsonProperty(value = "shift_description")
	private String shiftDescription;

	@JsonProperty(value = "start_time")
	private String startTime;

	@JsonProperty(value = "end_time")
	private String endTime;

	// Información del usuario
	@JsonProperty(value = "user_id")
	private Long userId;

	@JsonProperty(value = "employee_number")
	private String employeeNumber;

	@JsonProperty(value = "user_full_name")
	private String userFullName;

	@JsonProperty(value = "job_position")
	private String jobPosition;

	// Estadísticas
	@JsonProperty(value = "total_scanned_palets")
	private Integer totalScannedPalets;

	@JsonProperty(value = "total_timesheets")
	private Integer totalTimesheets;

	// Campos calculados
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
}

