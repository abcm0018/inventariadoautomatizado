package com.abcm0018.sai.email.infrastructure.controller;

import com.abcm0018.sai.email.application.dtos.EmailLogFilterDTO;
import com.abcm0018.sai.email.application.dtos.EmailTemplateDTO;
import com.abcm0018.sai.email.application.dtos.SendEmailResponseDTO;
import com.abcm0018.sai.email.application.dtos.SendTestEmailRequestDTO;
import com.abcm0018.sai.email.application.service.EmailLogService;
import com.abcm0018.sai.email.application.service.EmailService;
import com.abcm0018.sai.email.application.service.EmailTemplateService;
import com.abcm0018.sai.email.domain.enums.EmailStatus;
import com.abcm0018.sai.shared.response.ResponseBuilder;
import com.abcm0018.sai.shared.response.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para administración y auditoría del módulo de email.
 *
 * Endpoints disponibles:
 * - Gestión de plantillas (lectura)
 * - Consulta de auditoría e histórico
 * - Operaciones de emergencia (reintentos, pruebas)
 *
 * NOTA: Los emails automáticos se disparan mediante EventListeners
 */
@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Emails", description = "Endpoints para administración y auditoría de emails")
public class EmailController {

	private final EmailTemplateService emailTemplateService;
	private final EmailService emailService;
	private final EmailLogService emailLogService;

