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
 * DTO de respuesta detallada para Palet
 * Incluye toda la información del palet, nivel de embalaje, producto y operador
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaletDetailResponseDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@JsonProperty(value = "sscc")
	private String sscc;

	@JsonProperty(value = "batch_number")
	private String batchNumber;

	@JsonProperty(value = "packaging_date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate packagingDate;

	@JsonProperty(value = "product_use_by_date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate productUseByDate;

	@JsonProperty(value = "production_time")
	private String productionTime;

	@JsonProperty(value = "create_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonProperty(value = "updated_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

	@JsonProperty(value = "pack_level")
	private PackLevelInfo packLevel;

	@JsonProperty(value = "product")
	private ProductInfo product;

	@JsonProperty(value = "scanned_by")
	private UserInfo scannedBy;

	@JsonProperty(value = "workshift")
	private WorkshiftInfo workshift;

	// Campos calculados
	@JsonProperty(value = "days_until_expiry")
	private Long daysUntilExpiry;

	@JsonProperty(value = "is_expiring_soon")
	private Boolean isExpiringSoon;

	@JsonProperty(value = "is_expired")
	private Boolean isExpired;

	@JsonProperty(value = "is_critical_expiry")
	private Boolean isCriticalExpiry;

	@JsonProperty(value = "expiry_status")
	private String expiryStatus;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PackLevelInfo implements Serializable {
		private Long id;

		@JsonProperty(value = "gtin")
		private String gtin;

		@JsonProperty(value = "packing_level")
		private String packingLevel;

		@JsonProperty(value = "net_weight")
		private java.math.BigDecimal netWeight;

		@JsonProperty(value = "height_mm")
		private java.math.BigDecimal heightMM;

		@JsonProperty(value = "width_mm")
		private java.math.BigDecimal widthMM;

		@JsonProperty(value = "units_in_level")
		private Integer unitsInLevel;

		@JsonProperty(value = "stacking_limit")
		private Integer stackingLimit;

		@JsonProperty(value = "boxes_per_palet")
		private Integer boxesPerPalet;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProductInfo implements Serializable {
		private Long id;

		@JsonProperty(value = "name")
		private String name;

		@JsonProperty(value = "brand")
		private String brand;

		@JsonProperty(value = "description")
		private String description;

		@JsonProperty(value = "format_code")
		private String formatCode;

		@JsonProperty(value = "manufactured_in")
		private String manufacturedIn;

		@JsonProperty(value = "status")
		private String status;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserInfo implements Serializable {
		private Long id;

		@JsonProperty(value = "employee_number")
		private String employeeNumber;

		@JsonProperty(value = "full_name")
		private String fullName;

		@JsonProperty(value = "email")
		private String email;

		@JsonProperty(value = "job_position")
		private String jobPosition;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class WorkshiftInfo implements Serializable {
		private Long id;

		@JsonProperty(value = "date")
		private LocalDate date;

		@JsonProperty(value = "shift_type")
		private String shiftType;

		@JsonProperty(value = "shift_description")
		private String shiftDescription;
	}
}
