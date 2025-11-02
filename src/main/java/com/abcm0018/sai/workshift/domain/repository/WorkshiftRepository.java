package com.abcm0018.sai.workshift.domain.repository;

import com.abcm0018.sai.shift.domain.entity.Shift;
import com.abcm0018.sai.shift.domain.enums.ShiftType;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkshiftRepository extends JpaRepository<Workshift, Long> {
	// ========== BÚSQUEDAS POR USUARIO ==========

	/**
	 * Encuentra todos los turnos de un usuario ordenados por fecha descendente
	 */
	List<Workshift> findByUserOrderByDateDesc(User user);

	/**
	 * Encuentra turnos de un usuario con paginación
	 */
	Page<Workshift> findByUser(User user, Pageable pageable);

	/**
	 * Encuentra turnos de un usuario en una fecha específica
	 */
	List<Workshift> findByUserAndDate(User user, LocalDate date);

	/**
	 * Encuentra el turno de un usuario en una fecha específica (debería ser único)
	 */
	Optional<Workshift> findByUserAndDateAndShift(User user, LocalDate date, Shift shift);

	/**
	 * Encuentra turnos de un usuario en un rango de fechas
	 */
	List<Workshift> findByUserAndDateBetweenOrderByDateAsc(User user, LocalDate startDate, LocalDate endDate);

	/**
	 * Cuenta turnos de un usuario
	 */
	Long countByUser(User user);

	/**
	 * Cuenta turnos de un usuario en un rango de fechas
	 */
	Long countByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

	// ========== BÚSQUEDAS POR FECHA ==========

	/**
	 * Encuentra todos los turnos de una fecha específica
	 */
	List<Workshift> findByDate(LocalDate date);

	/**
	 * Encuentra turnos de una fecha con paginación
	 */
	Page<Workshift> findByDate(LocalDate date, Pageable pageable);

	/**
	 * Encuentra turnos en un rango de fechas
	 */
	List<Workshift> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);

	/**
	 * Optimizado: Encuentra turnos en un rango de fechas, TRAYENDO ADEMÁS (FETCH)
	 * la entidad User asociada en la misma consulta para evitar N+1.
	 * * Reemplaza a: findByDateBetweenOrderByDateAsc
	 */
	@Query("SELECT w FROM Workshift w JOIN FETCH w.user u WHERE w.date BETWEEN :startDate AND :endDate ORDER BY w.date ASC")
	List<Workshift> findByDateBetweenWithUsers(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate
	);

	/**
	 * Encuentra turnos después de una fecha
	 */
	List<Workshift> findByDateAfterOrderByDateAsc(LocalDate date);

	/**
	 * Encuentra turnos antes de una fecha
	 */
	List<Workshift> findByDateBeforeOrderByDateDesc(LocalDate date);

	/**
	 * Cuenta turnos en una fecha específica
	 */
	Long countByDate(LocalDate date);

	/**
	 * Cuenta turnos en un rango de fechas
	 */
	Long countByDateBetween(LocalDate startDate, LocalDate endDate);

	// ========== BÚSQUEDAS POR TURNO (SHIFT) ==========

	/**
	 * Encuentra turnos por tipo de turno (shift)
	 */
	List<Workshift> findByShift(Shift shift);

	/**
	 * Encuentra turnos por shift en una fecha específica
	 */
	List<Workshift> findByShiftAndDate(Shift shift, LocalDate date);

	/**
	 * Encuentra turnos por shift en un rango de fechas
	 */
	List<Workshift> findByShiftAndDateBetween(Shift shift, LocalDate startDate, LocalDate endDate);

	/**
	 * Cuenta turnos por shift
	 */
	Long countByShift(Shift shift);

	// ========== BÚSQUEDAS COMBINADAS ==========

	/**
	 * Encuentra turnos de un usuario y un shift específico
	 */
	List<Workshift> findByUserAndShift(User user, Shift shift);

	/**
	 * Encuentra turnos de un usuario y un shift en un rango de fechas
	 */
	List<Workshift> findByUserAndShiftAndDateBetween(User user, Shift shift, LocalDate startDate, LocalDate endDate);

	/**
	 * Encuentra el turno actual de un usuario (hoy)
	 */
	@Query("SELECT w FROM Workshift w WHERE w.user = :user AND w.date = :today")
	Optional<Workshift> findTodayWorkshiftByUser(@Param("user") User user, @Param("today") LocalDate today);

	/**
	 * Encuentra turnos futuros de un usuario
	 */
	@Query("SELECT w FROM Workshift w WHERE w.user = :user AND w.date > :today ORDER BY w.date ASC")
	List<Workshift> findFutureWorkshiftsByUser(@Param("user") User user, @Param("today") LocalDate today);

	/**
	 * Encuentra turnos pasados de un usuario
	 */
	@Query("SELECT w FROM Workshift w WHERE w.user = :user AND w.date < :today ORDER BY w.date DESC")
	List<Workshift> findPastWorkshiftsByUser(@Param("user") User user, @Param("today") LocalDate today);

	// ========== BÚSQUEDAS POR TIPO DE TURNO (SHIFTTYPE) ==========

	/**
	 * Encuentra turnos por tipo de turno (MORNING, AFTERNOON, NIGHT)
	 */
	@Query("SELECT w FROM Workshift w WHERE w.shift.shiftType = :shiftType")
	List<Workshift> findByShiftType(@Param("shiftType") ShiftType shiftType);

	/**
	 * Encuentra turnos por tipo de turno en una fecha específica
	 */
	@Query("SELECT w FROM Workshift w WHERE w.shift.shiftType = :shiftType AND w.date = :date")
	List<Workshift> findByShiftTypeAndDate(
			@Param("shiftType") ShiftType shiftType,
			@Param("date") LocalDate date
	);

	/**
	 * Encuentra turnos por tipo de turno de un usuario
	 */
	@Query("SELECT w FROM Workshift w WHERE w.user = :user AND w.shift.shiftType = :shiftType")
	List<Workshift> findByUserAndShiftType(@Param("user") User user, @Param("shiftType") ShiftType shiftType);

	/**
	 * Cuenta turnos por tipo de turno
	 */
	@Query("SELECT COUNT(w) FROM Workshift w WHERE w.shift.shiftType = :shiftType")
	Long countByShiftType(@Param("shiftType") ShiftType shiftType);

	// ========== VALIDACIONES Y EXISTENCIA ==========

	/**
	 * Verifica si existe un turno para un usuario en una fecha
	 */
	boolean existsByUserAndDate(User user, LocalDate date);

	/**
	 * Verifica si existe un turno específico
	 */
	boolean existsByUserAndDateAndShift(User user, LocalDate date, Shift shift);

	/**
	 * Verifica si un usuario tiene turnos en un rango de fechas
	 */
	boolean existsByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

	// ========== BÚSQUEDAS PARA PLANIFICACIÓN ==========

	/**
	 * Encuentra turnos de la semana actual
	 */
	@Query("SELECT w FROM Workshift w WHERE w.date BETWEEN :startOfWeek AND :endOfWeek ORDER BY w.date, w.shift.startTime")
	List<Workshift> findWorkshiftsOfWeek(@Param("startOfWeek") LocalDate startOfWeek, @Param("endOfWeek") LocalDate endOfWeek);

	/**
	 * Encuentra turnos del mes actual
	 */
	@Query("SELECT w FROM Workshift w WHERE w.date BETWEEN :startOfMonth AND :endOfMonth ORDER BY w.date, w.shift.startTime")
	List<Workshift> findWorkshiftsOfMonth(@Param("startOfMonth") LocalDate startOfMonth, @Param("endOfMonth") LocalDate endOfMonth);

	/**
	 * Encuentra todos los usuarios que trabajan en una fecha específica
	 */
	@Query("SELECT DISTINCT w.user FROM Workshift w WHERE w.date = :date")
	List<User> findUsersWorkingOnDate(@Param("date") LocalDate date);

	/**
	 * Encuentra turnos que se solapan (mismo usuario, misma fecha, diferentes shifts)
	 * Útil para detectar conflictos de programación
	 */
	@Query("SELECT w FROM Workshift w WHERE w.user = :user AND w.date = :date")
	List<Workshift> findPotentialConflicts(@Param("user") User user, @Param("date") LocalDate date);

	// ========== ESTADÍSTICAS Y REPORTES ==========

	/**
	 * Cuenta turnos por usuario y tipo de turno
	 */
	@Query("SELECT w.shift.shiftType, COUNT(w) FROM Workshift w WHERE w.user = :user GROUP BY w.shift.shiftType")
	List<Object[]> countWorkshiftsByUserAndShiftType(@Param("user") User user);

	/**
	 * Cuenta turnos por fecha (para gráficos de ocupación)
	 */
	@Query("SELECT w.date, COUNT(w) FROM Workshift w " +
			"WHERE w.date BETWEEN :startDate AND :endDate " +
			"GROUP BY w.date " +
			"ORDER BY w.date")
	List<Object[]> countWorkshiftsByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	/**
	 * Cuenta turnos por tipo de turno en un rango de fechas
	 */
	@Query("SELECT w.shift.shiftType, COUNT(w) FROM Workshift w " +
			"WHERE w.date BETWEEN :startDate AND :endDate " +
			"GROUP BY w.shift.shiftType")
	List<Object[]> countByShiftTypeAndDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	/**
	 * Obtiene la última asignación de turno de un usuario
	 */
	@Query("SELECT w FROM Workshift w WHERE w.user = :user ORDER BY w.date DESC, w.createdAt DESC")
	Optional<Workshift> findLastWorkshiftByUser(@Param("user") User user);

	/**
	 * Obtiene el próximo turno de un usuario
	 */
	@Query("SELECT w FROM Workshift w WHERE w.user = :user AND w.date >= :today ORDER BY w.date ASC, w.shift.startTime ASC")
	Optional<Workshift> findNextWorkshiftByUser(@Param("user") User user, @Param("today") LocalDate today);

	// ========== OPERACIONES DE LIMPIEZA ==========

	/**
	 * Elimina turnos antiguos (para limpieza de datos)
	 */
	@Modifying
	@Query("DELETE FROM Workshift w WHERE w.date < :cutoffDate")
	void deleteWorkshiftsOlderThan(@Param("cutoffDate") LocalDate cutoffDate);

	/**
	 * Cuenta turnos modificados
	 */
	@Query("SELECT COUNT(w) FROM Workshift w WHERE w.updatedAt IS NOT NULL")
	Long countModifiedWorkshifts();

	/**
	 * Encuentra turnos sin palets escaneados (para auditoría)
	 */
	@Query("SELECT w FROM Workshift w WHERE w.date < :today AND SIZE(w.palets) = 0")
	List<Workshift> findWorkshiftsWithoutPalets(@Param("today") LocalDate today);

	/**
	 * Encuentra turnos sin fichajes (para auditoría)
	 */
	@Query("SELECT w FROM Workshift w WHERE w.date < :today AND SIZE(w.timesheets) = 0")
	List<Workshift> findWorkshiftsWithoutTimesheets(@Param("today") LocalDate today);
}
