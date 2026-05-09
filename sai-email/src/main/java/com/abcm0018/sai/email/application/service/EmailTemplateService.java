package com.abcm0018.sai.email.application.service;

import java.util.List;
import java.util.Map;

import com.abcm0018.sai.email.application.dtos.EmailTemplateDTO;

public interface EmailTemplateService {
	/**
	 * Construye el cuerpo del email procesando la plantilla con los parámetros
	 */
	String buildBody(String templateName, Map<String, String> parameters);

	/**
	 * Obtiene el asunto de la plantilla
	 */
	String getSubject(String templateName);

	/**
	 * Obtiene toda la plantilla como DTO
	 */
	EmailTemplateDTO getTemplate(String templateName);

	/**
	 * Obtiene todas las plantillas disponibles
	 */
	List<EmailTemplateDTO> getAllTemplates();

	/**
	 * Busca plantillas por palabra clave
	 */
	List<EmailTemplateDTO> searchTemplates(String search);
}
