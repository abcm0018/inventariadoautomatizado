package com.abcm0018.sai.email.application.events;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.abcm0018.sai.email.application.dtos.SendEmailRequestDTO;
import com.abcm0018.sai.email.application.service.EmailService;
import com.abcm0018.sai.email.domain.enums.EmailType;
import com.abcm0018.sai.email.domain.events.BatchReceivedEvent;
import com.abcm0018.sai.email.domain.events.CriticalStockAlertEvent;
import com.abcm0018.sai.email.domain.events.LowStockAlertEvent;
import com.abcm0018.sai.email.domain.events.PaletExpiredEvent;
import com.abcm0018.sai.email.domain.events.PaletExpiryAlertEvent;
import com.abcm0018.sai.email.domain.events.PasswordResetEvent;
import com.abcm0018.sai.email.domain.events.UserCreatedEvent;
import com.abcm0018.sai.email.domain.events.WorkshiftAssignedEvent;
import com.abcm0018.sai.email.domain.events.WorkshiftCanceledEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Listener central para todos los eventos que disparan emails automáticos.
 * <p>
 * Escucha eventos del dominio y dispara el envío de emails correspondientes.
 * Implementa el patrón Publisher-Subscriber para desacoplamiento.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventListener {

	private final EmailService emailService;

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	/**
	 * Escucha el evento de alerta de stock bajo y envía un email.
	 */
	@Async
	@EventListener
	public void onLowStockAlert(LowStockAlertEvent alertEvent) {
		log.info("Enviando email de alerta de stock bajo para el producto: {} (id: {})", alertEvent.getProductName(), alertEvent.getProductId());

		try {
			Map<String, String> parameters = new HashMap<>();

			parameters.put("productName", alertEvent.getProductName());
			parameters.put("currentStock", alertEvent.getCurrentStock().toString());
			parameters.put("minimumStock", alertEvent.getMinimumStock().toString());
			parameters.put("alertDate", LocalDateTime.now().format(DATE_FORMATTER));

			SendEmailRequestDTO request = SendEmailRequestDTO.builder()
					.recipient(alertEvent.getRecipientEmail())
					.emailType(EmailType.LOW_STOCK_ALERT)
					.templateParameters(parameters)
					.build();

			emailService.sendEmail(request);

			log.info("✅ Email de alerta de stock bajo enviado exitosamente a: {}", alertEvent.getRecipientEmail());
		} catch (Exception e) {
			log.error("❌ Error al procesar LowStockAlert para {}: {}", alertEvent.getProductName(), e.getMessage(), e);
		}
	}

	/**
	 * Escucha evento de stock crítico y envía alerta urgente
	 */
	@Async
	@EventListener
	public void onCriticalStockAlert(CriticalStockAlertEvent event) {
		log.warn("⚠️Evento CriticalStockAlert recibido para producto: {}", event.getProductName());

		try {
			Map<String, String> params = new HashMap<>();
			params.put("productName", event.getProductName());
			params.put("currentStock", event.getCurrentStock().toString());
			params.put("supervisorName", "Supervisor"); // Se puede mejorar con el nombre real

			SendEmailRequestDTO request = SendEmailRequestDTO.builder()
					.recipient(event.getSupervisorEmail())
					.emailType(EmailType.CRITICAL_STOCK_ALERT)
					.templateParameters(params)
					.build();

			emailService.sendEmail(request);
			log.warn("⚠️Email de alerta de stock crítico enviado a: {}", event.getSupervisorEmail());

		} catch (Exception e) {
			log.error("❌ Error al procesar CriticalStockAlert para {}: {}", event.getProductName(), e.getMessage(), e);
		}
	}

	/**
	 * Escucha evento de palets próximos a caducar
	 */
	@Async
	@EventListener
	public void onPaletExpiryAlert(PaletExpiryAlertEvent event) {
		log.info("Evento PaletExpiryAlert recibido para lote: {}", event.getBatchNumber());

		try {
			Map<String, String> params = new HashMap<>();
			params.put("productName", event.getProductName());
			params.put("batchNumber", event.getBatchNumber());
			params.put("expiryDate", event.getExpiryDate().format(DATE_FORMATTER));
			params.put("daysRemaining", event.getDaysRemaining().toString());
			params.put("reportDate", java.time.LocalDate.now().format(DATE_FORMATTER));

			SendEmailRequestDTO request = SendEmailRequestDTO.builder()
					.recipient(event.getRecipientEmail())
					.emailType(EmailType.PALET_EXPIRY_SOON)
					.templateParameters(params)
					.build();

			emailService.sendEmail(request);
			log.info("✅Email de alerta de caducidad próxima enviado a: {}", event.getRecipientEmail());

		} catch (Exception e) {
			log.error("❌Error al procesar PaletExpiryAlert para lote {}: {}", event.getBatchNumber(), e.getMessage(), e);
		}
	}

	/**
	 * Escucha evento de palets expirados
	 */
	@Async
	@EventListener
	public void onPaletExpired(PaletExpiredEvent event) {
		log.warn("Evento PaletExpired recibido para lote: {}", event.getBatchNumber());

		try {
			Map<String, String> params = new HashMap<>();
			params.put("productName", event.getProductName());
			params.put("batchNumber", event.getBatchNumber());
			params.put("ssccList", String.join(", ", event.getSscc()));
			params.put("expiryDate", event.getExpiryDate().format(DATE_FORMATTER));
			params.put("supervisorName", "Supervisor");

			SendEmailRequestDTO request = SendEmailRequestDTO.builder()
					.recipient(event.getSupervisorEmail())
					.emailType(EmailType.PALET_EXPIRED)
					.templateParameters(params)
					.build();

			emailService.sendEmail(request);
			log.warn("⚠️Email de alerta de palets expirados enviado a: {}", event.getSupervisorEmail());

		} catch (Exception e) {
			log.error("❌Error al procesar PaletExpired para lote {}: {}", event.getBatchNumber(), e.getMessage(), e);
		}
	}

	/**
	 * Escucha evento de recepción de lote
	 */
	@Async
	@EventListener
	public void onBatchReceived(BatchReceivedEvent event) {
		log.info("Evento BatchReceived recibido: Lote {}", event.getBatchNumber());

		try {
			Map<String, String> params = new HashMap<>();
			params.put("employeeName", event.getEmployeeName());
			params.put("batchNumber", event.getBatchNumber());
			params.put("quantity", event.getQuantity().toString());
			params.put("receivedDate", event.getReceivedDate().format(
					DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
			params.put("operator", event.getEmployeeName());

			SendEmailRequestDTO request = SendEmailRequestDTO.builder()
					.recipient(event.getOperatorEmail())
					.emailType(EmailType.BATCH_RECEIVED)
					.templateParameters(params)
					.build();

			emailService.sendEmail(request);
			log.info("✅Email de confirmación de lote recibido enviado a: {}", event.getOperatorEmail());

		} catch (Exception e) {
			log.error("❌Error al procesar BatchReceived para lote {}: {}", event.getBatchNumber(), e.getMessage(), e);
		}
	}

	/**
	 * Escucha evento de turno asignado
	 */
	@Async
	@EventListener
	public void onWorkshiftAssigned(WorkshiftAssignedEvent event) {
		log.info("Evento WorkshiftAssigned recibido para empleado: {}", event.getEmployeeName());

		try {
			Map<String, String> params = new HashMap<>();
			params.put("employeeName", event.getEmployeeName());
			params.put("shiftDate", event.getShiftDate().format(DATE_FORMATTER));
			params.put("shiftType", event.getShiftType());
			params.put("startTime", event.getStartTime().format(TIME_FORMATTER));
			params.put("endTime", event.getEndTime().format(TIME_FORMATTER));

			SendEmailRequestDTO request = SendEmailRequestDTO.builder()
					.recipient(event.getEmployeeEmail())
					.emailType(EmailType.WORKSHIFT_ASSIGNED)
					.templateParameters(params)
					.build();

			emailService.sendEmail(request);
			log.info("Email de turno asignado enviado a: {}", event.getEmployeeEmail());

		} catch (Exception e) {
			log.error("Error al procesar WorkshiftAssigned para {}: {}", event.getEmployeeName(), e.getMessage(), e);
		}
	}

	/**
	 * Escucha evento de turno cancelado
	 */
	@Async
	@EventListener
	public void onWorkshiftCanceled(WorkshiftCanceledEvent event) {
		log.warn("Evento WorkshiftCanceled recibido para empleado: {}", event.getEmployeeName());

		try {
			Map<String, String> params = new HashMap<>();
			params.put("employeeName", event.getEmployeeName());
			params.put("shiftDate", event.getShiftDate().format(DATE_FORMATTER));
			params.put("shiftType", event.getShiftType());
			params.put("cancellationReason", event.getCancellationReason());

			SendEmailRequestDTO request = SendEmailRequestDTO.builder()
					.recipient(event.getEmployeeEmail())
					.emailType(EmailType.WORKSHIFT_CANCELED)
					.templateParameters(params)
					.build();

			emailService.sendEmail(request);
			log.warn("⚠️Email de turno cancelado enviado a: {}", event.getEmployeeEmail());

		} catch (Exception e) {
			log.error("❌Error al procesar WorkshiftCanceled para {}: {}", event.getEmployeeName(), e.getMessage(), e);
		}
	}

	/**
	 * Escucha evento de nuevo usuario registrado
	 */
	@Async
	@EventListener
	public void onUserCreated(UserCreatedEvent event) {
		log.info("Evento UserCreated recibido para usuario: {}", event.getEmployeeNumber());

		try {
			Map<String, String> params = new HashMap<>();
			params.put("name", event.getName());
			params.put("surname", event.getSurname());
			params.put("employeeNumber", event.getEmployeeNumber());
			params.put("password", event.getTemporaryPassword());

			SendEmailRequestDTO request = SendEmailRequestDTO.builder()
					.recipient(event.getUserEmail())
					.emailType(EmailType.NEW_EMPLOYEE_CREDENTIALS)
					.templateParameters(params)
					.build();

			emailService.sendEmail(request);
			log.info("✅Email de bienvenida enviado a: {}", event.getUserEmail());

		} catch (Exception e) {
			log.error("❌Error al procesar UserCreated para {}: {}", event.getEmployeeNumber(), e.getMessage(), e);
		}
	}

	/**
	 * Escucha evento de solicitud de reseteo de contraseña
	 */
	@Async
	@EventListener
	public void onPasswordResetRequested(PasswordResetEvent event) {
		log.info("Evento PasswordResetRequested recibido para: {}", event.getUserEmail());

		try {
			Map<String, String> params = new HashMap<>();
			params.put("name", event.getName());
			params.put("surname", event.getSurname());
			params.put("temporaryPassword", event.getTemporaryPassword());
			params.put("eamil", event.getUserEmail());

			SendEmailRequestDTO request = SendEmailRequestDTO.builder()
					.recipient(event.getUserEmail())
					.emailType(EmailType.PASSWORD_RESET)
					.templateParameters(params)
					.build();

			emailService.sendEmail(request);
			log.info("✅Email de reseteo de contraseña enviado a: {}", event.getUserEmail());

		} catch (Exception e) {
			log.error("❌Error al procesar PasswordResetRequested para {}: {}", event.getUserEmail(), e.getMessage(), e);
		}
	}
}
