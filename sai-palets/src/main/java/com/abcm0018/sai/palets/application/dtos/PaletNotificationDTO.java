package com.abcm0018.sai.palets.application.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.abcm0018.sai.productos.domain.enums.PackingLevel;
import com.abcm0018.sai.shift.domain.enums.ShiftType;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload de notificación enviado al frontend vía WebSocket (STOMP /topic/palets)
 * y devuelto por el endpoint REST /api/v1/palets/recent.
 * <p>
 * Contiene los datos mínimos necesarios para renderizar la fila del ActivityFeed
 * del Dashboard en tiempo real: identificación del palet, producto, operador y
 * timestamp de escaneo para el cálculo del turno.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaletNotificationDTO {

	// Identificación
	private Long paletId;
	private String sscc;
	private String batchNumber;
	private String gtin;
	private String productSku;
	private String productName;
	private String brand;

	// Logística
	private PackingLevel packLevel;
	private Integer unitsInLevel;
	private BigDecimal grossWeightKg;
	private Integer stackingLimit;
	private String dimensionsMm;

	// Trazabilidad
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime scannedAt;
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime packagingDateTime;
	private LocalDate productUseByDate;
	private boolean isExpired;

	// Operativa
	private String employeeName;
	private ShiftType shiftType;
}