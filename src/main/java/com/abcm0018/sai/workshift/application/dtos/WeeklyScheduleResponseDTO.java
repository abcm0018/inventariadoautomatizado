package com.abcm0018.sai.workshift.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de generación de planificación semanal
 * Contiene información agregada sobre los turnos generados
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyScheduleResponseDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonProperty(value = "start_date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@JsonProperty(value = "end_date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;

	@JsonProperty(value = "total_workshifts_generated")
	private Integer totalWorkshiftsGenerated;

	@JsonProperty(value = "total_operators")
	private Integer totalOperators;

	@JsonProperty(value = "total_shifts")
	private Integer totalShifts;

	@JsonProperty(value = "work_days")
	private Integer workDays;

	@JsonProperty(value = "workshifts")
	private List<WorkshiftSummaryDTO> workshifts;

	@JsonProperty(value = "distribution_by_user")
	private Map<String, Integer> distributionByUser;

	@JsonProperty(value = "distribution_by_shift_type")
	private Map<String, Integer> distributionByShiftType;

	@JsonProperty(value = "distribution_by_day")
	private Map<LocalDate, Integer> distributionByDay;

	@JsonProperty(value = "generated_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime generatedAt;

	@JsonProperty(value = "success")
	private Boolean success;

	@JsonProperty(value = "message")
	private String message;
}