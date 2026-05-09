package com.abcm0018.sai.shift.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;

import com.abcm0018.sai.shift.domain.enums.ShiftType;
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
public class ShiftSummaryDTO implements Serializable {
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

	private Boolean active;

	@JsonProperty("duration_hours")
	private Long durationHours;

	@JsonProperty("shift_description")
	private String shiftDescription;
}
