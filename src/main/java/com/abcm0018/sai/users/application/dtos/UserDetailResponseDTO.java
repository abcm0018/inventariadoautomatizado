package com.abcm0018.sai.users.application.dtos;

import java.io.Serializable;
import java.io.Serial;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con detalles completos del usuario incluyendo estadísticas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponseDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	// Información básica
	@JsonProperty("user_info")
	private UserResponseDTO userInfo;

	// Estadísticas de trabajo
	@JsonProperty("total_workshifts")
	private Long totalWorkshifts;

	@JsonProperty("total_scanned_palets")
	private Long totalScannedPalets;

	@JsonProperty("has_workshifts")
	private Boolean hasWorkshifts;

	@JsonProperty("has_scanned_palets")
	private Boolean hasScannedPalets;

	// Estado actual
	@JsonProperty("has_workshift_today")
	private Boolean hasWorkshiftToday;

	@JsonProperty("current_workshift_info")
	private CurrentWorkshiftInfo currentWorkshiftInfo;

	/**
	 * Información del turno actual
	 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CurrentWorkshiftInfo implements Serializable {
		private Long workshiftId;
		private String shiftType;
		private String shiftDescription;
		private Boolean hasCheckedIn;
		private Boolean hasCheckedOut;
	}
}
