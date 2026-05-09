package com.abcm0018.sai.productos.application.bulk.job;

/**
 * Enum que representa los estados posibles de un trabajo
 * de importación masiva.
 */
public enum ImportJobStatus {
	/**
	 * El trabajo ha sido creado y está en cola,
	 * esperando ser procesado.
	 */
	PENDING,

	/**
	 * El trabajo está siendo procesado activamente
	 * por un worker (@Async).
	 */
	PROCESSING,

	/**
	 * El trabajo finalizó exitosamente.
	 * Los detalles se pueden encontrar en 'resultDetails'.
	 */
	COMPLETED,

	/**
	 * El trabajo falló durante el procesamiento.
	 * Los detalles del error se pueden encontrar en 'resultDetails'.
	 */
	FAILED
}