package com.abcm0018.sai.email.application.service;

import com.abcm0018.sai.email.application.dtos.SendEmailRequestDTO;
import com.abcm0018.sai.email.application.dtos.SendEmailResponseDTO;

public interface EmailService {
	/**
	 * Envía un email a partir de un request
	 */
	SendEmailResponseDTO sendEmail(SendEmailRequestDTO request);

	/**
	 * Envía un email directamente (para casos especiales)
	 */
	SendEmailResponseDTO sendDirectEmail(String recipient, String subject, String bodyHtml);

	/**
	 * Reintenta enviar emails fallidos
	 */
	void retryFailedEmails();
}
