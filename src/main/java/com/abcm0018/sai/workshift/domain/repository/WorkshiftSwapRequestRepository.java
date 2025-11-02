package com.abcm0018.sai.workshift.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.workshift.domain.entity.Workshift;
import com.abcm0018.sai.workshift.domain.entity.WorkshiftSwapRequest;
import com.abcm0018.sai.workshift.domain.enums.SwapRequestStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkshiftSwapRequestRepository extends JpaRepository<WorkshiftSwapRequest, Long> {

	// ========== BÚSQUEDAS POR ESTADO ==========

	/**
	 * Encuentra todas las solicitudes por estado
	 */
	List<WorkshiftSwapRequest> findByStatus(SwapRequestStatus status);

	/**
	 * Encuentra solicitudes pendientes ordenadas por fecha de creación
	 */
	List<WorkshiftSwapRequest> findByStatusOrderByCreatedAtAsc(SwapRequestStatus status);

	/**
	 * Cuenta solicitudes por estado
	 */
	Long countByStatus(SwapRequestStatus status);

	/**
	 * Verifica si existen solicitudes pendientes
	 */
	boolean existsByStatus(SwapRequestStatus status);

	// ========== BÚSQUEDAS POR USUARIO ==========

	/**
	 * Encuentra todas las solicitudes de un usuario
	 */
	List<WorkshiftSwapRequest> findByUserOrderByCreatedAtDesc(User user);

	/**
	 * Encuentra solicitudes de un usuario por estado
	 */
	List<WorkshiftSwapRequest> findByUserAndStatus(User user, SwapRequestStatus status);

	/**
	 * Encuentra solicitudes de un usuario con paginación
	 */
	Page<WorkshiftSwapRequest> findByUser(User user, Pageable pageable);

	/**
	 * Cuenta solicitudes de un usuario
	 */
	Long countByUser(User user);

	/**
	 * Cuenta solicitudes pendientes de un usuario
	 */
	Long countByUserAndStatus(User user, SwapRequestStatus status);

	// ========== BÚSQUEDAS POR REVISOR ==========

	/**
	 * Encuentra solicitudes revisadas por un supervisor/administrador
	 */
	List<WorkshiftSwapRequest> findByReviewedByOrderByReviewedAtDesc(User reviewer);

	/**
	 * Cuenta solicitudes revisadas por un supervisor/administrador
	 */
	Long countByReviewedBy(User reviewer);

	// ========== BÚSQUEDAS POR WORKSHIFT ==========

	/**
	 * Encuentra todas las solicitudes de un turno actual
	 */
	List<WorkshiftSwapRequest> findByCurrentWorkshift(Workshift workshift);

	/**
	 * Encuentra todas las solicitudes donde el turno solicitado es específico
	 */
	List<WorkshiftSwapRequest> findByRequestedWorkshift(Workshift workshift);

	/**
	 * Encuentra solicitudes relacionadas con un turno (actual o solicitado)
	 */
	@Query("SELECT sr FROM WorkshiftSwapRequest sr " +
			"WHERE sr.currentWorkshift = :workshift OR sr.requestedWorkshift = :workshift")
	List<WorkshiftSwapRequest> findByWorkshift(@Param("workshift")Workshift workshift);

	/**
	 * Encuentra solicitudes pendientes para un turno específico
	 */
	@Query("SELECT sr FROM WorkshiftSwapRequest sr " +
			"WHERE (sr.currentWorkshift = :workshift OR sr.requestedWorkshift = :workshift) AND sr.status = :status")
	List<WorkshiftSwapRequest> findByWorkshiftAndStatus(@Param("workshift") Workshift workshift, @Param("status") SwapRequestStatus status);

	// ========== BÚSQUEDAS POR FECHA ==========

	/**
	 * Encuentra solicitudes creadas después de una fecha
	 */
	List<WorkshiftSwapRequest> findByCreatedAtAfter(LocalDateTime date);

	/**
	 * Encuentra solicitudes creadas en un rango de fechas
	 */
	List<WorkshiftSwapRequest> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

	/**
	 * Encuentra solicitudes pendientes antiguas (vencidas)
	 */
	@Query("SELECT sr FROM WorkshiftSwapRequest sr " +
			"WHERE sr.status = 'PENDING' " +
			"AND sr.createdAt < :thresholdDate " +
			"ORDER BY sr.createdAt ASC")
	List<WorkshiftSwapRequest> findOverdueRequests(@Param("thresholdDate") LocalDateTime thresholdDate);

	// ========== BÚSQUEDAS COMPLEJAS ==========

	/**
	 * Encuentra solicitudes pendientes de un usuario para un turno específico
	 */
	@Query("SELECT sr FROM WorkshiftSwapRequest sr " +
			"WHERE sr.user = :user " +
			"AND sr.currentWorkshift = :currentWorkshift " +
			"AND sr.status = 'PENDING'")
	Optional<WorkshiftSwapRequest> findPendingRequestByUserAndCurrentWorkshift(@Param("user") User user, @Param("currentWorkshift") Workshift currentWorkshift);

	/**
	 * Verifica si existe una solicitud pendiente duplicada
	 */
	@Query("SELECT CASE WHEN COUNT(sr) > 0 THEN true ELSE false END " +
			"FROM WorkshiftSwapRequest sr " +
			"WHERE sr.user = :user " +
			"AND sr.currentWorkshift = :currentWorkshift " +
			"AND sr.requestedWorkshift = :requestedWorkshift " +
			"AND sr.status = 'PENDING'")
	boolean existsPendingDuplicateRequest(@Param("user") User user, @Param("currentWorkshift") Workshift currentWorkshift, @Param("requestedWorkshift") Workshift requestedWorkshift);

	/**
	 * Encuentra solicitudes con filtros múltiples
	 */
	@Query("SELECT sr FROM WorkshiftSwapRequest sr " +
			"WHERE (:status IS NULL OR sr.status = :status) " +
			"AND (:userId IS NULL OR sr.user.id = :userId) " +
			"AND (:reviewerId IS NULL OR sr.reviewedBy.id = :reviewerId) " +
			"ORDER BY sr.createdAt DESC")
	Page<WorkshiftSwapRequest> findWithFilters(@Param("status") SwapRequestStatus status, @Param("userId") Long userId, @Param("reviewerId") Long reviewerId, Pageable pageable);

	/**
	 * Obtiene estadísticas de solicitudes por usuario
	 */
	@Query("SELECT sr.status, COUNT(sr) FROM WorkshiftSwapRequest sr " +
			"WHERE sr.user = :user GROUP BY sr.status")
	List<Object[]> getRequestStatisticsByUser(@Param("user") User user);

	/**
	 * Obtiene las últimas N solicitudes pendientes
	 */
	@Query("SELECT sr FROM WorkshiftSwapRequest sr " +
			"WHERE sr.status = 'PENDING' ORDER BY sr.createdAt DESC")
	Page<WorkshiftSwapRequest> findRecentPendingRequests(Pageable pageable);

	/**
	 * Encuentra solicitudes con notas de revisión
	 */
	@Query("SELECT sr FROM WorkshiftSwapRequest sr " +
			"WHERE sr.reviewNotes IS NOT NULL AND sr.reviewNotes != ''")
	List<WorkshiftSwapRequest> findRequestsWithReviewNotes();

	/**
	 * Encuentra solicitudes aprobadas en un rango de fechas
	 */
	@Query("SELECT sr FROM WorkshiftSwapRequest sr " +
			"WHERE sr.status = 'APPROVED' AND sr.reviewedAt BETWEEN :startDate AND :endDate")
	List<WorkshiftSwapRequest> findApprovedRequestsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	// ========== OPERACIONES DE LIMPIEZA ==========

	/**
	 * Elimina solicitudes canceladas antiguas
	 */
	@Modifying
	@Query("DELETE FROM WorkshiftSwapRequest sr " +
			"WHERE sr.status = 'CANCELLED' AND sr.updatedAt < :thresholdDate")
	void deleteCancelledRequestsOlderThan(@Param("thresholdDate") LocalDateTime thresholdDate);
}
