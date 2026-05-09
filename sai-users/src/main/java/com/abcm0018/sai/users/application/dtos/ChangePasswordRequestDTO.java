package com.abcm0018.sai.users.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cambiar la contraseña del usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequestDTO {
	@NotBlank(message = "La contraseña actual es obligatoria")
	private String currentPassword;

	@NotBlank(message = "La nueva contraseña es obligatoria")
	@Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
	@Pattern(
			regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
			message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial"
	)
	private String newPassword;

	@NotBlank(message = "La confirmación de contraseña es obligatoria")
	private String confirmPassword;
}
