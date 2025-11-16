package com.abcm0018.sai.palets.application.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.abcm0018.sai.productos.domain.enums.PackingLevel;
import com.abcm0018.sai.shift.domain.enums.ShiftType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) para notificaciones WebSocket.
 * * Contiene información enriquecida para un análisis detallado
 * por parte del equipo de logística (Paso 6).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaletNotificationDTO {

	// --- (Identificación) ---
	private String sscc;              // (De Palet) El ID único del palet
	private String batchNumber;       // (De Palet) Lote de producción
	private String gtin;              // (De ProductPackLevel) El EAN/GTIN del embalaje
	private String productSku;        // (De Product) El SKU interno
	private String productName;       // (De Product) Nombre del producto
	private String brand;             // (De Product) Marca del producto

	// --- (Logística) ---
	private PackingLevel packLevel;   // (De ProductPackLevel) Ej: "PALET", "CAJA"
	private Integer unitsInLevel;     // (De ProductPackLevel) Unidades totales en este palet
	private BigDecimal grossWeightKg; // (De ProductPackLevel) Peso bruto
	private Integer stackingLimit;    // (De ProductPackLevel) Límite de apilado (muy útil)
	private String dimensionsMm;      // (De ProductPackLevel) Ej: "1200x800x1000"

	// --- (Trazabilidad) ---
	private LocalDate scannedAt;    // (De Palet) El timestamp exacto del escaneo
	private LocalDate packagingDate;  // (De Palet) Fecha de envasado
	private LocalDate productUseByDate; // (De Palet) Fecha de CADUCIDAD
	private boolean isExpired;        // (De Palet) Flag de conveniencia

	// --- (Operativa) ---
	private String employeeName;      // (De User) Nombre del operario
	private ShiftType shiftType;      // (De Workshift -> Shift) Turno (Mañana, Tarde, Noche)
}