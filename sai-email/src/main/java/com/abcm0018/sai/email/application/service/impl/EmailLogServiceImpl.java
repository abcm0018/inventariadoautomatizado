package com.abcm0018.sai.email.application.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.abcm0018.sai.email.application.dtos.EmailLogFilterDTO;
import com.abcm0018.sai.email.application.dtos.SendEmailResponseDTO;
import com.abcm0018.sai.email.application.mapper.EmailMapper;
import com.abcm0018.sai.email.application.service.EmailLogService;
import com.abcm0018.sai.email.domain.entity.EmailLog;
import com.abcm0018.sai.email.domain.enums.EmailStatus;
import com.abcm0018.sai.email.domain.repository.EmailLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailLogServiceImpl implements EmailLogService {

	private final EmailLogRepository emailLogRepository;
	private final EmailMapper emailMapper;

	@Override
	public void logEmailSent(String recipient, String subject, String emailType, String body, EmailStatus status) {

		log.debug("Registrando email enviado: {} a {}", emailType, recipient);

		EmailLog emailLog = EmailLog.builder()
				.recipient(recipient)
				.subject(subject)
				.emailType(emailType)
				.body(body)
				.status(status)
				.sentAt(LocalDateTime.now())
				.retryCount(0)
				.build();

		emailLogRepository.save(emailLog);
		log.info("Email registrado en log: {} a {}", emailType, recipient);
	}

	@Override
	public void logEmailError(String recipient, String emailType, String errorMessage) {
		log.warn("Registrando error de email: {} para {}: {}", emailType, recipient, errorMessage);

		EmailLog emailLog = EmailLog.builder()
				.recipient(recipient)
				.emailType(emailType)
				.status(EmailStatus.FAILED)
				.errorMessage(errorMessage)
				.retryCount(0)
				.build();

		emailLogRepository.save(emailLog);
		log.error("Error registrado en log para: {}", recipient);
	}

	@Override
	public List<SendEmailResponseDTO> getEmailsByRecipient(String recipient) {
		log.debug("Obteniendo histórico de emails para: {}", recipient);
		List<EmailLog> emailLogs = emailLogRepository.findByRecipient(recipient);
		return emailMapper.emailLogToResponseDTOList(emailLogs);
	}

	@Override
	public Page<SendEmailResponseDTO> getEmailsByStatus(EmailStatus status, Pageable pageable) {
		log.debug("Obteniendo emails con estado: {}", status);
		Page<EmailLog> emailLogs = emailLogRepository.findByStatus(status, pageable);
		return emailLogs.map(emailMapper::emailLogToResponseDTO);
	}

	@Override
	public List<SendEmailResponseDTO> getEmailsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
		log.debug("Obteniendo emails entre {} y {}", startDate, endDate);
		List<EmailLog> emailLogs = emailLogRepository.findByDateRange(startDate, endDate);
		return emailMapper.emailLogToResponseDTOList(emailLogs);
	}

	@Override
	public Long countEmailsByRecipientAndDateRange(String recipient, LocalDateTime startDate, LocalDateTime endDate) {
		log.debug("Contando emails para {} entre {} y {}", recipient, startDate, endDate);
		return emailLogRepository.countByRecipientAndDateRange(recipient, startDate, endDate);
	}

	@Override
	public boolean hasRecentEmail(String recipient, String emailType, Integer minutesThreshold) {
		log.debug("Verificando email reciente para {} de tipo {} (últimos {} minutos)", recipient, emailType, minutesThreshold);

		LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutesThreshold);
		boolean exists = emailLogRepository.existsRecentEmail(recipient, emailType, threshold);

		if (exists) {
			log.debug("Email reciente encontrado para {} - Posible spam", recipient);
		}

		return exists;
	}

	@Override
	public Page<SendEmailResponseDTO> searchEmailsWithFilters(EmailLogFilterDTO filterDTO, Pageable pageable) {
		log.debug("Búsqueda avanzada de emails con filtros: {}", filterDTO);

		Page<EmailLog> emailLogs = emailLogRepository.findWithFilters(
				filterDTO.getRecipient(),
				filterDTO.getEmailType(),
				filterDTO.getStatus(),
				filterDTO.getStartDate(),
				filterDTO.getEndDate(),
				pageable
		);

		return emailLogs.map(emailMapper::emailLogToResponseDTO);
	}
}
