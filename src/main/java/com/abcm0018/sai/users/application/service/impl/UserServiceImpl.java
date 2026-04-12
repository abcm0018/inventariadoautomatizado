package com.abcm0018.sai.users.application.service.impl;

import com.abcm0018.sai.email.domain.events.PasswordResetEvent;
import com.abcm0018.sai.email.domain.events.UserCreatedEvent;
import com.abcm0018.sai.shared.constants.CustomErrorCode;
import com.abcm0018.sai.timesheet.domain.entity.Timesheet;
import com.abcm0018.sai.timesheet.domain.repository.TimesheetRepository;
import com.abcm0018.sai.users.application.mapper.UserMapper;
import com.abcm0018.sai.users.domain.enums.Role;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.users.domain.enums.Status;
import com.abcm0018.sai.users.domain.repository.UserRepository;
import com.abcm0018.sai.users.exceptions.UserServiceException;
import com.abcm0018.sai.users.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import com.abcm0018.sai.users.application.dtos.*;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio de gestión de usuarios
 * <p>
 * RESPONSABILIDADES:
 * - CRUD completo de usuarios con validaciones de negocio
 * - Gestión de contraseñas con encriptación segura
 * - Control de estado (activación, bloqueo, expiración)
 * - Búsquedas avanzadas con filtros y paginación
 * - Estadísticas y reportes de usuarios
 * - Caché Redis para optimizar consultas frecuentes
 * <p>
 * SEGURIDAD:
 * - Contraseñas siempre encriptadas con BCrypt
 * - Validación de constraints únicos (email, employee number)
 * - Control de permisos por rol
 * - Auditoría de cambios críticos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserMapper userMapper;
	private final UserRepository userRepository;
	private final TimesheetRepository timesheetRepository;
	private final PasswordEncoder passwordEncoder;
	private final ApplicationEventPublisher eventPublisher;

	// Constantes de negocio
	private static final int MIN_PASSWORD_LENGTH = 8;
	private static final int MAX_PASSWORD_LENGTH = 100;

	/**
	 * Crea un nuevo usuario en el sistema
	 * <p>
	 * VALIDACIONES:
	 * - Employee number único
	 * - Email único
	 * - Contraseña cumple requisitos de seguridad
	 * - Todos los campos obligatorios presentes
	 * <p>
	 * PROCESO:
	 * 1. Valida constraints únicos
	 * 2. Valida formato de contraseña
	 * 3. Encripta contraseña con BCrypt
	 * 4. Crea usuario en BD
	 * 5. Invalida caché de usuarios
	 *
	 * @param requestDTO Datos del nuevo usuario
	 * @return Usuario creado con todos sus datos
	 * @throws UserServiceException si hay errores de validación
	 */
	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "users", allEntries = true),
			@CacheEvict(value = "usersByRole", allEntries = true),
			@CacheEvict(value = "activeUsers", allEntries = true)
	})
	public UserResponseDTO createUser(CreateUserRequestDTO requestDTO) {
		log.info("Creando nuevo usuario - Email: {}, Role: {}", requestDTO.getEmail(), requestDTO.getRole());

		// Validaciones de negocio
		validateUniqueEmail(requestDTO.getEmail());
		validatePassword(requestDTO.getPassword());

		// Convertir DTO a entidad
		User user = userMapper.toUser(requestDTO);

		// Establecer employee number
		user.setEmployeeNumber(generateNextEmployeeNumber());

		// Encriptar contraseña
		String encryptedPassword = passwordEncoder.encode(requestDTO.getPassword());
		user.setPassword(encryptedPassword);

		// Establecer fecha de registro
		user.setRegistrationDate(LocalDate.now());

		// Guardar en BD
		User saved = userRepository.save(user);

		log.info("Usuario creado exitosamente - ID: {}, Role: {}", saved.getId(), saved.getRole());

		createAndPublishUserCreateEvent(requestDTO, saved);

		return userMapper.toResponse(saved);
	}

	/**
	 * Busca un usuario por ID
	 * Cachea el resultado para optimizar consultas frecuentes
	 *
	 * @param id ID del usuario
	 * @return Usuario encontrado
	 * @throws UserServiceException si no existe
	 */
	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "users", key = "#id", unless = "#result == null")
	public UserResponseDTO findById(Long id) {
		log.debug("Buscando usuario por ID: {}", id);

		User user = findUserEntityById(id);

		return userMapper.toResponse(user);
	}

	/**
	 * Busca un usuario por número de empleado
	 * Usado principalmente para autenticación
	 *
	 * @param employeeNumber Número de empleado único
	 * @return Usuario encontrado
	 * @throws UserServiceException si no existe
	 */
	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "usersByEmployeeNumber", key = "#employeeNumber", unless = "#result == null")
	public UserResponseDTO findByEmployeeNumber(String employeeNumber) {
		log.debug("Buscando usuario por employee number: {}", employeeNumber);

		User user = userRepository.findByEmployeeNumber(employeeNumber)
				.orElseThrow(() -> new UserServiceException(CustomErrorCode.NOT_FOUND, "Usuario no encontrado con número de empleado: " + employeeNumber, HttpStatus.NOT_FOUND));

		return userMapper.toResponse(user);
	}

	/**
	 * Busca un usuario por email
	 *
	 * @param email Email del usuario
	 * @return Usuario encontrado
	 * @throws UserServiceException si no existe
	 */
	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "usersByEmail", key = "#email", unless = "#result == null")
	public UserResponseDTO findByEmail(String email) {
		log.debug("Buscando usuario por email: {}", email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserServiceException(CustomErrorCode.NOT_FOUND, "Usuario no encontrado con email: " + email, HttpStatus.NOT_FOUND));

		return userMapper.toResponse(user);
	}

	/**
	 * Lista todos los usuarios con paginación
	 * NO se cachea por ser una lista grande y dinámica
	 *
	 * @param pageable Configuración de paginación
	 * @return Página de usuarios
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<UserSummaryDTO> findAll(Pageable pageable) {
		log.debug("Listando todos los usuarios - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());

		Page<User> users = userRepository.findAll(pageable);

		return users.map(userMapper::toSummary);
	}

	/**
	 * Actualiza un usuario existente
	 * Solo actualiza los campos que vienen en el DTO (no nulos)
	 * <p>
	 * VALIDACIONES:
	 * - Si cambia employee number, debe ser único
	 * - Si cambia email, debe ser único
	 * - No se puede cambiar la contraseña por este método
	 * <p>
	 * PROCESO:
	 * 1. Valida que exista el usuario
	 * 2. Valida constraints únicos si cambian
	 * 3. Actualiza solo campos no nulos
	 * 4. Actualiza caché
	 *
	 * @param id ID del usuario
	 * @param requestDTO Datos a actualizar
	 * @return Usuario actualizado
	 * @throws UserServiceException si hay conflictos
	 */
	@Override
	@Transactional
	@Caching(
			put = @CachePut(value = "users", key = "#id"),
			evict = {
					@CacheEvict(value = "usersByEmployeeNumber", allEntries = true),
					@CacheEvict(value = "usersByEmail", allEntries = true),
					@CacheEvict(value = "usersByRole", allEntries = true),
					@CacheEvict(value = "activeUsers", allEntries = true)
			}
	)
	public UserResponseDTO updateUser(Long id, UpdateUserRequestDTO requestDTO) {
		log.info("Actualizando usuario {}", id);

		User existingUser = findUserEntityById(id);

		// Validar cambios en campos únicos
		if (requestDTO.getEmployeeNumber() != null &&
				!existingUser.getEmployeeNumber().equals(requestDTO.getEmployeeNumber())) {
			validateUniqueEmployeeNumber(requestDTO.getEmployeeNumber());
		}

		if (requestDTO.getEmail() != null &&
				!existingUser.getEmail().equals(requestDTO.getEmail())) {
			validateUniqueEmail(requestDTO.getEmail());
		}

		if (requestDTO.getStatus() != null) {
			applyStatusToEntity(requestDTO.getStatus(), existingUser);
		}

		// Actualizar usando el mapper (solo campos no nulos)
		userMapper.updateEntityFromRequest(requestDTO, existingUser);

		User saved = userRepository.save(existingUser);

		log.info("Usuario {} actualizado exitosamente", id);

		return userMapper.toResponse(saved);
	}

	/**
	 * Elimina un usuario (soft delete)
	 * Solo marca al usuario como inactivo, no lo borra de la BD
	 * <p>
	 * IMPORTANTE: Los datos históricos se mantienen intactos
	 * - Turnos asignados permanecen
	 * - Palets escaneados permanecen
	 * - Fichajes permanecen
	 *
	 * @param id ID del usuario
	 */
	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "users", key = "#id"),
			@CacheEvict(value = "usersByEmployeeNumber", allEntries = true),
			@CacheEvict(value = "usersByEmail", allEntries = true),
			@CacheEvict(value = "activeUsers", allEntries = true)
	})
	public void deleteUser(Long id) {
		log.info("Desactivando usuario {}", id);

		User user = findUserEntityById(id);

		if(user.isBlocked()){
			throw new UserServiceException(CustomErrorCode.BAD_REQUEST, "Usuario con número de empleado: " + user.getEmployeeNumber() + " no se puede borrar debido a que esta bloqueado", HttpStatus.BAD_REQUEST);
		}

		// Soft delete: marcar como inactivo
		user.setActive(false);

		userRepository.save(user);

		log.info("Usuario {} borrado temporalmente", id);
	}

	/**
	 * Elimina un usuario permanentemente de la BD
	 * <p>
	 * RESTRICCIONES:
	 * - Solo si NO tiene turnos asignados
	 * - Solo si NO ha escaneado palets
	 * - Solo administradores pueden ejecutar esto
	 * <p>
	 * ADVERTENCIA: Esta acción es IRREVERSIBLE
	 *
	 * @param id ID del usuario
	 * @throws UserServiceException si tiene datos relacionados
	 */
	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "users", allEntries = true),
			@CacheEvict(value = "usersByEmployeeNumber", allEntries = true),
			@CacheEvict(value = "usersByEmail", allEntries = true),
			@CacheEvict(value = "usersByRole", allEntries = true),
			@CacheEvict(value = "activeUsers", allEntries = true)
	})
	public void permanentlyDeleteUser(Long id) {
		log.warn("Eliminando PERMANENTEMENTE usuario {}", id);

		User user = findUserEntityById(id);

		// Validar que no tenga datos relacionados
		validateCanPermanentlyDelete(user);

		userRepository.delete(user);

		log.warn("Usuario {} eliminado PERMANENTEMENTE", id);
	}

	/**
	 * Cambia la contraseña del usuario
	 * Requiere la contraseña actual para validación de seguridad
	 * <p>
	 * VALIDACIONES:
	 * - Contraseña actual es correcta
	 * - Nueva contraseña cumple requisitos
	 * - Nueva contraseña confirmada correctamente
	 * <p>
	 * SEGURIDAD:
	 * - Nueva contraseña se encripta con BCrypt
	 * - Se registra en auditoría
	 *
	 * @param userId ID del usuario
	 * @param requestDTO Contraseñas actual y nueva
	 * @throws UserServiceException si la contraseña actual es incorrecta
	 */
	@Override
	@Transactional
	@CacheEvict(value = "users", key = "#userId")
	public void changePassword(Long userId, ChangePasswordRequestDTO requestDTO) {
		log.info("Cambiando contraseña para usuario {}", userId);

		User user = findUserEntityById(userId);

		// Verificar contraseña actual
		if (!passwordEncoder.matches(requestDTO.getCurrentPassword(), user.getPassword())) {
			log.warn("Intento de cambio de contraseña fallido - Contraseña actual incorrecta");
			throw new UserServiceException(CustomErrorCode.UNAUTHORIZED, "La contraseña actual es incorrecta", HttpStatus.UNAUTHORIZED);
		}

		// Verificar confirmación
		if (!requestDTO.getNewPassword().equals(requestDTO.getConfirmPassword())) {
			throw new UserServiceException(CustomErrorCode.BAD_REQUEST, "La nueva contraseña y su confirmación no coinciden", HttpStatus.BAD_REQUEST
			);
		}

		// Validar nueva contraseña
		validatePassword(requestDTO.getNewPassword());

		// Actualizar contraseña
		user.setPassword(passwordEncoder.encode(requestDTO.getNewPassword()));
		userRepository.save(user);

		log.info("Contraseña actualizada exitosamente para usuario {}", userId);

		// TODO: Enviar email de confirmación
		// emailService.sendPasswordChangedEmail(user);
		// TODO: Registrar en auditoría
		// auditService.logPasswordChanged(user);
	}

	/**
	 * Restablece la contraseña del usuario (solo admin)
	 * NO requiere la contraseña actual
	 * <p>
	 * USO: Cuando un usuario olvida su contraseña
	 * <p>
	 * SEGURIDAD:
	 * - Solo administradores pueden ejecutar esto
	 * - Se genera una contraseña temporal
	 * - Usuario debe cambiarla en el primer login
	 *
	 * @param userId ID del usuario
	 * @param requestDTO Nueva contraseña y notas
	 */
	@Override
	@Transactional
	@CacheEvict(value = "users", key = "#userId")
	public void resetPassword(Long userId, ResetPasswordRequestDTO requestDTO) {
		log.info("Restableciendo contraseña para usuario {}", userId);

		User user = findUserEntityById(userId);

		// Validar nueva contraseña
		validatePassword(requestDTO.getNewPassword());

		// Actualizar contraseña
		user.setPassword(passwordEncoder.encode(requestDTO.getNewPassword()));

		// Marcar como pendiente de cambio (opcional)
		// user.setPasswordChangeRequired(true);

		userRepository.save(user);

		log.info("Contraseña restablecida exitosamente para usuario {}", userId);

		createAndPublishPasswordResetEvent(requestDTO, user);
	}

	/**
	 * Activa un usuario inactivo
	 * También desbloquea y quita la expiración
	 *
	 * @param id ID del usuario
	 * @return Usuario activado
	 */
	@Override
	@Transactional
	@Caching(
			put = @CachePut(value = "users", key = "#id"),
			evict = {
					@CacheEvict(value = "activeUsers", allEntries = true),
					@CacheEvict(value = "usersByRole", allEntries = true)
			}
	)
	public UserResponseDTO activateUser(Long id) {
		log.info("Activando usuario {}", id);

		User user = findUserEntityById(id);

		user.setActive(true);
		user.setBlocked(false);
		user.setExpired(false);

		User activated = userRepository.save(user);

		log.info("Usuario {} activado exitosamente", id);

		return userMapper.toResponse(activated);
	}

	/**
	 * Desactiva un usuario
	 * <p>
	 * EFECTOS:
	 * - No podrá hacer login
	 * - No aparecerá en planificaciones de turnos
	 * - Sus datos históricos se mantienen
	 *
	 * @param id ID del usuario
	 * @param statusChangeDTO Razón de la desactivación
	 * @return Usuario desactivado
	 */
	@Override
	@Transactional
	@Caching(
			put = @CachePut(value = "users", key = "#id"),
			evict = {
					@CacheEvict(value = "activeUsers", allEntries = true),
					@CacheEvict(value = "usersByRole", allEntries = true)
			}
	)
	public UserResponseDTO deactivateUser(Long id, UserStatusChangeDTO statusChangeDTO) {
		log.info("Desactivando usuario {} - Razón: {}", id, statusChangeDTO.getReason());

		User user = findUserEntityById(id);

		user.setActive(false);

		User deactivated = userRepository.save(user);

		log.info("Usuario {} desactivado exitosamente", id);

		// TODO: Registrar razón en auditoría
		// auditService.logUserDeactivated(user, statusChangeDTO.getReason());
		// TODO: Notificar al usuario
		// emailService.sendUserDeactivatedEmail(user, statusChangeDTO.getReason());

		return userMapper.toResponse(deactivated);
	}

	/**
	 * Bloquea un usuario por razones de seguridad
	 * <p>
	 * USO TÍPICO:
	 * - Intentos fallidos de login
	 * - Comportamiento sospechoso
	 * - Violación de políticas
	 * <p>
	 * EFECTOS:
	 * - No puede hacer login
	 * - Se marca como inactivo también
	 * - Requiere intervención de admin para desbloquear
	 *
	 * @param id ID del usuario
	 * @param statusChangeDTO Razón del bloqueo
	 * @return Usuario bloqueado
	 */
	@Override
	@Transactional
	@Caching(
			put = @CachePut(value = "users", key = "#id"),
			evict = {
					@CacheEvict(value = "activeUsers", allEntries = true),
					@CacheEvict(value = "usersByRole", allEntries = true)
			}
	)
	public UserResponseDTO blockUser(Long id, UserStatusChangeDTO statusChangeDTO) {
		log.warn("Bloqueando usuario {} - Razón: {}", id, statusChangeDTO.getReason());

		User user = findUserEntityById(id);

		user.setBlocked(true);
		user.setActive(false);

		User blocked = userRepository.save(user);

		log.warn("Usuario {} bloqueado", id);

		// TODO: Registrar en auditoría con alta prioridad
		// auditService.logUserBlocked(user, statusChangeDTO.getReason());
		// TODO: Notificar al usuario y a supervisores
		// emailService.sendUserBlockedEmail(user, statusChangeDTO.getReason());

		return userMapper.toResponse(blocked);
	}

	/**
	 * Desbloquea un usuario
	 * También lo activa automáticamente
	 *
	 * @param id ID del usuario
	 * @return Usuario desbloqueado
	 */
	@Override
	@Transactional
	@Caching(
			put = @CachePut(value = "users", key = "#id"),
			evict = {
					@CacheEvict(value = "activeUsers", allEntries = true),
					@CacheEvict(value = "usersByRole", allEntries = true)
			}
	)
	public UserResponseDTO unblockUser(Long id) {
		log.info("Desbloqueando usuario {}", id);

		User user = findUserEntityById(id);

		user.setBlocked(false);
		user.setActive(true);

		User unblocked = userRepository.save(user);

		log.info("Usuario {} desbloqueado exitosamente", id);

		return userMapper.toResponse(unblocked);
	}

	/**
	 * Busca usuarios por rol
	 * Cachea resultado para optimizar consultas frecuentes
	 *
	 * @param role Rol a buscar
	 * @return Lista de usuarios con ese rol
	 */
	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "usersByRole", key = "#role")
	public List<UserSummaryDTO> findUsersByRole(Role role) {
		log.debug("Buscando usuarios con rol: {}", role);

		List<User> users = userRepository.findByRole(role);

		return userMapper.toSummaryList(users);
	}

	/**
	 * Busca operadores activos
	 * IMPORTANTE: Usado por WorkshiftService para generar turnos automáticos
	 * <p>
	 * CRITERIOS:
	 * - Rol = OPERATOR
	 * - active = true
	 * - blocked = false
	 * - expired = false
	 *
	 * @return Lista de operadores que pueden trabajar
	 */
	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "activeOperators")
	public List<UserSummaryDTO> findActiveOperators() {
		log.debug("Buscando operadores activos");

		List<User> operators = userRepository.findByRoleAndActiveTrue(Role.OPERATOR);

		// Filtrar bloqueados y expirados
		List<User> validOperators = operators.stream()
				.filter(u -> !u.isBlocked() && !u.isExpired()).toList();

		log.info("Encontrados {} operadores activos y válidos", validOperators.size());

		return userMapper.toSummaryList(validOperators);
	}

	/**
	 * Busca administradores activos
	 *
	 * @return Lista de administradores activos
	 */
	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "activeAdmins")
	public List<UserSummaryDTO> findActiveAdmins() {
		log.debug("Buscando administradores activos");

		List<User> admins = userRepository.findByRoleAndActiveTrue(Role.ADMIN);

		return userMapper.toSummaryList(admins);
	}

	/**
	 * Busca supervisores activos
	 *
	 * @return Lista de supervisores activos
	 */
	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "activeSupervisors")
	public List<UserSummaryDTO> findActiveSupervisors() {
		log.debug("Buscando supervisores activos");

		List<User> supervisors = userRepository.findByRoleAndActiveTrue(Role.SUPERVISOR);

		return userMapper.toSummaryList(supervisors);
	}

	/**
	 * Cuenta usuarios por rol
	 *
	 * @param role Rol a contar
	 * @return Cantidad de usuarios con ese rol
	 */
	@Override
	@Transactional(readOnly = true)
	public Long countByRole(Role role) {
		return userRepository.countByRole(role);
	}

	/**
	 * Lista usuarios activos con paginación
	 *
	 * @param pageable Configuración de paginación
	 * @return Página de usuarios activos
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<UserSummaryDTO> findActiveUsers(Pageable pageable) {
		log.debug("Listando usuarios activos - Página: {}", pageable.getPageNumber());

		Page<User> users = userRepository.findByActiveTrue(pageable);

		return users.map(userMapper::toSummary);
	}

	/**
	 * Lista usuarios que requieren gestión con paginación
	 *
	 * @param pageable Configuración de paginación
	 * @return Página de usuarios que requieren gestion
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<UserSummaryDTO> findManagementUsers(Pageable pageable) {
		log.debug("Listando usuarios que requieren gestión - Página: {}", pageable.getPageNumber());

		Page<User> users = userRepository.findManagementUsers(pageable);

		return users.map(userMapper::toSummary);
	}


	/**
	 * Busca usuarios bloqueados
	 *
	 * @return Lista de usuarios bloqueados
	 */
	@Override
	@Transactional(readOnly = true)
	public List<UserSummaryDTO> findBlockedUsers() {
		log.debug("Buscando usuarios bloqueados");

		List<User> blocked = userRepository.findByBlockedTrue();

		return userMapper.toSummaryList(blocked);
	}

	/**
	 * Cuenta usuarios activos
	 *
	 * @return Cantidad de usuarios activos
	 */
	@Override
	@Transactional(readOnly = true)
	public Long countActiveUsers() {
		return userRepository.countByActiveTrue();
	}

	/**
	 * Busca usuarios con filtros múltiples
	 * <p>
	 * FILTROS SOPORTADOS:
	 * - Rol (ADMIN, SUPERVISOR, OPERATOR)
	 * - Estado activo (true/false)
	 * - Estado bloqueado (true/false)
	 * - Puesto de trabajo (búsqueda parcial)
	 * - Término de búsqueda general
	 *
	 * @param filterDTO Filtros a aplicar
	 * @param pageable Configuración de paginación
	 * @return Página de usuarios que cumplen los filtros
	 */
	@Override
	@Transactional(readOnly = true)
	public Page<UserSummaryDTO> findWithFilters(UserFilterDTO filterDTO, Pageable pageable) {
		log.debug("Búsqueda con filtros: {}", filterDTO);

		Page<User> users = userRepository.findWithFilters(
				filterDTO.getRole(),
				filterDTO.getActive(),
				filterDTO.getBlocked(),
				filterDTO.getJobPosition(),
				pageable
		);

		return users.map(userMapper::toSummary);
	}

	/**
	 * Busca usuarios por nombre completo (búsqueda parcial)
	 * Busca coincidencias en nombre + apellido
	 *
	 * @param fullName Término de búsqueda
	 * @return Lista de usuarios que coinciden
	 */
	@Override
	@Transactional(readOnly = true)
	public List<UserSummaryDTO> searchByFullName(String fullName) {
		log.debug("Buscando usuarios por nombre: '{}'", fullName);

		if (StringUtils.isEmpty(fullName)) {
			log.warn("Término de búsqueda vacío");
			return List.of();
		}

		List<User> users = userRepository.findByFullNameContaining(fullName.trim());

		return userMapper.toSummaryList(users);
	}

	/**
	 * Busca usuarios que trabajan en una fecha específica
	 * Consulta los turnos asignados
	 *
	 * @param date Fecha a consultar
	 * @return Lista de usuarios trabajando ese día
	 */
	@Override
	@Transactional(readOnly = true)
	public List<UserSummaryDTO> findUsersWorkingOnDate(LocalDate date) {
		log.debug("Buscando usuarios trabajando el: {}", date);

		List<User> users = userRepository.findUsersWorkingOnDate(date);

		return userMapper.toSummaryList(users);
	}

	/**
	 * Obtiene detalles completos de un usuario incluyendo estadísticas
	 * <p>
	 * INFORMACIÓN INCLUIDA:
	 * - Datos básicos del usuario
	 * - Total de turnos asignados
	 * - Total de palets escaneados
	 * - Turno actual (si tiene hoy)
	 * - Estado de fichaje
	 *
	 * @param id ID del usuario
	 * @return Detalles completos del usuario
	 */
	@Override
	@Transactional(readOnly = true)
	public UserDetailResponseDTO getUserDetails(Long id) {
		log.debug("Obteniendo detalles completos del usuario {}", id);

		User user = findUserEntityById(id);

		// Obtener estadísticas
		Long totalWorkshifts = userRepository.countWorkshiftsByUserId(id);
		Long totalScannedPalets = userRepository.countScannedPaletsByUserId(id);

		// Verificar si tiene turno hoy
		Boolean hasWorkshiftToday = user.hasWorkshiftToday();

		// Información del turno actual (si existe)
		UserDetailResponseDTO.CurrentWorkshiftInfo currentWorkshiftInfo = getCurrentWorkshiftInfo(hasWorkshiftToday, user);

		return userMapper.toDetailResponseWithStats(
				user,
				totalWorkshifts,
				totalScannedPalets,
				hasWorkshiftToday,
				currentWorkshiftInfo
		);
	}


	/**
	 * Obtiene estadísticas globales del sistema de usuarios
	 * <p>
	 * MÉTRICAS INCLUIDAS:
	 * - Totales por estado (activos, inactivos, bloqueados)
	 * - Distribución por rol
	 * - Usuarios con turnos asignados
	 * - Usuarios que han escaneado palets
	 *
	 * @return Estadísticas completas
	 */
	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "userStatistics")
	public UserStatisticsDTO getGlobalStatistics() {
		log.debug("Obteniendo estadísticas globales de usuarios");

		// Contadores básicos
		Long totalUsers = userRepository.count();
		Long activeUsers = userRepository.countByActiveTrue();
		Long inactiveUsers = userRepository.countByActiveFalse();
		Long blockedUsers = userRepository.countByBlockedTrue();

		// Por rol
		Long totalAdmins = countByRole(Role.ADMIN);
		Long totalSupervisors = countByRole(Role.SUPERVISOR);
		Long totalOperators = countByRole(Role.OPERATOR);

		// Estadísticas de actividad
		Long usersWithWorkshifts = (long) userRepository.findUsersWithWorkshifts().size();
		Long usersWhoScannedPalets = (long) userRepository.findUsersWhoScannedPalets().size();

		// Mapas de distribución
		Map<String, Long> usersByRole = new HashMap<>();
		List<Object[]> roleResults = userRepository.countUsersByRole();
		for (Object[] result : roleResults) {
			Role role = (Role) result[0];
			Long count = (Long) result[1];
			usersByRole.put(role.name(), count);
		}

		Map<String, Long> activeUsersByRole = new HashMap<>();
		List<Object[]> activeRoleResults = userRepository.countActiveUsersByRole();
		for (Object[] result : activeRoleResults) {
			Role role = (Role) result[0];
			Long count = (Long) result[1];
			activeUsersByRole.put(role.name(), count);
		}

		return UserStatisticsDTO.builder()
				.totalUsers(totalUsers)
				.activeUsers(activeUsers)
				.inactiveUsers(inactiveUsers)
				.blockedUsers(blockedUsers)
				.expiredUsers(0L) // TODO: Implementar
				.usersByRole(usersByRole)
				.activeUsersByRole(activeUsersByRole)
				.totalAdmins(totalAdmins)
				.totalSupervisors(totalSupervisors)
				.totalOperators(totalOperators)
				.usersWithWorkshifts(usersWithWorkshifts)
				.usersWhoScannedPalets(usersWhoScannedPalets)
				.build();
	}

	/**
	 * Obtiene los N usuarios más productivos
	 * Ordenados por cantidad de palets escaneados
	 *
	 * @param limit Cantidad de usuarios
	 * @return Lista de usuarios más productivos
	 */
	@Override
	@Transactional(readOnly = true)
	public List<UserSummaryDTO> getTopUsersByProductivity(int limit) {
		log.debug("Obteniendo top {} usuarios por productividad", limit);

		List<User> topUsers = userRepository.findTopUsersByScannedPalets(
				PageRequest.of(0, limit)
		);

		return userMapper.toSummaryList(topUsers);
	}

	/**
	 * Verifica si existe un usuario con ese número de empleado
	 *
	 * @param employeeNumber Número de empleado
	 * @return true si existe
	 */
	@Override
	public boolean existsByEmployeeNumber(String employeeNumber) {
		return userRepository.existsByEmployeeNumber(employeeNumber);
	}

	/**
	 * Verifica si existe un usuario con ese email
	 *
	 * @param email Email
	 * @return true si existe
	 */
	@Override
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	/**
	 * Verifica si un usuario puede trabajar
	 * <p>
	 * CRITERIOS:
	 * - Activo
	 * - No bloqueado
	 * - No expirado
	 * - Rol OPERATOR
	 *
	 * @param userId ID del usuario
	 * @return true si puede trabajar
	 */
	@Override
	public boolean canUserWork(Long userId) {
		return userRepository.canUserWork(userId);
	}

	/**
	 * Verifica si un usuario está activo y válido
	 * <p>
	 * CRITERIOS:
	 * - Activo
	 * - No bloqueado
	 * - No expirado
	 *
	 * @param userId ID del usuario
	 * @return true si es válido
	 */
	@Override
	public boolean isUserValid(Long userId) {
		return userRepository.isUserActiveAndValid(userId);
	}

	/**
	 * Obtiene la entidad User por ID (sin convertir a DTO)
	 * Método interno para evitar conversiones innecesarias
	 */
	private User findUserEntityById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new UserServiceException(CustomErrorCode.NOT_FOUND, "Usuario no encontrado con ID: " + id, HttpStatus.NOT_FOUND));
	}

	/**
	 * Valida que el employee number sea único
	 */
	private void validateUniqueEmployeeNumber(String employeeNumber) {
		if (userRepository.existsByEmployeeNumber(employeeNumber)) {
			throw new UserServiceException(CustomErrorCode.CONFLICT, "Ya existe un usuario con el número de empleado: " + employeeNumber, HttpStatus.CONFLICT);
		}
	}

	/**
	 * Valida que el email sea único
	 */
	private void validateUniqueEmail(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new UserServiceException(CustomErrorCode.CONFLICT,
					"Ya existe un usuario con el email: " + email,
					HttpStatus.CONFLICT
			);
		}
	}

	

	/**
	 * Genera el siguiente número de empleado disponible
	 * <p>
	 * LÓGICA:
	 * - Obtiene el último employeeNumber usado
	 * - Incrementa en 1
	 * - Formato: 4 dígitos (1000-9999)
	 * - Si no hay usuarios, empieza en 1000
	 * - Si se alcanza 9999, lanza excepción
	 *
	 * @return Siguiente número de empleado disponible
	 * @throws UserServiceException si se alcanza el límite máximo
	 */
	private String generateNextEmployeeNumber() {
		Optional<String> lastEmployeeNumber = userRepository.findLastEmployeeNumber();

		int nextEmployeeNumber;

		if (lastEmployeeNumber.isEmpty()) {
			// Primer usuario, empezar en 1000
			nextEmployeeNumber = 1000;
			log.debug("Primer usuario del sistema, asignando employeeNumber: 1000");
		} else {
			// Obtener el último número y sumar 1
			int currentNumber = Integer.parseInt(lastEmployeeNumber.get());
			nextEmployeeNumber = currentNumber + 1;

			// Validar que no se exceda el límite
			if (nextEmployeeNumber > 9999) {
				throw new UserServiceException(CustomErrorCode.CONFLICT, "Se ha alcanzado el límite máximo de números de empleado (9999). No se pueden crear más usuarios.", HttpStatus.CONFLICT);
			}

			log.debug("Generando siguiente employeeNumber: {} (anterior: {})", nextEmployeeNumber, currentNumber);
		}

		return String.format("%04d", nextEmployeeNumber);
	}


	/**
	 * Valida que la contraseña cumpla los requisitos de seguridad
	 * <p>
	 * REQUISITOS:
	 * - Entre 8 y 100 caracteres
	 * - Al menos una mayúscula
	 * - Al menos una minúscula
	 * - Al menos un número
	 * - Al menos un carácter especial
	 */
	private void validatePassword(String password) {
		if (StringUtils.isEmpty(password)) {
			throw new UserServiceException(
					CustomErrorCode.BAD_REQUEST,
					"La contraseña no puede estar vacía",
					HttpStatus.BAD_REQUEST
			);
		}

		if (password.length() < MIN_PASSWORD_LENGTH) {
			throw new UserServiceException(
					CustomErrorCode.BAD_REQUEST,
					String.format("La contraseña debe tener al menos %d caracteres", MIN_PASSWORD_LENGTH),
					HttpStatus.BAD_REQUEST
			);
		}

		if (password.length() > MAX_PASSWORD_LENGTH) {
			throw new UserServiceException(
					CustomErrorCode.BAD_REQUEST,
					String.format("La contraseña no puede exceder %d caracteres", MAX_PASSWORD_LENGTH),
					HttpStatus.BAD_REQUEST
			);
		}

		// Validación de complejidad (ya se valida en el DTO con @Pattern)
		// Aquí es redundante, pero se mantiene por seguridad
		boolean hasUpperCase = password.chars().anyMatch(Character::isUpperCase);
		boolean hasLowerCase = password.chars().anyMatch(Character::isLowerCase);
		boolean hasDigit = password.chars().anyMatch(Character::isDigit);
		boolean hasSpecial = password.chars().anyMatch(c -> "@$!%*?&".indexOf(c) >= 0);

		if (!hasUpperCase || !hasLowerCase || !hasDigit || !hasSpecial) {
			throw new UserServiceException(
					CustomErrorCode.BAD_REQUEST,
					"La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial (@$!%*?&)",
					HttpStatus.BAD_REQUEST
			);
		}
	}

	/**
	 * Valida que un usuario puede ser eliminado permanentemente
	 * Verifica que no tenga turnos ni palets asociados
	 */
	private void validateCanPermanentlyDelete(User user) {
		// Verificar turnos
		Long workshiftCount = userRepository.countWorkshiftsByUserId(user.getId());
		if (workshiftCount > 0) {
			String message = String.format("No se puede eliminar el usuario porque tiene %d turnos asignados. Use desactivación en su lugar.", workshiftCount);
			throw new UserServiceException(CustomErrorCode.CONFLICT, message, HttpStatus.CONFLICT);
		}

		// Verificar palets
		Long paletCount = userRepository.countScannedPaletsByUserId(user.getId());
		if (paletCount > 0) {
			String message = String.format("No se puede eliminar el usuario porque ha escaneado %d palets. Use desactivación en su lugar.", paletCount);
			throw new UserServiceException(CustomErrorCode.CONFLICT, message, HttpStatus.CONFLICT);
		}
	}

	/**
	 * Obtiene información del turno actual del usuario
	 * <p>
	 * IMPORTANTE: Este método debe ejecutarse dentro de una transacción
	 * para evitar LazyInitializationException
	 * <p>
	 * LÓGICA:
	 * 1. Verifica si el usuario tiene turno hoy
	 * 2. Obtiene el workshift del día actual
	 * 3. Busca el timesheet asociado al usuario
	 * 4. Verifica estados de check-in y check-out
	 *
	 * @param user Usuario a consultar
	 * @param hasWorkshiftToday Flag indicando si tiene turno hoy
	 * @return Información del turno actual o null
	 */
	private UserDetailResponseDTO.CurrentWorkshiftInfo getCurrentWorkshiftInfo(Boolean hasWorkshiftToday, User user) {

		// Si no tiene turno hoy, retornar null
		if (hasWorkshiftToday == null) {
			return null;
		}

		try {
			// Obtener el turno de hoy
			Workshift todayWorkshift = user.getTodayWorkshift();

			if (todayWorkshift == null) {
				log.warn("El usuario {} marcado con turno de hoy pero no tiene turno de hoy", user.getId());
				return null;
			}

			// Contruir información básica del turno
			UserDetailResponseDTO.CurrentWorkshiftInfo currentWorkshiftInfo =
					UserDetailResponseDTO.CurrentWorkshiftInfo.builder()
							.workshiftId(todayWorkshift.getId())
							.shiftType(todayWorkshift.getShift().getShiftType().getDisplayName())
							.shiftDescription(todayWorkshift.getShift().getDescription())
							.build();

			// Buscar el timesheet del usuario para este workshift
			// IMPORTANTE: Un usuario tiene SOLO un timesheet por workshift
			Optional<Timesheet> timesheetOpt = timesheetRepository
					.findByUserIdAndWorkshiftId(user.getId(), todayWorkshift.getId());

			if (timesheetOpt.isPresent()) {
				Timesheet timesheet = timesheetOpt.get();
				// Verificar el estado de fichaje
				currentWorkshiftInfo.setHasCheckedIn(timesheet.getCheckInAt() != null);
				currentWorkshiftInfo.setHasCheckedOut(timesheet.getCheckOutAt() != null);
			} else {
				// No ha fichado todavía
				currentWorkshiftInfo.setHasCheckedIn(false);
				currentWorkshiftInfo.setHasCheckedOut(false);

				log.debug("Usuario {} no ha fichado aún en su turno de hoy", user.getId());
			}

			return currentWorkshiftInfo;
		} catch (Exception e) {
			log.error("Error obteniendo información del turno actual para usuario {}", user.getId(), e);
			// Retornar null en lugar de propagar la excepción
			// Esto permite que getUserDetails continúe funcionando
			return null;
		}
	}

	private void createAndPublishUserCreateEvent(CreateUserRequestDTO requestDTO, User user) {
		try {
			// Publicamos el evento para enviar el email de bienvenida
			UserCreatedEvent event = new UserCreatedEvent(this, user.getId(), user.getName(), user.getSurname(),
					user.getEmployeeNumber(), user.getEmail(), requestDTO.getPassword());

			eventPublisher.publishEvent(event);
			log.debug("Evento UserCreatedEvent publicado para usuario {}", user.getId());
		} catch (Exception e) {
			// Loguear pero no fallar la creación del usuario
			log.error("Error publicando evento UserCreatedEvent: {}", e.getMessage(), e);
			// El usuario se creó correctamente, solo falló la notificación
			// TODO: Implementar reintento o cola de eventos fallidos
		}
	}

	private void createAndPublishPasswordResetEvent(ResetPasswordRequestDTO requestDTO, User user) {
		try {
			// Generamos el evento de reseteo de contraseña
			PasswordResetEvent event = new PasswordResetEvent(this, user.getName(), user.getSurname(),
					requestDTO.getNewPassword(), user.getPassword());

			eventPublisher.publishEvent(event);
			log.debug("Evento PasswordResetEvent publicado para usuario {}", user.getId());
		} catch (Exception e) {
			log.error("Error publicando evento PasswordResetEvent: {}", e.getMessage(), e);
			// El reset se completó, solo falló la notificación
		}
	}

	/**
	 * Método privado para mapear el Enum Status a los campos booleanos de User
	 */
	private void applyStatusToEntity(Status status, User user) {
		switch (status) {
			case ACTIVE -> {
				user.setActive(true);
				user.setBlocked(false);
				user.setExpired(false);
			}
			case INACTIVE -> {
				user.setActive(false);
				user.setBlocked(false);
				user.setExpired(false);
			}
			case BLOCKED -> {
				user.setActive(false); // Si está bloqueado, no debería estar activo
				user.setBlocked(true);
				user.setExpired(false);
			}
			case EXPIRED -> {
				user.setActive(false);
				user.setBlocked(false);
				user.setExpired(true);
			}
		}
	}

}
