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

	@NotEmpty(message = "El EAN no puede estar vacío.")
	@Size(min = 13, max = 14, message = "El EAN debe tener 13 o 14 dígitos.")
	private String ean; // Este será el GTIN-14 (o GTIN-13) del ProductPackLevel

	@NotEmpty(message = "El número de lote no puede estar vacío.")
	private String batchNumber;

	@NotEmpty(message = "El número de empleado no puede estar vacío.")
	private String employeeNumber;

	@NotNull(message = "La fecha de empaque no puede ser nula.")
	private LocalDate packagingDate;

	@NotNull(message = "La fecha de caducidad no puede ser nula.")
	private LocalDate productUseByDate;

	@NotEmpty(message = "La hora de producción no puede estar vacía.")
	@Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "La hora debe estar en formato HH:mm.")
	private String productionTime;

	/**
	 * Fecha en la que el operario realizó el escaneo.
	 * Se usará para determinar el turno de trabajo.
	 */
	@NotNull(message = "La fecha de escaneo no puede ser nula.")
	private LocalDateTime scanDate;
}