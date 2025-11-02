package com.abcm0018.sai.workshift.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar conflictos o validaciones en asignaciones de workshifts
 * Útil para validaciones antes de crear/actualizar turnos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkshiftConflictDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonProperty(value = "has_conflicts")
	private Boolean hasConflicts;

	@JsonProperty(value = "conflict_type")
	private String conflictType; // DUPLICATE, OVERLAP, INACTIVE_USER, INACTIVE_SHIFT

	@JsonProperty(value = "user_id")
	private Long userId;

	@JsonProperty(value = "user_full_name")
	private String userFullName;

	@JsonProperty(value = "date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	@JsonProperty(value = "conflicting_workshifts")
	private List<WorkshiftSummaryDTO> conflictingWorkshifts;

	@JsonProperty(value = "message")
	private String message;

	@JsonProperty(value = "can_proceed")
	private Boolean canProceed;

	@JsonProperty(value = "warnings")
	private List<String> warnings;
}
