package com.abcm0018.sai.shift.domain.repository;

import com.abcm0018.sai.shift.domain.entity.Shift;
import com.abcm0018.sai.shift.domain.enums.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

	/**
	 * Busca un turno por su tipo (MORNING, AFTERNOON, NIGHT)
	 * Debe ser único según la constraint
	 */
	Optional<Shift> findByShiftType(ShiftType shiftType);

	/**
	 * Verifica si existe un turno con ese tipo
	 */
	boolean existsByShiftType(ShiftType shiftType);

	/**
	 * Busca un turno activo por tipo
	 */
	@Query("SELECT s FROM Shift s WHERE s.shiftType = :shiftType AND s.active = true")
	Optional<Shift> findActiveByShiftType(@Param("shiftType") ShiftType shiftType);

	/**
	 * Obtiene todos los turnos activos
	 * IMPORTANTE: Usado por WorkshiftService para la planificación
	 */
	List<Shift> findByActiveTrue();

	/**
	 * Obtiene turnos activos ordenados por hora de inicio
	 */
	@Query("SELECT s FROM Shift s WHERE s.active = true ORDER BY s.startTime ASC")
	List<Shift> findAllActiveOrderedByStartTime();

	/**
	 * Obtiene todos los turnos inactivos
	 */
	List<Shift> findByActiveFalse();

	/**
	 * Cuenta turnos activos
	 */
	Long countByActiveTrue();

	/**
	 * Cuenta turnos inactivos
	 */
	Long countByActiveFalse();

	/**
	 * Busca turnos que empiezan a una hora específica
	 */
	List<Shift> findByStartTime(LocalTime startTime);

	/**
	 * Busca turnos que terminan a una hora específica
	 */
	List<Shift> findByEndTime(LocalTime endTime);

	/**
	 * Busca turnos que empiezan en un rango de horas
	 */
	@Query("SELECT s FROM Shift s WHERE s.startTime BETWEEN :startRange AND :endRange")
	List<Shift> findByStartTimeBetween(@Param("startRange") LocalTime startRange, @Param("endRange") LocalTime endRange);

	/**
	 * Busca turnos que terminan en un rango de horas
	 */
	@Query("SELECT s FROM Shift s WHERE s.endTime BETWEEN :startRange AND :endRange")
	List<Shift> findByEndTimeBetween(@Param("startRange") LocalTime startRange, @Param("endRange") LocalTime endRange);

	/**
	 * Busca turnos con duración específica (en horas)
	 * Nota: Requiere cálculo en la aplicación, no en BD
	 */
	@Query("SELECT s FROM Shift s")
	List<Shift> findAllForDurationFilter();

	/**
	 * Busca turnos que cruzan la medianoche
	 * (endTime < startTime)
	 */
	@Query("SELECT s FROM Shift s WHERE s.endTime < s.startTime")
	List<Shift> findShiftsCrossingMidnight();

	/**
	 * Busca turnos que NO cruzan la medianoche
	 */
	@Query("SELECT s FROM Shift s WHERE s.endTime >= s.startTime")
	List<Shift> findShiftsNotCrossingMidnight();

	/**
	 * Busca turnos activos por tipo
	 */
	List<Shift> findByShiftTypeAndActiveTrue(ShiftType shiftType);

	/**
	 * Busca turnos activos que empiezan después de cierta hora
	 */
	@Query("SELECT s FROM Shift s WHERE s.active = true AND s.startTime >= :time ORDER BY s.startTime")
	List<Shift> findActiveStartingAfter(@Param("time") LocalTime time);

	/**
	 * Busca turnos activos que terminan antes de cierta hora
	 */
	@Query("SELECT s FROM Shift s WHERE s.active = true AND s.endTime <= :time ORDER BY s.endTime")
	List<Shift> findActiveEndingBefore(@Param("time") LocalTime time);

	/**
	 * Verifica si existe un turno activo con horario solapado
	 * Útil para evitar conflictos al crear/editar turnos
	 */
	@Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Shift s " +
			"WHERE s.active = true AND s.id != :excludeId " +
			"AND ((s.startTime <= :startTime AND s.endTime > :startTime) " +
			"OR (s.startTime < :endTime AND s.endTime >= :endTime) " +
			"OR (s.startTime >= :startTime AND s.endTime <= :endTime))")
	boolean existsOverlappingShift(
			@Param("startTime") LocalTime startTime,
			@Param("endTime") LocalTime endTime,
			@Param("excludeId") Long excludeId
	);

	/**
	 * Verifica si un shift tiene workshifts asignados
	 */
	@Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END " +
			"FROM Workshift w WHERE w.shift.id = :shiftId")
	boolean hasAssignedWorkshifts(@Param("shiftId") Long shiftId);

	/**
	 * Cuenta workshifts asignados a un shift
	 */
	@Query("SELECT COUNT(w) FROM Workshift w WHERE w.shift.id = :shiftId")
	Long countAssignedWorkshifts(@Param("shiftId") Long shiftId);

	/**
	 * Obtiene todos los turnos ordenados por tipo
	 */
	List<Shift> findAllByOrderByShiftTypeAsc();

	/**
	 * Obtiene todos los turnos ordenados por hora de inicio
	 */
	List<Shift> findAllByOrderByStartTimeAsc();

	/**
	 * Cuenta turnos por tipo
	 */
	@Query("SELECT s.shiftType, COUNT(s) FROM Shift s GROUP BY s.shiftType")
	List<Object[]> countByShiftType();

	/**
	 * Obtiene la duración promedio de todos los turnos
	 * Nota: Requiere cálculo en la aplicación
	 */
	@Query("SELECT s FROM Shift s WHERE s.active = true")
	List<Shift> findAllActiveForStatistics();

	/**
	 * Busca el turno que contiene una hora específica
	 * Útil para determinar qué turno está activo en un momento dado
	 */
	@Query("SELECT s FROM Shift s WHERE s.active = true " +
			"AND ((s.startTime <= s.endTime AND :time >= s.startTime AND :time < s.endTime) " +
			"OR (s.startTime > s.endTime AND (:time >= s.startTime OR :time < s.endTime)))")
	Optional<Shift> findShiftContainingTime(@Param("time") LocalTime time);

	/**
	 * Busca turnos con descripción (búsqueda parcial)
	 */
	@Query("SELECT s FROM Shift s WHERE LOWER(s.description) LIKE LOWER(CONCAT('%', :description, '%'))")
	List<Shift> findByDescriptionContaining(@Param("description") String description);

	/**
	 * Obtiene turnos modificados
	 */
	@Query("SELECT s FROM Shift s WHERE s.updatedAt IS NOT NULL")
	List<Shift> findModifiedShifts();

	/**
	 * Cuenta turnos modificados
	 */
	@Query("SELECT COUNT(s) FROM Shift s WHERE s.updatedAt IS NOT NULL")
	Long countModifiedShifts();

	@Query("SELECT COUNT(w) FROM Workshift w WHERE w.shift.id = :shiftId AND w.date >= :today")
	Long countFutureAssignedWorkshifts(@Param("shiftId") Long shiftId, @Param("today") LocalDate today);

	/**
	 * Devuelve un conjunto de todos los ShiftType que ya existen en la BD.
	 * Optimizado para el Seeder.
	 */
	@Query("SELECT s.shiftType FROM Shift s")
	Set<ShiftType> findAllShiftTypes();
}
