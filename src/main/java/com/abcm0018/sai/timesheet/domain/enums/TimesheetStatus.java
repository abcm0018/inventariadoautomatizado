package com.abcm0018.sai.timesheet.domain.enums;

import lombok.Getter;

/**
 * Estados de un registro de timesheet
 */
@Getter
public enum TimesheetStatus {
	/**
	 * Registro abierto - usuario ha fichado entrada pero no salida
	 */
	OPEN("Abierto"),

	/**
	 * Registro cerrado - usuario ha fichado entrada y salida
	 */
	CLOSED("Cerrado"),

	/**
	 * Registro con anomalía - requiere revisión
	 */
	ANOMALY("Anomalía"),

	/**
	 * Registro corregido manualmente
	 */
	CORRECTED("Corregido");

	private final String displayName;

	TimesheetStatus(String displayName) {
		this.displayName = displayName;
	}
}
