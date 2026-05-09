package com.abcm0018.sai.users.infrastructure.controller;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.abcm0018.sai.shared.response.ResponseBuilder;
import com.abcm0018.sai.shared.response.StandardResponse;
import com.abcm0018.sai.users.application.dtos.*;
import com.abcm0018.sai.users.domain.enums.Role;
import com.abcm0018.sai.users.application.service.UserService;

import com.abcm0018.sai.users.domain.enums.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static java.util.stream.Collectors.toList;

/**
 * Controlador REST para la gestión de usuarios del sistema
 * <p>
 * SEGURIDAD:
 * - ADMIN: Acceso completo a todas las operaciones
 * - SUPERVISOR: Consultas y operaciones limitadas
 * - OPERATOR: Solo consulta de información propia
 * <p>
 * OPERACIONES DISPONIBLES:
 * - CRUD completo de usuarios
 * - Gestión de contraseñas (cambio y reset)
 * - Control de estado (activación, bloqueo, desactivación)
 * - Búsquedas avanzadas con filtros
 * - Estadísticas y reportes
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints para gestión de usuarios del sistema")
public class UserController {

	private final UserService userService;

	/**
	 * Crear un nuevo usuario en el sistema
	 * Solo administradores pueden crear usuarios
	 */
	@CrossOrigin
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Crear nuevo usuario",
			description = "Crea un usuario con rol específico. La contraseña se encripta automáticamente. " +
					"Solo administradores pueden ejecutar esta operación."
	)
	public StandardResponse<UserResponseDTO> createUser(@Valid @RequestBody CreateUserRequestDTO requestDTO) {

		log.info("Creando nuevo usuario - Employee: {}, Role: {}", requestDTO.getName(), requestDTO.getRole());

		UserResponseDTO created = userService.createUser(requestDTO);

		return ResponseBuilder.withCreatedElements(HttpStatus.CREATED, true, 1, "Usuario creado exitosamente", created);
	}

	/**
	 * Obtener un usuario por ID
	 * Supervisores y admins pueden ver cualquier usuario
	 * Operadores solo pueden ver su propia información
	 */
	@CrossOrigin
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR') or #id == authentication.principal.id")
	@Operation(
			summary = "Obtener usuario por ID",
			description = "Retorna información completa del usuario. " +
					"Operadores solo pueden consultar su propia información."
	)
	public StandardResponse<UserResponseDTO> getUserById(
			@PathVariable @Parameter(description = "ID del usuario") Long id) {

		log.debug("Consultando usuario con ID: {}", id);

		UserResponseDTO user = userService.findById(id);

		return ResponseBuilder.with(HttpStatus.OK, true, "Usuario encontrado", user);
	}

	/**
	 * Obtener detalles completos de un usuario incluyendo estadísticas
	 */
	@CrossOrigin
	@GetMapping("/{id}/details")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR') or #id == authentication.principal.id")
	@Operation(
			summary = "Obtener detalles completos del usuario",
			description = "Incluye estadísticas de turnos, palets escaneados y estado de fichaje actual"
	)
	public StandardResponse<UserDetailResponseDTO> getUserDetails(@PathVariable @Parameter(description = "ID del usuario") Long id) {

		log.debug("Consultando detalles completos del usuario {}", id);

		UserDetailResponseDTO details = userService.getUserDetails(id);

		return ResponseBuilder.with(HttpStatus.OK, true, "Detalles del usuario obtenidos exitosamente", details);
	}

	/**
	 * Listar todos los usuarios con paginación
	 */
	@CrossOrigin
	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Listar todos los usuarios con paginación",
			description = "Retorna página de usuarios ordenados por fecha de registro descendente"
	)
	public StandardResponse<Page<UserSummaryDTO>> getAllUsers(
			@PageableDefault(size = 20, sort = "registrationDate", direction = Sort.Direction.DESC)
			Pageable pageable) {

		log.debug("Listando usuarios - Página: {}, Tamaño: {}",
				pageable.getPageNumber(), pageable.getPageSize());

		Page<UserSummaryDTO> users = userService.findAll(pageable);

		String message = String.format(
				"Página %d de %d (Total: %d usuarios)",
				users.getNumber() + 1,
				users.getTotalPages(),
				users.getTotalElements()
		);

		return ResponseBuilder.with(HttpStatus.OK, true, message, users);
	}

	/**
	 * Actualizar un usuario existente
	 * Solo administradores pueden actualizar usuarios
	 */
	@CrossOrigin
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Actualizar usuario",
			description = "Actualiza campos no nulos del usuario. La contraseña NO se actualiza por este endpoint."
	)
	public StandardResponse<UserResponseDTO> updateUser(
			@PathVariable @Parameter(description = "ID del usuario") Long id,
			@Valid @RequestBody UpdateUserRequestDTO requestDTO) {

		log.info("Actualizando usuario {}", id);

		UserResponseDTO updated = userService.updateUser(id, requestDTO);

		return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Usuario actualizado exitosamente", updated);
	}

	/**
	 * Desactivar un usuario (soft delete)
	 * Marca al usuario como inactivo pero mantiene sus datos
	 */
	@CrossOrigin
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Desactivar usuario",
			description = "Soft delete: marca al usuario como inactivo. " +
					"Los datos históricos (turnos, palets) se mantienen intactos."
	)
	public StandardResponse<Void> deleteUser(
			@PathVariable @Parameter(description = "ID del usuario") Long id) {

		log.info("Desactivando usuario {}", id);

		userService.deleteUser(id);

		return ResponseBuilder.withDeletedElements(
				HttpStatus.OK,
				true,
				1,
				"Usuario desactivado exitosamente"
		);
	}

	/**
	 * Eliminar un usuario permanentemente
	 * ADVERTENCIA: Acción irreversible, solo si no tiene datos relacionados
	 */
	@CrossOrigin
	@DeleteMapping("/{id}/permanent")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Eliminar usuario permanentemente (PELIGROSO)",
			description = "ACCIÓN IRREVERSIBLE. Solo se puede eliminar si NO tiene turnos ni palets asociados. " +
					"Se recomienda usar desactivación en su lugar."
	)
	public StandardResponse<Void> permanentlyDeleteUser(
			@PathVariable @Parameter(description = "ID del usuario") Long id) {

		log.warn("Eliminando PERMANENTEMENTE usuario {}", id);

		userService.permanentlyDeleteUser(id);

		return ResponseBuilder.withDeletedElements(HttpStatus.OK, true, 1, "Usuario eliminado permanentemente");
	}

	/**
	 * Buscar usuario por número de empleado
	 */
	@CrossOrigin
	@GetMapping("/employee/{employeeNumber}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Buscar usuario por número de empleado",
			description = "Usado principalmente para autenticación y validaciones"
	)
	public StandardResponse<UserResponseDTO> getUserByEmployeeNumber(
			@PathVariable @Parameter(description = "Número de empleado único") String employeeNumber) {

		log.debug("Buscando usuario por employee number: {}", employeeNumber);

		UserResponseDTO user = userService.findByEmployeeNumber(employeeNumber);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				"Usuario encontrado",
				user
		);
	}

	/**
	 * Buscar usuario por email
	 */
	@CrossOrigin
	@GetMapping("/email/{email}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Buscar usuario por email",
			description = "Retorna el usuario asociado al email proporcionado"
	)
	public StandardResponse<UserResponseDTO> getUserByEmail(
			@PathVariable @Parameter(description = "Email del usuario") String email) {

		log.debug("Buscando usuario por email: {}", email);

		UserResponseDTO user = userService.findByEmail(email);

		return ResponseBuilder.with(HttpStatus.OK, true, "Usuario encontrado", user);
	}

	/**
	 * Cambiar contraseña (requiere contraseña actual)
	 * El usuario puede cambiar su propia contraseña
	 */
	@CrossOrigin
	@PutMapping("/{id}/change-password")
	@PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
	@Operation(
			summary = "Cambiar contraseña del usuario",
			description = "Requiere la contraseña actual para validación de seguridad. " +
					"Los usuarios pueden cambiar su propia contraseña."
	)
	public StandardResponse<Void> changePassword(
			@PathVariable @Parameter(description = "ID del usuario") Long id,
			@Valid @RequestBody ChangePasswordRequestDTO requestDTO) {

		log.info("Cambiando contraseña para usuario {}", id);

		userService.changePassword(id, requestDTO);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				"Contraseña actualizada exitosamente"
		);
	}

	/**
	 * Restablecer contraseña (solo admin, NO requiere contraseña actual)
	 * Usado cuando un usuario olvida su contraseña
	 */
	@CrossOrigin
	@PutMapping("/{id}/reset-password")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Restablecer contraseña (solo admin)",
			description = "NO requiere contraseña actual. Usado cuando el usuario olvida su contraseña. " +
					"Se registra en auditoría con las notas del administrador."
	)
	public StandardResponse<Void> resetPassword(
			@PathVariable @Parameter(description = "ID del usuario") Long id,
			@Valid @RequestBody ResetPasswordRequestDTO requestDTO) {

		log.warn("Restableciendo contraseña para usuario {} - Admin action", id);

		userService.resetPassword(id, requestDTO);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				"Contraseña restablecida exitosamente. El usuario debe cambiarla en el próximo login."
		);
	}

	/**
	 * Activar un usuario inactivo
	 */
	@CrossOrigin
	@PutMapping("/{id}/activate")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Activar usuario",
			description = "Reactiva un usuario desactivado. También lo desbloquea y quita la expiración."
	)
	public StandardResponse<UserResponseDTO> activateUser(
			@PathVariable @Parameter(description = "ID del usuario") Long id) {

		log.info("Activando usuario {}", id);

		UserResponseDTO activated = userService.activateUser(id);

		return ResponseBuilder.withUpdatedElements(
				HttpStatus.OK,
				true,
				1,
				"Usuario activado exitosamente",
				activated
		);
	}

	/**
	 * Desactivar un usuario
	 */
	@CrossOrigin
	@PutMapping("/{id}/deactivate")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Desactivar usuario",
			description = "El usuario no podrá hacer login ni será incluido en planificaciones de turnos. " +
					"Requiere especificar razón para auditoría."
	)
	public StandardResponse<UserResponseDTO> deactivateUser(
			@PathVariable @Parameter(description = "ID del usuario") Long id,
			@Valid @RequestBody UserStatusChangeDTO statusChangeDTO) {

		log.info("Desactivando usuario {} - Razón: {}", id, statusChangeDTO.getReason());

		UserResponseDTO deactivated = userService.deactivateUser(id, statusChangeDTO);

		return ResponseBuilder.withUpdatedElements(
				HttpStatus.OK,
				true,
				1,
				"Usuario desactivado exitosamente",
				deactivated
		);
	}

	/**
	 * Bloquear un usuario por seguridad
	 */
	@CrossOrigin
	@PutMapping("/{id}/block")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Bloquear usuario",
			description = "Bloquea al usuario por razones de seguridad (intentos fallidos, comportamiento sospechoso). " +
					"Requiere razón obligatoria y se registra en auditoría de alta prioridad."
	)
	public StandardResponse<UserResponseDTO> blockUser(
			@PathVariable @Parameter(description = "ID del usuario") Long id,
			@Valid @RequestBody UserStatusChangeDTO statusChangeDTO) {

		log.warn("Bloqueando usuario {} - Razón: {}", id, statusChangeDTO.getReason());

		UserResponseDTO blocked = userService.blockUser(id, statusChangeDTO);

		return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Usuario bloqueado exitosamente", blocked);
	}

	/**
	 * Desbloquear un usuario
	 */
	@CrossOrigin
	@PutMapping("/{id}/unblock")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Desbloquear usuario",
			description = "Quita el bloqueo de seguridad y reactiva al usuario automáticamente"
	)
	public StandardResponse<UserResponseDTO> unblockUser(@PathVariable @Parameter(description = "ID del usuario") Long id) {

		log.info("Desbloqueando usuario {}", id);

		UserResponseDTO unblocked = userService.unblockUser(id);

		return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Usuario desbloqueado exitosamente", unblocked);
	}

	/**
	 * Buscar usuarios por rol
	 */
	@CrossOrigin
	@GetMapping("/role/{role}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Buscar usuarios por rol",
			description = "Retorna todos los usuarios con el rol especificado (ADMIN, SUPERVISOR, OPERATOR, WAREHOUSE_KEEPER)"
	)
	public StandardResponse<List<UserSummaryDTO>> getUsersByRole(
			@PathVariable @Parameter(description = "Rol del usuario") Role role) {

		log.debug("Buscando usuarios con rol: {}", role);

		List<UserSummaryDTO> users = userService.findUsersByRole(role);

		String message = String.format("Encontrados %d usuarios con rol %s", users.size(), role.getDisplayName());

		return ResponseBuilder.with(HttpStatus.OK, true, message, users);
	}

	/**
	 * Obtener operadores activos
	 * CRÍTICO: Usado por el scheduler de turnos automáticos
	 */
	@CrossOrigin
	@GetMapping("/operators/active")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener operadores activos",
			description = "CRÍTICO: Usado por la planificación automática de turnos. Retorna operadores activos, no bloqueados y no expirados."
	)
	public StandardResponse<List<UserSummaryDTO>> getActiveOperators() {

		log.debug("Obteniendo operadores activos");

		List<UserSummaryDTO> operators = userService.findActiveOperators();

		String message = String.format("Encontrados %d operadores activos disponibles para trabajo", operators.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, operators);
	}

