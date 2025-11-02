package com.abcm0018.sai.workshift.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con estadísticas globales de workshifts
 * Útil para dashboards y reportes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkshiftStatisticsDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	// Estadísticas generales
	@JsonProperty(value = "total_workshifts")
	private Long totalWorkshifts;

	@JsonProperty(value = "total_active_operators")
	private Long totalActiveOperators;

	@JsonProperty(value = "total_shifts_configured")
	private Long totalShiftsConfigured;

	// Estadísticas por período
	@JsonProperty(value = "workshifts_this_week")
	private Long workshiftsThisWeek;

	@JsonProperty(value = "workshifts_this_month")
	private Long workshiftsThisMonth;

	@JsonProperty(value = "workshifts_today")
	private Long workshiftsToday;

	// Estadísticas futuras
	@JsonProperty(value = "future_workshifts")
	private Long futureWorkshifts;

	@JsonProperty(value = "next_week_planned")
	private Boolean nextWeekPlanned;

	// Distribución
	@JsonProperty(value = "workshifts_by_shift_type")
	private Map<String, Long> workshiftsByShiftType;

	@JsonProperty(value = "workshifts_by_user")
	private Map<String, Long> workshiftsByUser;

	@JsonProperty(value = "average_workshifts_per_user")
	private Double averageWorkshiftsPerUser;

	// Auditoría
	@JsonProperty(value = "modified_workshifts")
	private Long modifiedWorkshifts;

	@JsonProperty(value = "workshifts_without_palets")
	private Long workshiftsWithoutPalets;

	@JsonProperty(value = "workshifts_without_timesheets")
	private Long workshiftsWithoutTimesheets;

	// Período de consulta
	@JsonProperty(value = "query_start_date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate queryStartDate;

	@JsonProperty(value = "query_end_date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate queryEndDate;
}