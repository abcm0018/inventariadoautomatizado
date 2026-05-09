package com.abcm0018.sai.users.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.abcm0018.sai.users.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta completa de usuario
 * Incluye toda la información excepto la contraseña
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@JsonProperty("employee_number")
	private String employeeNumber;

	private String name;
	private String surname;

	@JsonProperty("full_name")
	private String fullName;

	private String email;

	@JsonProperty("job_position")
	private String jobPosition;

	private Role role;

	@JsonProperty("role_display_name")
	private String roleDisplayName;

	private Boolean active;
	private Boolean blocked;
	private Boolean expired;

	@JsonProperty("registration_date")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate registrationDate;

	@JsonProperty("created_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonProperty("updated_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

	// Campos calculados
	@JsonProperty("can_work")
	private Boolean canWork;

	@JsonProperty("account_status")
	private String accountStatus;

	@JsonProperty("days_since_registration")
	private Long daysSinceRegistration;
}
