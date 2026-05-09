package com.abcm0018.sai.users.application.dtos;

import com.abcm0018.sai.users.domain.enums.Role;

import lombok.*;

/**
 * DTO para filtros de búsqueda de usuarios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterDTO {
	private Role role;
	private Boolean active;
	private Boolean blocked;
	private String jobPosition;
	private String searchTerm; // Para buscar por nombre, apellido o email
}
