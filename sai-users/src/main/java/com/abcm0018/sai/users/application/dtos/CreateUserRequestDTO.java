package com.abcm0018.sai.users.application.dtos;

import com.abcm0018.sai.users.domain.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear un nuevo usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDTO {

	@NotBlank(message = "El nombre es obligatorio")
	@Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
	private String name;

	@NotBlank(message = "El apellido es obligatorio")
	@Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
	private String surname;

	@NotBlank(message = "El email es obligatorio")
	@Email(message = "El email debe ser válido")
	@Size(max = 150, message = "El email no puede exceder 150 caracteres")
	private String email;

	@NotBlank(message = "La contraseña es obligatoria")
	@Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
	@Pattern(
			regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
			message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial"
	)
	private String password;

	@NotBlank(message = "El puesto de trabajo es obligatorio")
	@Size(min = 3, max = 100, message = "El puesto debe tener entre 3 y 100 caracteres")
	private String jobPosition;

	@NotNull(message = "El rol es obligatorio")
	private Role role;

	// Campos opcionales con valores por defecto
	@Builder.Default
	private Boolean active = true;

	@Builder.Default
	private Boolean blocked = false;

	@Builder.Default
	private Boolean expired = false;
}
