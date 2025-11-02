package com.abcm0018.sai.workshift.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO resumido para listados de workshifts
 * Solo incluye información esencial para reducir payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkshiftSummaryDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@JsonProperty(value = "date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	@JsonProperty(value = "shift_type")
	private String shiftType;

	@JsonProperty(value = "user_full_name")
	private String userFullName;

	@JsonProperty(value = "employee_number")
	private String employeeNumber;

	@JsonProperty(value = "is_today")
	private Boolean isToday;

	@JsonProperty(value = "is_past")
	private Boolean isPast;

	@JsonProperty(value = "total_scanned_palets")
	private Integer totalScannedPalets;
}