package com.abcm0018.sai.palets.infrastructure.messaging.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO que representa el mensaje JSON recibido de RabbitMQ.
 * Incluye validaciones sintácticas (Paso 3, Parte 1).
 */
@Data
public class PaletLecturaMessageDTO {
	@NotEmpty(message = "El SSCC no puede estar vacío.")
	@Size(min = 18, max = 18, message = "El SSCC debe tener 18 dígitos.")
	@Pattern(regexp = "^[0-9]+$", message = "El SSCC solo debe contener números.")
	private String sscc;
	private String ean; // Este será el GTIN-14 (o GTIN-13) del ProductPackLevel
	private String batchNumber;
	private String employeeNumber;
	private LocalDateTime packagingDateTime;
	private LocalDate productUseByDate;
	/**
	 * Fecha en la que el operario realizó el escaneo.
	 * Se usará para determinar el turno de trabajo.
	 */
	@NotNull(message = "La fecha de escaneo no puede ser nula.")
	private LocalDateTime scanDate;

	@NotEmpty(message = "El identificador del puesto (stationId) es obligatorio.")
	private String stationId;

	@NotEmpty(message = "El identificador de la cámara (cameraId) es obligatorio.")
	private String cameraId;
}