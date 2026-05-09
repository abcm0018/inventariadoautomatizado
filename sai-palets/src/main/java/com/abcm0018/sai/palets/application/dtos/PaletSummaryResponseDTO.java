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
 * DTO de respuesta resumida para Palet
 * Información mínima para listados
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaletSummaryResponseDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@JsonProperty(value = "sscc")
	private String sscc;

	@JsonProperty(value = "batch_number")
	private String batchNumber;

	@JsonProperty(value = "gtin")
	private String gtin;

	@JsonProperty(value = "product_use_by_date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate productUseByDate;

	@JsonProperty(value = "product_name")
	private String productName;

	@JsonProperty(value = "product_brand")
	private String productBrand;

	@JsonProperty(value = "scanned_at")
	private LocalDateTime createdAt;

	@JsonProperty(value = "employee_number")
	private String employeeNumber;

	@JsonProperty(value = "shift")
	private String shift;

	@JsonProperty(value = "days_until_expiry")
	private Long daysUntilExpiry;

	@JsonProperty(value = "expiry_status")
	private String expiryStatus;

	@JsonProperty(value = "is_expired")
	private Boolean isExpired;

	@JsonProperty(value = "is_expiring_soon")
	private Boolean isExpiringSoon;
}
