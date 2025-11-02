package com.abcm0018.sai.timesheet.application.dtos;


import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.abcm0018.sai.timesheet.domain.enums.TimesheetStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetResponseDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@JsonProperty("workshift_id")
	private Long workshiftId;

	@JsonProperty("user_id")
	private Long userId;

	@JsonProperty("user_full_name")
	private String userFullName;

	@JsonProperty("check_in_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime checkInAt;

	@JsonProperty("check_out_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime checkOutAt;

	private String notes;
	private TimesheetStatus status;

	// Campos calculados
	@JsonProperty("worked_hours")
	private Long workedHours;

	@JsonProperty("worked_minutes")
	private Long workedMinutes;

	@JsonProperty("worked_duration")
	private String workedDuration;

	@JsonProperty("is_open")
	private Boolean isOpen;

	@JsonProperty("is_closed")
	private Boolean isClosed;

	@JsonProperty("is_late")
	private Boolean isLate;

	@JsonProperty("late_minutes")
	private Long lateMinutes;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonProperty("updated_at")
	private LocalDateTime updatedAt;
}
