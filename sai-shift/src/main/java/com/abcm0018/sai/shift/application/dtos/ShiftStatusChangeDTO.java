package com.abcm0018.sai.shift.application.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cambiar el estado de un turno (activar/desactivar)
 * Similar a ProductStatusChangeDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftStatusChangeDTO {

	@NotNull(message = "El nuevo estado es obligatorio")
	private Boolean active;

	@Size(min = 10, max = 500, message = "La razón debe tener entre 10 y 500 caracteres")
	private String reason;
}
