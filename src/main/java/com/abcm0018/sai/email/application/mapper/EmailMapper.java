package com.abcm0018.sai.email.application.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.abcm0018.sai.email.application.dtos.EmailTemplateDTO;
import com.abcm0018.sai.email.application.dtos.SendEmailResponseDTO;
import com.abcm0018.sai.email.domain.entity.EmailLog;
import com.abcm0018.sai.email.domain.entity.EmailTemplate;

/**
 * Mapper para convertir entre Email Entities y DTOs
 * MapStruct genera la implementación automáticamente en tiempo de compilación
 */
@Mapper(
		componentModel = "spring",
		unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE
)
public interface EmailMapper {
	/**
	 * Convierte EmailTemplate Entity a EmailTemplateDTO
	 */
	@Mapping(target = "id", source = "id")
	@Mapping(target = "templateName", source = "templateName")
	@Mapping(target = "subject", source = "subject")
	@Mapping(target = "body", source = "body")
	EmailTemplateDTO templateToDTO(EmailTemplate template);

	/**
	 * Convierte lista de EmailTemplate a EmailTemplateDTO
	 */
	List<EmailTemplateDTO> templateToDTOList(List<EmailTemplate> templates);

	/**
	 * Convierte EmailLog Entity a SendEmailResponseDTO
	 * MapStruct mapea automáticamente el enum EmailStatus
	 */
	@Mapping(target = "id", source = "id")
	@Mapping(target = "recipient", source = "recipient")
	@Mapping(target = "subject", source = "subject")
	@Mapping(target = "emailType", source = "emailType")
	@Mapping(target = "status", source = "status")
	@Mapping(target = "sentAt", source = "sentAt")
	@Mapping(target = "errorMessage", source = "errorMessage")
	SendEmailResponseDTO emailLogToResponseDTO(EmailLog emailLog);

	/**
	 * Convierte lista de EmailLog a SendEmailResponseDTO
	 */
	List<SendEmailResponseDTO> emailLogToResponseDTOList(List<EmailLog> emailLogs);
}
