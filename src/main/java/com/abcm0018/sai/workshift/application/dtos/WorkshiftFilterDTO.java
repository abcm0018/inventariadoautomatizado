package com.abcm0018.sai.workshift.application.dtos;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para filtros de búsqueda de workshifts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkshiftFilterDTO {

	private Long userId;

	private Long shiftId;

	private String shiftType;

	private LocalDate startDate;

	private LocalDate endDate;

	private LocalDate exactDate;

	private Boolean isPast;

	private Boolean isFuture;

	private Boolean isToday;
}