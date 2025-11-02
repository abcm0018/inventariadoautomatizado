package com.abcm0018.sai.workshift.domain.enums;

import lombok.Getter;

/**
 * Estados posibles de una solicitud de cambio de turno
 */
@Getter
public enum SwapRequestStatus {
	/**
	 * Solicitud pendiente de revisión
	 */
	PENDING("Pendiente"),

	/**
	 * Solicitud aprobada por supervisor/admin
	 */
	APPROVED("Aprobada"),

	/**
	 * Solicitud rechazada por supervisor/admin
	 */
	REJECTED("Rechazada"),

	/**
	 * Solicitud cancelada por el usuario solicitante
	 */
	CANCELLED("Cancelada");

	private final String displayName;

	SwapRequestStatus(String displayName) {
		this.displayName = displayName;
	}
}
