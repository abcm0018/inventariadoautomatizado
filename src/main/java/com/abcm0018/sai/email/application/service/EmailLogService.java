package com.abcm0018.sai.email.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.abcm0018.sai.email.application.dtos.EmailLogFilterDTO;
import com.abcm0018.sai.email.application.dtos.SendEmailResponseDTO;
import com.abcm0018.sai.email.domain.enums.EmailStatus;

public interface EmailLogService {
	/**
	 * Registra un email en el log
	 */
	void logEmailSent(String recipient, String subject, String emailType, String body, EmailStatus status);

	/**
	 * Registra un error de envío
	 */
	void logEmailError(String recipient, String emailType, String errorMessage);

	/**
	 * Obtiene histórico de emails por destinatario
	 */
	List<SendEmailResponseDTO> getEmailsByRecipient(String recipient);

	/**
	 * Obtiene emails por estado
	 */
	Page<SendEmailResponseDTO> getEmailsByStatus(EmailStatus status, Pageable pageable);

	/**
	 * Obtiene emails en rango de fechas
	 */
	List<SendEmailResponseDTO> getEmailsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

	/**
	 * Cuenta emails enviados a un destinatario en un periodo
	 */
	Long countEmailsByRecipientAndDateRange(String recipient, LocalDateTime startDate, LocalDateTime endDate);

	/**
	 * Verifica si existe un email reciente para evitar spam
	 */
	boolean hasRecentEmail(String recipient, String emailType, Integer minutesThreshold);

	Page<SendEmailResponseDTO> searchEmailsWithFilters(EmailLogFilterDTO filterDTO, Pageable pageable);
}
