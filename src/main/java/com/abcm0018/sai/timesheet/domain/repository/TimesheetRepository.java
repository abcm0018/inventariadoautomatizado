package com.abcm0018.sai.timesheet.domain.repository;

import com.abcm0018.sai.timesheet.domain.entity.Timesheet;
import com.abcm0018.sai.timesheet.domain.enums.TimesheetStatus;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {
	/**
	 * Busca el timesheet de un workshift específico
	 */
	Optional<Timesheet> findByWorkshift(Workshift workshift);

	/**
	 * Busca timesheets por ID de workshift
	 */
	List<Timesheet> findByWorkshiftId(Long workshiftId);

	/**
	 * Verifica si existe un timesheet para un workshift
	 */
	boolean existsByWorkshift(Workshift workshift);

	/**
	 * Cuenta timesheets por workshift
	 */
	Long countByWorkshift(Workshift workshift);

	/**
	 * Busca todos los timesheets de un usuario
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.workshift.user.id = :userId ORDER BY t.checkInAt DESC")
	List<Timesheet> findByUserId(@Param("userId") Long userId);

	/**
	 * Busca timesheets de un usuario con paginación
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.workshift.user.id = :userId ORDER BY t.checkInAt DESC")
	Page<Timesheet> findByUserId(@Param("userId") Long userId, Pageable pageable);

	/**
	 * Busca timesheets de un usuario en un rango de fechas
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.workshift.user.id = :userId " +
			"AND t.checkInAt BETWEEN :startDate AND :endDate " +
			"ORDER BY t.checkInAt DESC")
	List<Timesheet> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	/**
	 * Cuenta timesheets de un usuario
	 */
	@Query("SELECT COUNT(t) FROM Timesheet t WHERE t.workshift.user.id = :userId")
	Long countByUserId(@Param("userId") Long userId);

	/**
	 * Busca timesheets por estado
	 */
	List<Timesheet> findByStatus(TimesheetStatus status);

	/**
	 * Busca timesheets por estado con paginación
	 */
	Page<Timesheet> findByStatus(TimesheetStatus status, Pageable pageable);

	/**
	 * Busca timesheets abiertos (sin check-out)
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.checkOutAt IS NULL ORDER BY t.checkInAt DESC")
	List<Timesheet> findOpenTimesheets();

	/**
	 * Busca timesheets cerrados (con check-out)
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.checkOutAt IS NOT NULL ORDER BY t.checkOutAt DESC")
	List<Timesheet> findClosedTimesheets();

	/**
	 * Cuenta timesheets por estado
	 */
	Long countByStatus(TimesheetStatus status);

	/**
	 * Busca timesheets abiertos de un usuario específico
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.workshift.user.id = :userId " +
			"AND t.checkOutAt IS NULL ORDER BY t.checkInAt DESC")
	List<Timesheet> findOpenTimesheetsByUser(@Param("userId") Long userId);

	/**
	 * Busca el último timesheet abierto de un usuario (si existe)
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.workshift.user.id = :userId " +
			"AND t.checkOutAt IS NULL ORDER BY t.checkInAt DESC")
	Optional<Timesheet> findLastOpenTimesheetByUser(@Param("userId") Long userId);

	/**
	 * Busca timesheets por fecha (día completo)
	 */
	@Query("SELECT t FROM Timesheet t WHERE DATE(t.checkInAt) = :date ORDER BY t.checkInAt ASC")
	List<Timesheet> findByDate(@Param("date") LocalDate date);

	/**
	 * Busca timesheets en un rango de fechas
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.checkInAt BETWEEN :startDate AND :endDate " +
			"ORDER BY t.checkInAt DESC")
	List<Timesheet> findByDateRange(
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate
	);

	/**
	 * Busca timesheets creados hoy
	 */
	@Query("SELECT t FROM Timesheet t WHERE DATE(t.checkInAt) = CURRENT_DATE ORDER BY t.checkInAt DESC")
	List<Timesheet> findTodayTimesheets();

	/**
	 * Busca timesheets de esta semana
	 */
	@Query("SELECT t FROM Timesheet t WHERE WEEK(t.checkInAt) = WEEK(CURRENT_DATE) " +
			"AND YEAR(t.checkInAt) = YEAR(CURRENT_DATE) ORDER BY t.checkInAt DESC")
	List<Timesheet> findThisWeekTimesheets();

	/**
	 * Busca timesheets de este mes
	 */
	@Query("SELECT t FROM Timesheet t WHERE MONTH(t.checkInAt) = MONTH(CURRENT_DATE) " +
			"AND YEAR(t.checkInAt) = YEAR(CURRENT_DATE) ORDER BY t.checkInAt DESC")
	List<Timesheet> findThisMonthTimesheets();

	/**
	 * Busca timesheets por tipo de turno
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.workshift.shift.shiftType = :shiftType ORDER BY t.checkInAt DESC")
	List<Timesheet> findByShiftType(@Param("shiftType") com.abcm0018.sai.shift.domain.enums.ShiftType shiftType);

	/**
	 * Busca timesheets por tipo de turno en un rango de fechas
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.workshift.shift.shiftType = :shiftType " +
			"AND t.checkInAt BETWEEN :startDate AND :endDate " +
			"ORDER BY t.checkInAt DESC")
	List<Timesheet> findByShiftTypeAndDateRange(
			@Param("shiftType") com.abcm0018.sai.shift.domain.enums.ShiftType shiftType,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate
	);

	/**
	 * Busca timesheets con tardanzas (más de X minutos de retraso)
	 * Nota: Esta query es aproximada, la validación exacta se hace en el servicio
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.status = 'ANOMALY' ORDER BY t.checkInAt DESC")
	List<Timesheet> findAnomalies();

	/**
	 * Busca timesheets con check-out pendiente (más de X horas sin cerrar)
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.checkOutAt IS NULL " +
			"AND t.checkInAt < :thresholdTime " +
			"ORDER BY t.checkInAt ASC")
	List<Timesheet> findOverdueCheckouts(@Param("thresholdTime") LocalDateTime thresholdTime);

	/**
	 * Busca timesheets con horas trabajadas anormales (más de X horas)
	 */
	@Query(value = "SELECT * FROM TIMESHEETS t WHERE t.CHECK_OUT_AT IS NOT NULL " +
			"AND TIMESTAMPDIFF(HOUR, t.CHECK_IN_AT, t.CHECK_OUT_AT) > :maxHours " +
			"ORDER BY t.CHECK_IN_AT DESC", nativeQuery = true)
	List<Timesheet> findExcessiveWorkHours(@Param("maxHours") long maxHours);

	/**
	 * Cuenta timesheets por día en un rango de fechas
	 */
	@Query("SELECT DATE(t.checkInAt), COUNT(t) FROM Timesheet t " +
			"WHERE t.checkInAt BETWEEN :startDate AND :endDate " +
			"GROUP BY DATE(t.checkInAt) " +
			"ORDER BY DATE(t.checkInAt)")
	List<Object[]> countTimesheetsByDay(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	/**
	 * Cuenta timesheets por usuario en un rango de fechas
	 */
	@Query("SELECT t.workshift.user, COUNT(t) FROM Timesheet t " +
			"WHERE t.checkInAt BETWEEN :startDate AND :endDate " +
			"GROUP BY t.workshift.user.id " +
			"ORDER BY COUNT(t) DESC")
	List<Object[]> countTimesheetsByUser(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	/**
	 * Cuenta timesheets por tipo de turno en un rango de fechas
	 */
	@Query("SELECT t.workshift.shift.shiftType, COUNT(t) FROM Timesheet t " +
			"WHERE t.checkInAt BETWEEN :startDate AND :endDate " +
			"GROUP BY t.workshift.shift.shiftType " +
			"ORDER BY COUNT(t) DESC")
	List<Object[]> countTimesheetsByShiftType(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	/**
	 * Promedio de horas trabajadas por día
	 */
	@Query(value = "SELECT AVG(TIMESTAMPDIFF(HOUR, t.CHECK_IN_AT, t.CHECK_OUT_AT)) " +
			"FROM TIMESHEETS t " +
			"WHERE t.CHECK_OUT_AT IS NOT NULL " +
			"AND t.CHECK_IN_AT BETWEEN :startDate AND :endDate",
			nativeQuery = true)
	Double getAverageWorkedHours(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	/**
	 * Total de horas trabajadas por usuario en un período
	 */
	@Query(value = "SELECT COALESCE(SUM(TIMESTAMPDIFF(HOUR, t.CHECK_IN_AT, t.CHECK_OUT_AT)), 0) " +
			"FROM TIMESHEETS t " +
			"INNER JOIN WORKSHIFTS w ON t.WORKSHIFT_ID = w.ID " +
			"WHERE w.USER_ID = :userId " +
			"AND t.CHECK_OUT_AT IS NOT NULL " +
			"AND t.CHECK_IN_AT BETWEEN :startDate AND :endDate",
			nativeQuery = true)
	Long getTotalWorkedHoursByUser(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	/**
	 * Usuarios más puntuales (menor promedio de tardanza)
	 */
	@Query(value = "SELECT u.* FROM USERS u " +
			"INNER JOIN WORKSHIFTS w ON w.USER_ID = u.ID " +
			"INNER JOIN TIMESHEETS t ON t.WORKSHIFT_ID = w.ID " +
			"INNER JOIN SHIFTS s ON w.SHIFT_ID = s.ID " +
			"WHERE t.CHECK_IN_AT BETWEEN :startDate AND :endDate " +
			"GROUP BY u.ID " +
			"ORDER BY AVG(TIMESTAMPDIFF(MINUTE, " +
			"  TIMESTAMP(w.DATE, s.START_TIME), " +
			"  t.CHECK_IN_AT)) ASC",
			nativeQuery = true)
	List<User> findMostPunctualUsers(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

	/**
	 * Usuarios con más horas extras
	 */
	@Query(value = "SELECT u.*, SUM(TIMESTAMPDIFF(HOUR, t.CHECK_IN_AT, t.CHECK_OUT_AT)) as total_hours " +
			"FROM USERS u " +
			"INNER JOIN WORKSHIFTS w ON w.USER_ID = u.ID " +
			"INNER JOIN TIMESHEETS t ON t.WORKSHIFT_ID = w.ID " +
			"WHERE t.CHECK_OUT_AT IS NOT NULL " +
			"AND t.CHECK_IN_AT BETWEEN :startDate AND :endDate " +
			"GROUP BY u.ID " +
			"ORDER BY total_hours DESC",
			nativeQuery = true)
	List<Object[]> findUsersWithMostHours(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

	/**
	 * Búsqueda con múltiples filtros
	 */
	@Query("SELECT t FROM Timesheet t WHERE " +
			"(:userId IS NULL OR t.workshift.user.id = :userId) AND " +
			"(:workshiftId IS NULL OR t.workshift.id = :workshiftId) AND " +
			"(:status IS NULL OR t.status = :status) AND " +
			"(:startDate IS NULL OR t.checkInAt >= :startDate) AND " +
			"(:endDate IS NULL OR t.checkInAt <= :endDate) " +
			"ORDER BY t.checkInAt DESC")
	Page<Timesheet> findWithFilters(@Param("userId") Long userId, @Param("workshiftId") Long workshiftId, @Param("status") TimesheetStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

	/**
	 * Búsqueda de timesheets de un usuario en una fecha específica
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.workshift.user.id = :userId AND DATE(t.checkInAt) = :date")
	Optional<Timesheet> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

	/**
	 * Busca timesheets sin notas
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.notes IS NULL OR t.notes = '' ORDER BY t.checkInAt DESC")
	List<Timesheet> findTimesheetsWithoutNotes();

	/**
	 * Busca timesheets con notas
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.notes IS NOT NULL AND t.notes != '' " +
			"ORDER BY t.checkInAt DESC")
	List<Timesheet> findTimesheetsWithNotes();

	/**
	 * Verifica si un usuario ya tiene un timesheet abierto
	 */
	@Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
			"FROM Timesheet t " +
			"WHERE t.workshift.user.id = :userId " +
			"AND t.checkOutAt IS NULL")
	boolean hasOpenTimesheet(@Param("userId") Long userId);

	/**
	 * Busca timesheets duplicados para el mismo workshift
	 */
	@Query("SELECT t.workshift.id, COUNT(t) FROM Timesheet t " +
			"GROUP BY t.workshift.id " +
			"HAVING COUNT(t) > 1")
	List<Object[]> findDuplicateTimesheets();

	/**
	 * Busca timesheets modificados recientemente
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.updatedAt IS NOT NULL ORDER BY t.updatedAt DESC")
	List<Timesheet> findRecentlyModified(Pageable pageable);

	/**
	 * Busca timesheets antiguos para archivar
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.checkInAt < :cutoffDate")
	List<Timesheet> findOldTimesheetsForArchiving(@Param("cutoffDate") LocalDateTime cutoffDate);

	/**
	 * Busca el timesheet de un usuario en un workshift específico
	 * Spring Data JPA genera automáticamente la query
	 *
	 * @param userId ID del usuario
	 * @param workshiftId ID del workshift
	 * @return Timesheet si existe
	 */
	@Query("SELECT t FROM Timesheet t WHERE t.workshift.user.id = :userId AND t.workshift.id = :workshiftId")
	Optional<Timesheet> findByUserIdAndWorkshiftId(Long userId, Long workshiftId);
}
