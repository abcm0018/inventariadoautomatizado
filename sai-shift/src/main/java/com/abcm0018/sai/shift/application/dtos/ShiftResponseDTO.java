package com.abcm0018.sai.shift.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.abcm0018.sai.shift.domain.enums.ShiftType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta completa para un turno
 * Incluye toda la información y campos calculados
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftResponseDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@JsonProperty("shift_type")
	private ShiftType shiftType;

	@JsonProperty("shift_type_display")
	private String shiftTypeDisplay;

	@JsonProperty("start_time")
	@JsonFormat(pattern = "HH:mm")
	private LocalTime startTime;

	@JsonProperty("end_time")
	@JsonFormat(pattern = "HH:mm")
	private LocalTime endTime;

	private String description;

	private Boolean active;

	@JsonProperty("created_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonProperty("updated_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

	@JsonProperty("duration_hours")
	private Long durationHours;

	@JsonProperty("duration_minutes")
	private Long durationMinutes;

	@JsonProperty("crosses_midnight")
	private Boolean crossesMidnight;

	@JsonProperty("shift_description")
	private String shiftDescription;

	@JsonProperty("has_been_updated")
	private Boolean hasBeenUpdated;

	@JsonProperty("total_workshifts")
	private Long totalWorkshifts;

	@JsonProperty("can_be_deleted")
	private Boolean canBeDeleted;
}