//	/**
//	 * Obtener administradores activos
//	 */
//	@CrossOrigin
//	@GetMapping("/admins/active")
//	@PreAuthorize("hasRole('ADMIN')")
//	@Operation(
//			summary = "Obtener administradores activos",
//			description = "Retorna lista de todos los administradores activos del sistema"
//	)
//	public StandardResponse<List<UserSummaryDTO>> getActiveAdmins() {
//
//		log.debug("Obteniendo administradores activos");
//
//		List<UserSummaryDTO> admins = userService.findActiveAdmins();
//
//		return ResponseBuilder.with(
//				HttpStatus.OK,
//				true,
//				String.format("Encontrados %d administradores activos", admins.size()),
//				admins
//		);
//	}

//	/**
//	 * Obtener supervisores activos
//	 */
//	@CrossOrigin
//	@GetMapping("/supervisors/active")
//	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
//	@Operation(
//			summary = "Obtener supervisores activos",
//			description = "Retorna lista de todos los supervisores activos del sistema"
//	)
//	public StandardResponse<List<UserSummaryDTO>> getActiveSupervisors() {
//
//		log.debug("Obteniendo supervisores activos");
//
//		List<UserSummaryDTO> supervisors = userService.findActiveSupervisors();
//
//		return ResponseBuilder.with(
//				HttpStatus.OK,
//				true,
//				String.format("Encontrados %d supervisores activos", supervisors.size()),
//				supervisors
//		);
//	}

	/**
	 * Contar usuarios por rol
	 */
	@CrossOrigin
	@GetMapping("/role/{role}/count")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Contar usuarios por rol",
			description = "Retorna la cantidad total de usuarios con el rol especificado"
	)
	public StandardResponse<Long> countUsersByRole(
			@PathVariable @Parameter(description = "Rol a contar") Role role) {

		Long count = userService.countByRole(role);

		String message = String.format("Hay %d usuarios con rol %s", count, role.getDisplayName());

		return ResponseBuilder.with(HttpStatus.OK, true, message, count);
	}

	/**
	 * Listar usuarios activos con paginación
	 */
	@CrossOrigin
	@GetMapping("/active")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Listar usuarios activos",
			description = "Retorna página de usuarios con estado activo = true"
	)
	public StandardResponse<Page<UserSummaryDTO>> getActiveUsers(
			@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
			Pageable pageable) {

		log.debug("Listando usuarios activos - Página: {}", pageable.getPageNumber());

		Page<UserSummaryDTO> users = userService.findActiveUsers(pageable);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				String.format("Encontrados %d usuarios activos", users.getTotalElements()),
				users
		);
	}

	/**
	 * Listar usuarios que requieren gestión (Inactivos, Bloqueados, Expirados)
	 */
	@CrossOrigin
	@GetMapping("/management")
	@PreAuthorize("hasAnyRole('ADMIN')")
	@Operation(
			summary = "Listar usuarios que requieren gestión",
			description = "Retorna página de usuarios con estado activo = true"
	)
	public StandardResponse<Page<UserSummaryDTO>> getManagementUsers(
			@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
			Pageable pageable) {

		log.debug("Listando usuarios que requieren gestión - Página: {}", pageable.getPageNumber());

		Page<UserSummaryDTO> users = userService.findManagementUsers(pageable);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				String.format("Encontrados %d usuarios activos", users.getTotalElements()),
				users
		);
	}

