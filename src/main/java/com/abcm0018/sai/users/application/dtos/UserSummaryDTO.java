package com.abcm0018.sai.users.application.dtos;

import java.io.Serial;
import java.io.Serializable;

import com.abcm0018.sai.users.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO resumido de usuario para listados
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	@JsonProperty("employee_number")
	private String employeeNumber;

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

	@JsonProperty("account_status")
	private String accountStatus;
}