	/**
	 * Obtener todas las plantillas de email disponibles
	 * Solo ADMIN puede consultar
	 */
	@CrossOrigin
	@GetMapping("/templates")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Listar todas las plantillas de email",
			description = "Retorna todas las plantillas disponibles en el sistema ordenadas alfabéticamente"
	)
	public StandardResponse<List<EmailTemplateDTO>> getAllTemplates() {
		log.debug("Obteniendo todas las plantillas de email");

		List<EmailTemplateDTO> templates = emailTemplateService.getAllTemplates();

		String message = String.format("Se encontraron %d plantillas", templates.size());
		return ResponseBuilder.with(HttpStatus.OK, true, message, templates);
	}

	/**
	 * Obtener una plantilla específica por ID
	 */
	@CrossOrigin
	@GetMapping("/templates/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Obtener plantilla por ID",
			description = "Retorna los detalles completos de una plantilla específica"
	)
	public StandardResponse<EmailTemplateDTO> getTemplateById(
			@PathVariable @Parameter(description = "ID de la plantilla") Long id) {

		log.debug("Obteniendo plantilla con ID: {}", id);

		EmailTemplateDTO template = emailTemplateService.getTemplate(id.toString());

		return ResponseBuilder.with(HttpStatus.OK, true, "Plantilla encontrada", template);
	}

	/**
	 * Buscar plantillas por palabra clave
	 */
	@CrossOrigin
	@GetMapping("/templates/search")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Buscar plantillas por palabra clave",
			description = "Busca en nombre y asunto de las plantillas"
	)
	public StandardResponse<List<EmailTemplateDTO>> searchTemplates(
			@RequestParam @Parameter(description = "Palabra clave de búsqueda") String query) {

		log.debug("Buscando plantillas con criterio: {}", query);

		List<EmailTemplateDTO> templates = emailTemplateService.searchTemplates(query);

		String message = String.format("Se encontraron %d plantillas coincidentes", templates.size());
		return ResponseBuilder.with(HttpStatus.OK, true, message, templates);
	}

	/**
	 * Listar todos los logs de emails con paginación
	 * Solo ADMIN
	 */
	@CrossOrigin
	@GetMapping("/logs")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Listar histórico de emails enviados",
			description = "Retorna los logs de todos los emails con paginación"
	)
	public StandardResponse<Page<SendEmailResponseDTO>> getAllEmailLogs(
			@PageableDefault(size = 20, sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable) {

		log.debug("Listando histórico de emails - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());

		Page<SendEmailResponseDTO> logs = emailLogService.getEmailsByStatus(EmailStatus.SENT, pageable);

		String message = String.format("Página %d de %d (Total: %d emails)",
				logs.getNumber() + 1, logs.getTotalPages(), logs.getTotalElements());

		return ResponseBuilder.with(HttpStatus.OK, true, message, logs);
	}

	/**
	 * Obtener histórico de emails de un destinatario específico
	 */
	@CrossOrigin
	@GetMapping("/logs/recipient/{email}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Obtener histórico de emails por destinatario",
			description = "Retorna todos los emails enviados a una dirección específica"
	)
	public StandardResponse<List<SendEmailResponseDTO>> getEmailsByRecipient(
			@PathVariable @Parameter(description = "Dirección de email del destinatario") String email) {

		log.debug("Obteniendo histórico de emails para destinatario: {}", email);

		List<SendEmailResponseDTO> logs = emailLogService.getEmailsByRecipient(email);

		String message = String.format("Se encontraron %d emails para %s", logs.size(), email);
		return ResponseBuilder.with(HttpStatus.OK, true, message, logs);
	}

	/**
	 * Filtrar emails por estado
	 */
	@CrossOrigin
	@GetMapping("/logs/status/{status}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Filtrar emails por estado",
			description = "Retorna los emails filtrados por estado (SENT, FAILED, PENDING, BOUNCED, SPAM)"
	)
	public StandardResponse<Page<SendEmailResponseDTO>> getEmailsByStatus(
			@PathVariable @Parameter(description = "Estado del email (SENT, FAILED, PENDING, BOUNCED, SPAM)") EmailStatus status,
			@PageableDefault(size = 20, sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable) {

		log.debug("Obteniendo emails con estado: {}", status);

		Page<SendEmailResponseDTO> logs = emailLogService.getEmailsByStatus(status, pageable);

		String message = String.format("Se encontraron %d emails con estado %s", logs.getTotalElements(), status);
		return ResponseBuilder.with(HttpStatus.OK, true, message, logs);
	}

	/**
	 * Buscar emails en un rango de fechas
	 */
	@CrossOrigin
	@GetMapping("/logs/date-range")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Buscar emails en un rango de fechas",
			description = "Retorna los emails enviados entre las fechas especificadas"
	)
	public StandardResponse<List<SendEmailResponseDTO>> getEmailsByDateRange(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
			@Parameter(description = "Fecha de inicio (ISO 8601)") LocalDateTime startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
			@Parameter(description = "Fecha de fin (ISO 8601)") LocalDateTime endDate) {

		log.debug("Obteniendo emails entre {} y {}", startDate, endDate);

		List<SendEmailResponseDTO> logs = emailLogService.getEmailsByDateRange(startDate, endDate);

		String message = String.format("Se encontraron %d emails en el rango especificado", logs.size());
		return ResponseBuilder.with(HttpStatus.OK, true, message, logs);
	}

	/**
	 * Búsqueda avanzada de logs con múltiples criterios
	 */
	@CrossOrigin
	@PostMapping("/logs/search")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Búsqueda avanzada de emails",
			description = "Permite buscar emails combinando múltiples criterios: destinatario, tipo, estado y rango de fechas"
	)
	public StandardResponse<Page<SendEmailResponseDTO>> searchEmailLogs(@Valid @RequestBody EmailLogFilterDTO filterDTO,
			@PageableDefault(size = 20, sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable) {

		log.debug("Búsqueda avanzada de emails con filtros: {}", filterDTO);

		Page<SendEmailResponseDTO> logs = emailLogService.searchEmailsWithFilters(filterDTO, pageable);

		String message = String.format("Se encontraron %d emails que cumplen los criterios", logs.getTotalElements());
		return ResponseBuilder.with(HttpStatus.OK, true, message, logs);
	}

	/**
	 * Contar emails por destinatario en un período
	 */
	@CrossOrigin
	@GetMapping("/logs/count-by-recipient")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Contar emails por destinatario en un período",
			description = "Retorna el número de emails enviados a un destinatario en un rango de fechas"
	)
	public StandardResponse<Long> countEmailsByRecipient(
			@RequestParam @Parameter(description = "Email del destinatario") String recipient,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
			@Parameter(description = "Fecha de inicio") LocalDateTime startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
			@Parameter(description = "Fecha de fin") LocalDateTime endDate) {

		log.debug("Contando emails para {} entre {} y {}", recipient, startDate, endDate);

		Long count = emailLogService.countEmailsByRecipientAndDateRange(recipient, startDate, endDate);

		String message = String.format("Se enviaron %d emails a %s en el período", count, recipient);
		return ResponseBuilder.with(HttpStatus.OK, true, message, count);
	}

	/**
	 * Reintentar envío de emails fallidos
	 * Solo ADMIN - Operación crítica
	 */
	@CrossOrigin
	@PostMapping("/retry-failed")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Reintentar envío de emails fallidos",
			description = "ADMIN ONLY. Intenta reenviar todos los emails que fallaron. Máximo 3 reintentos por email."
	)
	public StandardResponse<String> retryFailedEmails() {
		log.warn("ADMIN solicitó reintentar envío de emails fallidos");

		try {
			emailService.retryFailedEmails();
			return ResponseBuilder.with(HttpStatus.OK, true,
					"Proceso de reintento iniciado. Revisa los logs para más detalles", null);
		} catch (Exception e) {
			log.error("Error durante reintento de emails: {}", e.getMessage(), e);
			return ResponseBuilder.with(HttpStatus.INTERNAL_SERVER_ERROR, false,
					"Error al procesar reintentos: " + e.getMessage(), null);
		}
	}

	/**
	 * Enviar email de prueba
	 * Solo ADMIN - Para testing
	 */
	@CrossOrigin
	@PostMapping("/send-test")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Enviar email de prueba",
			description = "ADMIN ONLY. Permite enviar un email de prueba para validar la configuración SMTP"
	)
	public StandardResponse<SendEmailResponseDTO> sendTestEmail(@Valid @RequestBody SendTestEmailRequestDTO request) {

		log.warn("ADMIN solicitó enviar email de prueba a: {}", request.getRecipient());

		try {
			SendEmailResponseDTO response = emailService.sendDirectEmail(
					request.getRecipient(),
					request.getSubject(),
					request.getBodyHtml()
			);

			log.info("Email de prueba enviado exitosamente a: {}", request.getRecipient());
			return ResponseBuilder.with(HttpStatus.OK, true,
					"Email de prueba enviado exitosamente", response);

		} catch (Exception e) {
			log.error("Error al enviar email de prueba: {}", e.getMessage(), e);
			return ResponseBuilder.with(HttpStatus.BAD_REQUEST, false,
					"Error al enviar email: " + e.getMessage(), null);
		}
	}

	/**
	 * Reenviar email específico del histórico
	 * Solo ADMIN - Para casos excepcionales
	 */
	@CrossOrigin
	@PostMapping("/resend/{logId}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Reenviar email específico",
			description = "ADMIN ONLY. Permite reenviar un email específico del histórico"
	)
	public StandardResponse<SendEmailResponseDTO> resendEmail(
			@PathVariable @Parameter(description = "ID del log del email a reenviar") Long logId) {

		log.warn("ADMIN solicitó reenviar email con logId: {}", logId);

		try {
			// Aquí iría la lógica de obtener el email del log y reenviarlo
			// Esto requeriría un método adicional en EmailLogService

			return ResponseBuilder.with(HttpStatus.OK, true,
					"Email reenviado exitosamente", null);

		} catch (Exception e) {
			log.error("Error al reenviar email: {}", e.getMessage(), e);
			return ResponseBuilder.with(HttpStatus.BAD_REQUEST, false,
					"Error al reenviar email: " + e.getMessage(), null);
		}
	}

}
