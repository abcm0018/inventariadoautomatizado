package com.abcm0018.sai.users.application.dtos;

import com.abcm0018.sai.users.domain.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar un usuario existente
 * Todos los campos son opcionales (solo se actualizan los que vienen)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequestDTO {

	@Size(min = 3, max = 20, message = "El número de empleado debe tener entre 3 y 20 caracteres")
	@Pattern(regexp = "^[0-9]{4}$", message = "El número de empleado solo pueden ser 4 digitos")
	private String employeeNumber;

	@Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
	private String name;

	@Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
	private String surname;

	@Email(message = "El email debe ser válido")
	@Size(max = 150, message = "El email no puede exceder 150 caracteres")
	private String email;

	@Size(min = 3, max = 100, message = "El puesto debe tener entre 3 y 100 caracteres")
	private String jobPosition;

	private Role role;
}
