package com.abcm0018.sai.users.application.service;

import com.abcm0018.sai.users.application.dtos.*;
import com.abcm0018.sai.users.domain.enums.Role;
import com.abcm0018.sai.users.exceptions.UserServiceException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para la gestión de usuarios del sistema
 * Maneja operaciones CRUD, autenticación, autorización y estadísticas
 */
public interface UserService {

	/**
	 * Crea un nuevo usuario en el sistema
	 * Encripta la contraseña automáticamente
	 *
	 * @param requestDTO Datos del nuevo usuario
	 * @return Usuario creado
	 */
	UserResponseDTO createUser(CreateUserRequestDTO requestDTO);

	/**
	 * Busca un usuario por ID
	 *
	 * @param id ID del usuario
	 * @return Usuario encontrado
	 * @throws UserServiceException si no se encuentra
	 */
	UserResponseDTO findById(Long id);

	/**
	 * Busca un usuario por número de empleado
	 *
	 * @param employeeNumber Número de empleado único
	 * @return Usuario encontrado
	 * @throws UserServiceException si no se encuentra
	 */
	UserResponseDTO findByEmployeeNumber(String employeeNumber);

	/**
	 * Busca un usuario por email
	 *
	 * @param email Email del usuario
	 * @return Usuario encontrado
	 * @throws UserServiceException si no se encuentra
	 */
	UserResponseDTO findByEmail(String email);

	/**
	 * Lista todos los usuarios con paginación
	 *
	 * @param pageable Configuración de paginación
	 * @return Página de usuarios
	 */
	Page<UserSummaryDTO> findAll(Pageable pageable);

	/**
	 * Actualiza un usuario existente
	 * Solo actualiza los campos que vienen en el DTO
	 *
	 * @param id ID del usuario
	 * @param requestDTO Datos a actualizar
	 * @return Usuario actualizado
	 */
	UserResponseDTO updateUser(Long id, UpdateUserRequestDTO requestDTO);

	/**
	 * Elimina un usuario (soft delete - lo marca como inactivo)
	 *
	 * @param id ID del usuario
	 */
	void deleteUser(Long id);

	/**
	 * Elimina un usuario permanentemente de la base de datos
	 * Solo si no tiene turnos ni palets asociados
	 *
	 * @param id ID del usuario
	 * @throws UserServiceException si tiene datos relacionados
	 */
	void permanentlyDeleteUser(Long id);


	/**
	 * Cambia la contraseña del usuario
	 * Requiere la contraseña actual para validación
	 *
	 * @param userId ID del usuario
	 * @param requestDTO Contraseñas actual y nueva
	 */
	void changePassword(Long userId, ChangePasswordRequestDTO requestDTO);

	/**
	 * Restablece la contraseña del usuario (solo admin)
	 * No requiere la contraseña actual
	 *
	 * @param userId ID del usuario
	 * @param requestDTO Nueva contraseña
	 */
	void resetPassword(Long userId, ResetPasswordRequestDTO requestDTO);

	/**
	 * Activa un usuario inactivo
	 *
	 * @param id ID del usuario
	 * @return Usuario activado
	 */
	UserResponseDTO activateUser(Long id);

	/**
	 * Desactiva un usuario
	 *
	 * @param id ID del usuario
	 * @param statusChangeDTO Razón de la desactivación
	 * @return Usuario desactivado
	 */
	UserResponseDTO deactivateUser(Long id, UserStatusChangeDTO statusChangeDTO);

	/**
	 * Bloquea un usuario por razones de seguridad
	 *
	 * @param id ID del usuario
	 * @param statusChangeDTO Razón del bloqueo
	 * @return Usuario bloqueado
	 */
	UserResponseDTO blockUser(Long id, UserStatusChangeDTO statusChangeDTO);

	/**
	 * Desbloquea un usuario
	 *
	 * @param id ID del usuario
	 * @return Usuario desbloqueado
	 */
	UserResponseDTO unblockUser(Long id);

