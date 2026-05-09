package com.abcm0018.sai.email.application.service.impl;

import java.util.List;
import java.util.Map;

import com.abcm0018.sai.email.application.dtos.EmailTemplateDTO;
import com.abcm0018.sai.email.application.mapper.EmailMapper;
import com.abcm0018.sai.email.application.service.EmailTemplateService;
import com.abcm0018.sai.email.domain.entity.EmailTemplate;
import com.abcm0018.sai.email.domain.repository.EmailTemplateRepository;
import com.abcm0018.sai.email.exception.EmailServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {
	
	private final EmailMapper emailMapper;
	private final SpringTemplateEngine templateEngine;
	private final EmailTemplateRepository emailTemplateRepository;

	@Override
	public String buildBody(String templateName, Map<String, String> parameters) {
		
		log.debug("Construyendo cuerpo de email para plantilla: {}", templateName);

		EmailTemplate emailTemplate = getEmailTemplate(templateName);

		try {
			Context context = new Context();
			parameters.forEach(context::setVariable);

			String processedBody = templateEngine.process(emailTemplate.getBody(), context);
			log.debug("Cuerpo de email construido exitosamente para: {}", templateName);

			return processedBody;
		} catch (Exception e) {
			log.error("Error procesando plantilla {}: {}", templateName, e.getMessage(), e);
			throw EmailServiceException.templateProcessingError(templateName, e);
		}
	}

	@Override
	public String getSubject(String templateName) {
		log.debug("Obteniendo asunto para plantilla: {}", templateName);

		return getEmailTemplate(templateName).getSubject();
	}

	@Override
	public EmailTemplateDTO getTemplate(String templateName) {
		log.debug("Obteniendo plantilla completa: {}", templateName);

		EmailTemplate template = getEmailTemplate(templateName);

		return emailMapper.templateToDTO(template);
	}

	@Override
	public List<EmailTemplateDTO> getAllTemplates() {
		log.debug("Obteniendo todas las plantillas");
		List<EmailTemplateDTO> templates = emailMapper.templateToDTOList(emailTemplateRepository.findAllOrderByName());
		log.info("Se encontraron {} plantillas", templates.size());
		return templates;
	}

	@Override
	public List<EmailTemplateDTO> searchTemplates(String search) {
		log.debug("Buscando plantillas con criterio: {}", search);
		List<EmailTemplateDTO> results = emailMapper.templateToDTOList(emailTemplateRepository.searchTemplates(search));
		log.info("Se encontraron {} plantillas coincidentes", results.size());
		return results;
	}

	private EmailTemplate getEmailTemplate(String templateName) {
		return emailTemplateRepository.findByTemplateName(templateName).orElseThrow(() -> {
			log.error("Plantilla no encontrada: {}", templateName);
			return EmailServiceException.templateNotFound(templateName);
		});
	}
}
