package com.abcm0018.sai.email.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.abcm0018.sai.email.domain.entity.EmailLog;
import com.abcm0018.sai.email.domain.enums.EmailStatus;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

	/**
	 * Buscar todos los emails enviados a un destinatario
	 */
	List<EmailLog> findByRecipient(String recipient);

	/**
	 * Buscar emails por tipo (NEW_EMPLOYEE_CREDENTIALS, PASSWORD_RESET, etc.)
	 */
	Page<EmailLog> findByEmailType(String emailType, Pageable pageable);

	/**
	 * Buscar emails por estado (SENT, FAILED, PENDING)
	 */
	Page<EmailLog> findByStatus(EmailStatus status, Pageable pageable);

	/**
	 * Buscar emails fallidos para reintentos
	 */
	List<EmailLog> findByStatusAndRetryCountLessThan(EmailStatus status, Integer maxRetries);

	/**
	 * Buscar emails en un rango de fechas
	 */
	@Query("SELECT e FROM EmailLog e WHERE e.sentAt BETWEEN :startDate AND :endDate ORDER BY e.sentAt DESC")
	List<EmailLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	/**
	 * Contar emails enviados a un destinatario en un periodo
	 */
	@Query("SELECT COUNT(e) FROM EmailLog e WHERE e.recipient = :recipient AND e.sentAt BETWEEN :startDate AND :endDate")
	Long countByRecipientAndDateRange(@Param("recipient") String recipient,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	/**
	 * Obtener últimos N emails por tipo
	 */
	@Query("SELECT e FROM EmailLog e WHERE e.emailType = :emailType ORDER BY e.sentAt DESC")
	Page<EmailLog> findLatestByEmailType(@Param("emailType") String emailType, Pageable pageable);

	/**
	 * Verificar si existe un email reciente para un destinatario
	 */
	@Query("SELECT COUNT(e) > 0 FROM EmailLog e WHERE e.recipient = :recipient AND e.emailType = :emailType AND e.sentAt > :threshold")
	boolean existsRecentEmail(@Param("recipient") String recipient,
			@Param("emailType") String emailType,
			@Param("threshold") LocalDateTime threshold);

	/**
	 * Búsqueda avanzada con múltiples criterios
	 * Permite filtrar por: recipient, emailType, status y rango de fechas
	 */
	@Query("""
        SELECT e FROM EmailLog e
        WHERE (:recipient IS NULL OR e.recipient LIKE %:recipient%)
          AND (:emailType IS NULL OR e.emailType = :emailType)
          AND (:status IS NULL OR e.status = :status)
          AND (:startDate IS NULL OR e.sentAt >= :startDate)
          AND (:endDate IS NULL OR e.sentAt <= :endDate)
        ORDER BY e.sentAt DESC
        """)
	Page<EmailLog> findWithFilters(
			@Param("recipient") String recipient,
			@Param("emailType") String emailType,
			@Param("status") EmailStatus status,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate,
			Pageable pageable
	);
}
