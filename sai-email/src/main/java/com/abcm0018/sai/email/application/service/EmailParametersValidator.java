package com.abcm0018.sai.email.application.service;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.abcm0018.sai.email.domain.enums.EmailType;
import com.abcm0018.sai.email.exception.EmailServiceException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailParametersValidator {
	/**
	 * Valida que todos los parámetros requeridos estén presentes
	 */
	public void validateParameters(EmailType emailType, Map<String, String> parameters) {
		String[] required = emailType.getRequiredParameters();

		for (String param : required) {
			if (!parameters.containsKey(param) || parameters.get(param) == null || parameters.get(param).isBlank()) {
				log.warn("Parámetro requerido faltante: {} para tipo: {}", param, emailType.name());
				throw EmailServiceException.missingParameter(emailType.name(), param);
			}
		}

		log.debug("Validación de parámetros exitosa para tipo: {}", emailType.name());
	}
}
