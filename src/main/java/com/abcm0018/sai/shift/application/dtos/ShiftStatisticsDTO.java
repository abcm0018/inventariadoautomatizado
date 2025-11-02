package com.abcm0018.sai.shift.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShiftStatisticsDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	@JsonProperty("total_shifts")
	private Long totalShifts;

	@JsonProperty("active_shifts")
	private Long activeShifts;

	@JsonProperty("inactive_shifts")
	private Long inactiveShifts;

	@JsonProperty("shifts_crossing_midnight")
	private Long shiftsCrossingMidnight;

	@JsonProperty("average_shift_duration_hours")
	private Double averageShiftDurationHours;

	@JsonProperty("total_workshifts_assigned")
	private Long totalWorkshiftsAssigned;

	@JsonProperty("shifts_by_type")
	private Map<String, Long> shiftsByType;

	@JsonProperty("shifts_modified")
	private Long shiftsModified;

	@JsonProperty("can_create_more_shifts")
	private Boolean canCreateMoreShifts;
}