//	/**
//	 * Listar usuarios bloqueados
//	 */
//	@CrossOrigin
//	@GetMapping("/blocked")
//	@PreAuthorize("hasRole('ADMIN')")
//	@Operation(
//			summary = "Listar usuarios bloqueados",
//			description = "Retorna todos los usuarios bloqueados por seguridad"
//	)
//	public StandardResponse<List<UserSummaryDTO>> getBlockedUsers() {
//
//		log.debug("Listando usuarios bloqueados");
//
//		List<UserSummaryDTO> blocked = userService.findBlockedUsers();
//
//		return ResponseBuilder.with(
//				HttpStatus.OK,
//				true,
//				String.format("Encontrados %d usuarios bloqueados", blocked.size()),
//				blocked
//		);
//	}

	/**
	 * Contar usuarios activos
	 */
	@CrossOrigin
	@GetMapping("/active/count")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Contar usuarios activos",
			description = "Retorna el total de usuarios con estado activo"
	)
	public StandardResponse<Long> countActiveUsers() {

		Long count = userService.countActiveUsers();

		return ResponseBuilder.with(HttpStatus.OK, true, String.format("Hay %d usuarios activos en el sistema", count), count);
	}

	/**
	 * Búsqueda avanzada con filtros múltiples
	 */
	@CrossOrigin
	@PostMapping("/search")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Búsqueda avanzada con filtros",
			description = "Permite combinar múltiples criterios: rol, estado activo, bloqueado, puesto de trabajo"
	)
	public StandardResponse<Page<UserSummaryDTO>> searchUsers(
			@RequestBody UserFilterDTO filterDTO,
			@PageableDefault(size = 20, sort = "name") Pageable pageable) {

		log.debug("Búsqueda avanzada con filtros: {}", filterDTO);

		Page<UserSummaryDTO> users = userService.findWithFilters(filterDTO, pageable);

		String message = String.format("Encontrados %d usuarios que cumplen los filtros", users.getTotalElements());

		return ResponseBuilder.with(HttpStatus.OK, true, message, users);
	}

	/**
	 * Buscar usuarios por nombre completo
	 */
	@CrossOrigin
	@GetMapping("/search/name")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Buscar por nombre completo",
			description = "Búsqueda parcial en nombre y apellido del usuario"
	)
	public StandardResponse<List<UserSummaryDTO>> searchByName(
			@RequestParam @Parameter(description = "Término de búsqueda") String name) {

		log.debug("Buscando usuarios por nombre: '{}'", name);

		List<UserSummaryDTO> users = userService.searchByFullName(name);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				String.format("Encontrados %d usuarios que coinciden con '%s'", users.size(), name),
				users
		);
	}

	/**
	 * Buscar usuarios que trabajan en una fecha específica
	 */
	@CrossOrigin
	@GetMapping("/working-on/{date}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Usuarios trabajando en una fecha",
			description = "Retorna lista de usuarios con turno asignado para la fecha especificada"
	)
	public StandardResponse<List<UserSummaryDTO>> getUsersWorkingOnDate(
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			@Parameter(description = "Fecha a consultar") LocalDate date) {

		log.debug("Buscando usuarios trabajando el: {}", date);

		List<UserSummaryDTO> users = userService.findUsersWorkingOnDate(date);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				String.format("%d usuarios trabajando el %s", users.size(), date),
				users
		);
	}

	/**
	 * Obtener estadísticas globales de usuarios
	 */
	@CrossOrigin
	@GetMapping("/statistics")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Estadísticas globales de usuarios",
			description = "Incluye métricas agregadas, distribución por rol, usuarios con actividad, etc."
	)
	public StandardResponse<UserStatisticsDTO> getStatistics() {

		log.debug("Obteniendo estadísticas globales de usuarios");

		UserStatisticsDTO statistics = userService.getGlobalStatistics();

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				"Estadísticas obtenidas exitosamente",
				statistics
		);
	}

	/**
	 * Obtener top usuarios más productivos
	 */
	@CrossOrigin
	@GetMapping("/top-productive")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Top usuarios más productivos",
			description = "Ordenados por cantidad de palets escaneados"
	)
	public StandardResponse<List<UserSummaryDTO>> getTopProductiveUsers(
			@RequestParam(defaultValue = "10")
			@Parameter(description = "Cantidad de usuarios a retornar") int limit) {

		log.debug("Obteniendo top {} usuarios más productivos", limit);

		List<UserSummaryDTO> topUsers = userService.getTopUsersByProductivity(limit);

		return ResponseBuilder.with(HttpStatus.OK, true, String.format("Top %d usuarios más productivos obtenidos", limit), topUsers);
	}

	/**
	 * Verificar si existe un usuario con ese número de empleado
	 */
	@CrossOrigin
	@GetMapping("/exists/employee/{employeeNumber}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Verificar existencia por employee number",
			description = "Retorna true si existe un usuario con ese número de empleado"
	)
	public StandardResponse<Boolean> checkEmployeeNumberExists(
			@PathVariable @Parameter(description = "Número de empleado") String employeeNumber) {

		boolean exists = userService.existsByEmployeeNumber(employeeNumber);

		String message = exists
				? "El número de empleado ya está registrado"
				: "El número de empleado está disponible";

		return ResponseBuilder.with(HttpStatus.OK, true, message, exists);
	}

	/**
	 * Verificar si existe un usuario con ese email
	 */
	@CrossOrigin
	@GetMapping("/exists/email/{email}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Verificar existencia por email",
			description = "Retorna true si existe un usuario con ese email"
	)
	public StandardResponse<Boolean> checkEmailExists(
			@PathVariable @Parameter(description = "Email del usuario") String email) {

		boolean exists = userService.existsByEmail(email);

		String message = exists ? "El email ya está registrado" : "El email está disponible";

		return ResponseBuilder.with(HttpStatus.OK, true, message, exists);
	}

	/**
	 * Verificar si un usuario puede trabajar
	 */
	@CrossOrigin
	@GetMapping("/{id}/can-work")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Verificar si puede trabajar",
			description = "Valida que sea operador, esté activo, no bloqueado y no expirado"
	)
	public StandardResponse<Boolean> checkCanUserWork(
			@PathVariable @Parameter(description = "ID del usuario") Long id) {

		boolean canWork = userService.canUserWork(id);

		String message = canWork
				? "El usuario puede trabajar"
				: "El usuario NO puede trabajar (inactivo, bloqueado, expirado o no es operador)";

		return ResponseBuilder.with(HttpStatus.OK, true, message, canWork);
	}

	/**
	 * Verificar si un usuario está válido
	 */
	@CrossOrigin
	@GetMapping("/{id}/is-valid")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Verificar validez del usuario",
			description = "Valida que esté activo, no bloqueado y no expirado"
	)
	public StandardResponse<Boolean> checkUserValidity(
			@PathVariable @Parameter(description = "ID del usuario") Long id) {

		boolean isValid = userService.isUserValid(id);

		String message = isValid ? "El usuario es válido" : "El usuario NO es válido (inactivo, bloqueado o expirado)";

		return ResponseBuilder.with(HttpStatus.OK, true, message, isValid);
	}

    /**
     * Obtener todos los roles disponibles en el sistema
     */
    @CrossOrigin
    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @Operation(
            summary = "Listar roles disponibles",
            description = "Obtiene la lista de roles con su nombre técnico y nombre para mostrar"
    )
    public StandardResponse<RoleResponseDTO> getAvailableRoles() {
        log.info("Petición REST para obtener todos los roles disponibles");

		List<String> rolesStr = Role.getRoles().stream()
				.map(role -> role.name() + "|" + role.getDisplayName())
				.toList();

		return ResponseBuilder.with(HttpStatus.OK, true, "Roles recuperados correctamente", new RoleResponseDTO(rolesStr));
    }

	@CrossOrigin
	@GetMapping("/status") // Endpoint: /api/v1/users/status
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Listar estados disponibles",
			description = "Obtiene los estados de cuenta con su valor técnico y nombre amigable"
	)
	public StandardResponse<StatusResponseDTO> getAvailableStatus() {
		log.info("Petición REST para obtener todos los estados disponibles");

		List<String> statusStr = Status.getStatus().stream().map(status -> status.name()  + "|" + status.getDisplayStatus()).toList();

		return ResponseBuilder.with(HttpStatus.OK, true, "Estados recuperados correctamente", new StatusResponseDTO(statusStr));
	}
}
