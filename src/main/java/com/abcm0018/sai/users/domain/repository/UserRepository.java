package com.abcm0018.sai.users.domain.repository;

import com.abcm0018.sai.users.domain.enums.Role;
import com.abcm0018.sai.users.domain.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	// ========== BÚSQUEDAS POR IDENTIFICACIÓN ==========

	/**
	 * Busca un usuario por número de empleado
	 * Usado para autenticación
	 */
	Optional<User> findByEmployeeNumber(String employeeNumber);

	/**
	 * Busca un usuario por email
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Verifica si existe un usuario con ese número de empleado
	 */
	boolean existsByEmployeeNumber(String employeeNumber);

	/**
	 * Verifica si existe un usuario con ese email
	 */
	boolean existsByEmail(String email);

	/**
	 * Busca usuarios por nombre (búsqueda parcial)
	 */
	List<User> findByNameContainingIgnoreCase(String name);

	/**
	 * Busca usuarios por apellido (búsqueda parcial)
	 */
	List<User> findBySurnameContainingIgnoreCase(String surname);

	/**
	 * Busca usuarios por nombre completo (búsqueda parcial)
	 */
	@Query("SELECT u FROM User u WHERE LOWER(CONCAT(u.name, ' ', u.surname)) LIKE LOWER(CONCAT('%', :fullName, '%'))")
	List<User> findByFullNameContaining(@Param("fullName") String fullName);

	// ========== BÚSQUEDAS POR ROL ==========

	/**
	 * Encuentra todos los usuarios por rol
	 */
	List<User> findByRole(Role role);

	/**
	 * Encuentra usuarios activos por rol
	 * IMPORTANTE: Usado por WorkshiftService para obtener operadores
	 */
	List<User> findByRoleAndActiveTrue(Role role);

	/**
	 * Encuentra usuarios por rol con paginación
	 */
	Page<User> findByRole(Role role, Pageable pageable);

	/**
	 * Cuenta los usuarios por rol
	 */
	Long countByRole(Role role);

	/**
	 * Cuenta usuarios activos por rol
	 */
	Long countByRoleAndActiveTrue(Role role);

	// ========== BÚSQUEDAS POR ESTADO ==========

	/**
	 * Encuentra todos los usuarios activos
	 */
	List<User> findByActiveTrue();

	/**
	 * Encuentra usuarios activos con paginación
	 */
	Page<User> findByActiveTrue(Pageable pageable);

	/**
	 * Encuentra usuarios inactivos
	 */
	List<User> findByActiveFalse();

	/**
	 * Encuentra usuarios bloqueados
	 */
	List<User> findByBlockedTrue();

	/**
	 * Encuentra usuarios expirados
	 */
	List<User> findByExpiredTrue();

	/**
	 * Cuenta usuarios activos
	 */
	Long countByActiveTrue();

	/**
	 * Cuenta usuarios inactivos
	 */
	Long countByActiveFalse();

	/**
	 * Cuenta usuarios bloqueados
	 */
	Long countByBlockedTrue();

	// ========== BÚSQUEDAS POR PUESTO DE TRABAJO ==========

	/**
	 * Encuentra usuarios por puesto de trabajo
	 */
	List<User> findByJobPosition(String jobPosition);

	/**
	 * Busca usuarios por puesto (búsqueda parcial)
	 */
	List<User> findByJobPositionContainingIgnoreCase(String jobPosition);

	// ========== BÚSQUEDAS POR FECHA ==========

	/**
	 * Encuentra usuarios registrados en una fecha específica
	 */
	List<User> findByRegistrationDate(LocalDate date);

	/**
	 * Encuentra usuarios registrados en un rango de fechas
	 */
	List<User> findByRegistrationDateBetween(LocalDate startDate, LocalDate endDate);

	/**
	 * Encuentra usuarios registrados después de una fecha
	 */
	List<User> findByRegistrationDateAfter(LocalDate date);

	// ========== BÚSQUEDAS COMBINADAS ==========

	/**
	 * Encuentra usuarios activos por rol y puesto
	 */
	@Query("SELECT u FROM User u WHERE u.role = :role AND u.jobPosition = :jobPosition AND u.active = true")
	List<User> findActiveByRoleAndJobPosition(@Param("role") Role role, @Param("jobPosition") String jobPosition);

	/**
	 * Busca usuarios con filtros múltiples
	 */
	@Query("SELECT u FROM User u WHERE " +
			"(:role IS NULL OR u.role = :role) AND " +
			"(:active IS NULL OR u.active = :active) AND " +
			"(:blocked IS NULL OR u.blocked = :blocked) AND " +
			"(:jobPosition IS NULL OR LOWER(u.jobPosition) LIKE LOWER(CONCAT('%', :jobPosition, '%')))")
	Page<User> findWithFilters(@Param("role") Role role, @Param("active") Boolean active, @Param("blocked") Boolean blocked, @Param("jobPosition") String jobPosition, Pageable pageable);

	// ========== BÚSQUEDAS RELACIONADAS CON TURNOS ==========

	/**
	 * Encuentra usuarios que tienen turnos asignados
	 */
	@Query("SELECT DISTINCT u FROM User u JOIN u.workshifts w")
	List<User> findUsersWithWorkshifts();

	/**
	 * Encuentra usuarios que trabajan en una fecha específica
	 */
	@Query("SELECT DISTINCT u FROM User u JOIN u.workshifts w WHERE w.date = :date")
	List<User> findUsersWorkingOnDate(@Param("date") LocalDate date);

	/**
	 * Encuentra usuarios sin turnos asignados
	 */
	@Query("SELECT u FROM User u WHERE SIZE(u.workshifts) = 0 AND u.active = true AND u.role = :role")
	List<User> findActiveUsersWithoutWorkshifts(@Param("role") Role role);

	/**
	 * Cuenta turnos de un usuario
	 */
	@Query("SELECT COUNT(w) FROM User u JOIN u.workshifts w WHERE u.id = :userId")
	Long countWorkshiftsByUserId(@Param("userId") Long userId);

	// ========== BÚSQUEDAS RELACIONADAS CON PALETS ==========

	/**
	 * Encuentra usuarios que han escaneado palets
	 */
	@Query("SELECT DISTINCT u FROM User u JOIN u.scannedPalets p")
	List<User> findUsersWhoScannedPalets();

	/**
	 * Cuenta palets escaneados por un usuario
	 */
	@Query("SELECT COUNT(p) FROM User u JOIN u.scannedPalets p WHERE u.id = :userId")
	Long countScannedPaletsByUserId(@Param("userId") Long userId);

	/**
	 * Top N usuarios por palets escaneados
	 */
	@Query("SELECT u FROM User u LEFT JOIN u.scannedPalets p GROUP BY u.id ORDER BY COUNT(p) DESC")
	List<User> findTopUsersByScannedPalets(Pageable pageable);

	// ========== ESTADÍSTICAS ==========

	/**
	 * Obtiene estadísticas de usuarios por rol
	 */
	@Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
	List<Object[]> countUsersByRole();

	/**
	 * Obtiene estadísticas de usuarios activos por rol
	 */
	@Query("SELECT u.role, COUNT(u) FROM User u WHERE u.active = true GROUP BY u.role")
	List<Object[]> countActiveUsersByRole();

	/**
	 * Cuenta usuarios registrados por mes en un año
	 */
	@Query("SELECT MONTH(u.registrationDate), COUNT(u) FROM User u " +
			"WHERE YEAR(u.registrationDate) = :year " +
			"GROUP BY MONTH(u.registrationDate) " +
			"ORDER BY MONTH(u.registrationDate)")
	List<Object[]> countUsersByMonthInYear(@Param("year") int year);

	// ========== VALIDACIONES ==========

	/**
	 * Verifica si un usuario está activo y no bloqueado
	 */
	@Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u " +
			"WHERE u.id = :userId AND u.active = true AND u.blocked = false AND u.expired = false")
	boolean isUserActiveAndValid(@Param("userId") Long userId);

	/**
	 * Verifica si un usuario puede trabajar (activo, no bloqueado, rol operador)
	 */
	@Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u " +
			"WHERE u.id = :userId AND u.active = true AND u.blocked = false " +
			"AND u.role = 'OPERATOR'")
	boolean canUserWork(@Param("userId") Long userId);

	// ========== OPERACIONES DE LIMPIEZA ==========

	/**
	 * Encuentra usuarios inactivos hace más de X días
	 */
	@Query("SELECT u FROM User u WHERE u.active = false " +
			"AND u.updatedAt < :thresholdDate")
	List<User> findInactiveUsersSince(@Param("thresholdDate") java.time.LocalDateTime thresholdDate);

	/**
	 * Encuentra cuentas expiradas sin actualizar
	 */
	@Query("SELECT u FROM User u WHERE u.expired = true AND u.active = true")
	List<User> findExpiredActiveAccounts();

	/**
	 * Obtiene el último (máximo) número de empleado registrado
	 * Ordena de forma descendente y obtiene el primero
	 *
	 * @return Optional con el employeeNumber más alto, o empty si no hay usuarios
	 */
	@Query("SELECT u.employeeNumber FROM User u ORDER BY u.employeeNumber DESC LIMIT 1")
	Optional<String> findLastEmployeeNumber();
}
