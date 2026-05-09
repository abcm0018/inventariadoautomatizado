package com.abcm0018.sai.timesheet.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
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
public class TimesheetDetailResponseDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@JsonProperty("check_in_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime checkInAt;

	@JsonProperty("check_out_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime checkOutAt;

	private String notes;
	private TimesheetStatus status;

	// Información del workshift
	@JsonProperty("workshift")
	private WorkshiftInfo workshift;

	// Información del usuario
	@JsonProperty("user")
	private UserInfo user;

	// Información del turno
	@JsonProperty("shift")
	private ShiftInfo shift;

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

	@JsonProperty("has_overtime")
	private Boolean hasOvertime;

	@JsonProperty("overtime_hours")
	private Long overtimeHours;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonProperty("updated_at")
	private LocalDateTime updatedAt;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class WorkshiftInfo implements Serializable {
		private Long id;

		@JsonFormat(pattern = "yyyy-MM-dd")
		private LocalDate date;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserInfo implements Serializable {
		private Long id;

		@JsonProperty("employee_number")
		private String employeeNumber;

		@JsonProperty("full_name")
		private String fullName;

		private String email;

		@JsonProperty("job_position")
		private String jobPosition;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ShiftInfo implements Serializable {
		private Long id;

		@JsonProperty("shift_type")
		private String shiftType;

		private String description;

		@JsonProperty("start_time")
		private String startTime;

		@JsonProperty("end_time")
		private String endTime;
	}
}
