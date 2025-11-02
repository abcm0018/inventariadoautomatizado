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

/**
 * DTO resumido para listados de timesheets
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetSummaryDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@JsonProperty("user_full_name")
	private String userFullName;

	@JsonProperty("check_in_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime checkInAt;

	@JsonProperty("check_out_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime checkOutAt;

	private TimesheetStatus status;

	@JsonProperty("worked_duration")
	private String workedDuration;

	@JsonProperty("is_open")
	private Boolean isOpen;
}