	/**
	 * Busca usuarios por rol
	 *
	 * @param role Rol a buscar
	 * @return Lista de usuarios con ese rol
	 */
	List<UserSummaryDTO> findUsersByRole(Role role);

	/**
	 * Busca operadores activos
	 * IMPORTANTE: Usado por WorkshiftService para generar turnos
	 *
	 * @return Lista de operadores activos
	 */
	List<UserSummaryDTO> findActiveOperators();

	/**
	 * Busca administradores activos
	 *
	 * @return Lista de administradores activos
	 */
	List<UserSummaryDTO> findActiveAdmins();

	/**
	 * Busca supervisores activos
	 *
	 * @return Lista de supervisores activos
	 */
	List<UserSummaryDTO> findActiveSupervisors();

	/**
	 * Cuenta usuarios por rol
	 *
	 * @param role Rol a contar
	 * @return Cantidad de usuarios con ese rol
	 */
	Long countByRole(Role role);

	/**
	 * Lista usuarios activos con paginación
	 *
	 * @param pageable Configuración de paginación
	 * @return Página de usuarios activos
	 */
	Page<UserSummaryDTO> findActiveUsers(Pageable pageable);

	/**
	 * Busca usuarios bloqueados
	 *
	 * @return Lista de usuarios bloqueados
	 */
	List<UserSummaryDTO> findBlockedUsers();

	/**
	 * Cuenta usuarios activos
	 *
	 * @return Cantidad de usuarios activos
	 */
	Long countActiveUsers();

	/**
	 * Busca usuarios con filtros múltiples
	 *
	 * @param filterDTO Filtros a aplicar
	 * @param pageable Configuración de paginación
	 * @return Página de usuarios que cumplen los filtros
	 */
	Page<UserSummaryDTO> findWithFilters(UserFilterDTO filterDTO, Pageable pageable);

	/**
	 * Busca usuarios por nombre completo (búsqueda parcial)
	 *
	 * @param fullName Término de búsqueda
	 * @return Lista de usuarios que coinciden
	 */
	List<UserSummaryDTO> searchByFullName(String fullName);

	/**
	 * Busca usuarios que trabajan en una fecha específica
	 *
	 * @param date Fecha a consultar
	 * @return Lista de usuarios trabajando ese día
	 */
	List<UserSummaryDTO> findUsersWorkingOnDate(LocalDate date);

	/**
	 * Obtiene detalles completos de un usuario incluyendo estadísticas
	 *
	 * @param id ID del usuario
	 * @return Detalles completos del usuario
	 */
	UserDetailResponseDTO getUserDetails(Long id);

	/**
	 * Obtiene estadísticas globales de usuarios
	 *
	 * @return Estadísticas del sistema
	 */
	UserStatisticsDTO getGlobalStatistics();

	/**
	 * Obtiene los N usuarios más productivos (más palets escaneados)
	 *
	 * @param limit Cantidad de usuarios
	 * @return Lista de usuarios más productivos
	 */
	List<UserSummaryDTO> getTopUsersByProductivity(int limit);

	/**
	 * Verifica si existe un usuario con ese número de empleado
	 *
	 * @param employeeNumber Número de empleado
	 * @return true si existe
	 */
	boolean existsByEmployeeNumber(String employeeNumber);

	/**
	 * Verifica si existe un usuario con ese email
	 *
	 * @param email Email
	 * @return true si existe
	 */
	boolean existsByEmail(String email);

	/**
	 * Verifica si un usuario puede trabajar
	 * (activo, no bloqueado, rol operador)
	 *
	 * @param userId ID del usuario
	 * @return true si puede trabajar
	 */
	boolean canUserWork(Long userId);

	/**
	 * Verifica si un usuario está activo y válido
	 *
	 * @param userId ID del usuario
	 * @return true si es válido
	 */
	boolean isUserValid(Long userId);
}
