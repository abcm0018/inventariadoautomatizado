package com.abcm0018.sai.workshift.domain.repository;

import com.abcm0018.sai.shift.domain.entity.Shift;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkshiftRepository extends JpaRepository<Workshift, Long>, JpaSpecificationExecutor<Workshift> {

	/**
	 * Encuentra turnos de un usuario en una fecha específica
	 */
	List<Workshift> findByUserAndDate(User user, LocalDate date);

	/**
	 * Encuentra todos los turnos de una fecha específica
	 */
	List<Workshift> findByDate(LocalDate date);

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
	 * Encuentra el turno actual de un usuario (hoy)
	 */
	@Query("SELECT w FROM Workshift w WHERE w.user = :user AND w.date = :today")
	Optional<Workshift> findTodayWorkshiftByUser(@Param("user") User user, @Param("today") LocalDate today);

	/**
	 * Encuentra turnos que se solapan (mismo usuario, misma fecha, diferentes shifts)
	 * Útil para detectar conflictos de programación
	 */
	@Query("SELECT w FROM Workshift w WHERE w.user = :user AND w.date = :date AND w.shift = :shift")
	List<Workshift> findPotentialConflicts(@Param("user") User user, @Param("date") LocalDate date, @Param("shift") Shift shift);

	/**
	 * Obtiene la última asignación de turno de un usuario
	 */
	@Query("SELECT w FROM Workshift w WHERE w.user = :user ORDER BY w.date DESC, w.createdAt DESC")
	List<Workshift> findLastWorkshiftByUser(@Param("user") User user, Pageable pageable);

	/**
	 * Obtiene el próximo turno de un usuario
	 */
	@Query("SELECT w FROM Workshift w WHERE w.user = :user AND w.date >= :today ORDER BY w.date ASC, w.shift.startTime ASC")
	List<Workshift> findNextWorkshiftByUser(@Param("user") User user, @Param("today") LocalDate today, Pageable pageable);

	/**
	 * Elimina turnos antiguos (para limpieza de datos)
	 */
	@Modifying
	@Query("DELETE FROM Workshift w WHERE w.date < :cutoffDate")
	void deleteWorkshiftsOlderThan(@Param("cutoffDate") LocalDate cutoffDate);

	@Query("SELECT w FROM Workshift w " +
			"WHERE w.date >= :startDate " +
			"AND w.date <= :endDate " +
			"AND w.user IN :users")
	List<Workshift> findByDateBetweenAndUserIn(LocalDate startDate, LocalDate endDate, List<User> users);

	@Query("SELECT w FROM Workshift w " +
			"WHERE w.user IN :users " +
			"AND w.date = (" +
			"    SELECT MAX(w2.date) " +
			"    FROM Workshift w2 " +
			"    WHERE w2.user = w.user " +
			")")
	List<Workshift> findLatestShiftTypesForUsers(List<User> users);
}
