package com.abcm0018.sai.email.application.service.impl;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.abcm0018.sai.email.application.dtos.SendEmailRequestDTO;
import com.abcm0018.sai.email.application.dtos.SendEmailResponseDTO;
import com.abcm0018.sai.email.application.service.EmailLogService;
import com.abcm0018.sai.email.application.service.EmailParametersValidator;
import com.abcm0018.sai.email.application.service.EmailService;
import com.abcm0018.sai.email.application.service.EmailTemplateService;
import com.abcm0018.sai.email.domain.enums.EmailStatus;
import com.abcm0018.sai.email.domain.repository.EmailLogRepository;
import com.abcm0018.sai.email.exception.EmailServiceException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

	private final JavaMailSender mailSender;
	private final EmailTemplateService emailTemplateService;
	private final EmailLogService emailLogService;
	private final EmailParametersValidator parametersValidator;
	private final EmailLogRepository emailLogRepository;

	@Value("${app.email.from-address}")
	private String fromAddress;

	@Value("${app.email.from-name}")
	private String fromName;

	@Override
	public SendEmailResponseDTO sendEmail(SendEmailRequestDTO request) {
		log.info("Iniciando envío de email tipo: {} a: {}", request.getEmailType(), request.getRecipient());

		try {
			// Validar parámetros según el tipo de email
			parametersValidator.validateParameters(request.getEmailType(), request.getTemplateParameters());

			// Construir cuerpo del email
			String body = emailTemplateService.buildBody(request.getEmailType().getTemplateName(), request.getTemplateParameters());

			// Determinar asunto
			String subject = request.getCustomSubject() != null
					? request.getCustomSubject()
					: emailTemplateService.getSubject(request.getEmailType().getTemplateName());

			// Enviar email
			SendEmailResponseDTO response = sendDirectEmail(request.getRecipient(), subject, body);

			// Registrar en log
			emailLogService.logEmailSent(request.getRecipient(), subject, request.getEmailType().name(), body, EmailStatus.SENT);

			log.info("Email enviado exitosamente a: {}", request.getRecipient());

			return response;

		} catch (EmailServiceException e) {
			log.error("Error en validación o procesamiento: {}", e.getMessage());
			emailLogService.logEmailError(request.getRecipient(), request.getEmailType().name(), e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("Error inesperado al enviar email: {}", e.getMessage(), e);
			emailLogService.logEmailError(request.getRecipient(), request.getEmailType().name(), e.getMessage());
			throw EmailServiceException.sendingError(request.getRecipient(), e);
		}
	}

	@Override
	public SendEmailResponseDTO sendDirectEmail(String recipient, String subject, String bodyHtml) {

		log.debug("Preparando envío directo de email a: {}", recipient);

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(fromAddress, fromName);
			helper.setTo(recipient);
			helper.setSubject(subject);
			helper.setText(bodyHtml, true);

			mailSender.send(message);

			log.info("Email enviado exitosamente a: {} con asunto: {}", recipient, subject);

			return SendEmailResponseDTO.builder()
					.recipient(recipient)
					.subject(subject)
					.status(EmailStatus.SENT)
					.sentAt(LocalDateTime.now())
					.build();

		} catch (MessagingException | UnsupportedEncodingException e) {
			log.error("Error al enviar email a {}: {}", recipient, e.getMessage(), e);
			throw EmailServiceException.sendingError(recipient, e);
		}
	}

	@Override
	public void retryFailedEmails() {
		log.info("Iniciando reintento de emails fallidos");

		int maxRetries = 3;
		var failedEmails = emailLogRepository.findByStatusAndRetryCountLessThan(
				EmailStatus.FAILED,
				maxRetries
		);

		log.info("Se encontraron {} emails para reintentar", failedEmails.size());

		failedEmails.forEach(emailLog -> {
			try {
				sendDirectEmail(emailLog.getRecipient(), emailLog.getSubject(), emailLog.getBody());
				emailLog.setStatus(EmailStatus.SENT);
				emailLog.setSentAt(LocalDateTime.now());
				log.info("Reintento exitoso para: {}", emailLog.getRecipient());
			} catch (EmailServiceException e) {
				emailLog.setRetryCount(emailLog.getRetryCount() + 1);
				log.warn("Reintento fallido para: {}. Intento: {}", emailLog.getRecipient(), emailLog.getRetryCount());
			}
			emailLogRepository.save(emailLog);
		});

		log.info("Reintento de emails completado");
	}
}

