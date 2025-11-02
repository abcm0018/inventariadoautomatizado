package com.abcm0018.sai.users.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para cambios de estado del usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusChangeDTO {

	@NotBlank(message = "La razón es obligatoria")
	@Size(min = 10, max = 500, message = "La razón debe tener entre 10 y 500 caracteres")
	private String reason;

	private String additionalNotes;
}