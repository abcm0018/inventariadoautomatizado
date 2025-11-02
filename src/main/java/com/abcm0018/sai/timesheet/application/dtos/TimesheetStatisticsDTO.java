package com.abcm0018.sai.timesheet.application.dtos;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetStatisticsDTO {
	private Long totalTimesheets;
	private Long openTimesheets;
	private Long closedTimesheets;
	private Long anomalies;
	private Double averageWorkedHours;
	private Long totalWorkedHours;

	// Estadísticas por estado
	private Map<String, Long> timesheetsByStatus;

	// Estadísticas por tipo de turno
	private Map<String, Long> timesheetsByShiftType;

	// Usuarios más puntuales
	private Map<String, Object> punctualityStats;

	// Usuarios con más horas trabajadas
	private Map<String, Object> workHoursStats;
}
