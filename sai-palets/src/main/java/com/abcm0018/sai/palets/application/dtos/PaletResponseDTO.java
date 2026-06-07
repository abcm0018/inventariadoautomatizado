package com.abcm0018.sai.palets.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta estándar para Palet, utilizado en las vistas de historial,
 * alertas de caducidad y búsqueda avanzada de {@code PaletsPage}.
 * <p>
 * Todos los campos de fecha/hora siguen el formato ISO 8601 con separador {@code T}
 * para garantizar compatibilidad con {@code date-fns parseISO} en el frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaletResponseDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@JsonProperty(value = "sscc")
	private String sscc;

	@JsonProperty(value = "batch_number")
	private String batchNumber;

	@JsonProperty(value = "gtin")
	private String gtin;

	@JsonProperty(value = "packaging_datetime")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime packagingDateTime;

	@JsonProperty(value = "product_use_by_date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate productUseByDate;

	@JsonProperty(value = "create_at")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonProperty(value = "updated_at")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime updatedAt;

	@JsonProperty(value = "pack_level_id")
	private Long packLevelId;

	@JsonProperty(value = "product_id")
	private Long productId;

	@JsonProperty(value = "product_name")
	private String productName;

	@JsonProperty(value = "product_brand")
	private String productBrand;

	@JsonProperty(value = "user_id")
	private Long userId;

	@JsonProperty(value = "employee_number")
	private String employeeNumber;

	@JsonProperty(value = "user_fullname")
	private String userFullName;

	@JsonProperty(value = "scanned_at")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime scannedAt;

	@JsonProperty(value = "workshift_id")
	private Long workshiftId;

	@JsonProperty(value = "workshift_date")
	private LocalDate workshiftDate;

	@JsonProperty(value = "shift_type")
	private String shiftType;

	@JsonProperty(value = "shift_display_name")
	private String shiftDisplayName;

	// Campos calculados
	@JsonProperty(value = "days_until_expiry")
	private Long daysUntilExpiry;

	@JsonProperty(value = "is_expiring_soon")
	private Boolean isExpiringSoon;

	@JsonProperty(value = "is_expired")
	private Boolean isExpired;
}
