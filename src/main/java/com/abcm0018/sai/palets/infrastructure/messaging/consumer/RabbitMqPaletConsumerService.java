package com.abcm0018.sai.palets.infrastructure.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.abcm0018.sai.palets.infrastructure.messaging.config.RabbitMQConfig;
import com.abcm0018.sai.palets.infrastructure.messaging.dtos.PaletLecturaMessageDTO;
import com.abcm0018.sai.palets.application.service.PaletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMqPaletConsumerService {
	// Para insertar datos en la base de datos
	private final PaletService paletService;

	@RabbitListener(queues = RabbitMQConfig.QUEUE_LECTURAS)
	public void procesarLecturaPalet(PaletLecturaMessageDTO message) {
		try {
			logMensajeRecibido(message);

			// PASO 3 (Validación de Negocio) y PASOS 4, 5, 6
			// Delegamos toda la lógica de negocio al servicio de aplicación.
			// Esto incluye la validación de negocio, la creación del palet,
			// la actualización de Redis y la notificación por WebSocket.
			paletService.procesarNuevaLecturaPalet(message);

			log.info("✅ Palet procesado exitosamente - SSCC: {}", message.getSscc());
		} catch (Exception e) {
			// Si PaletService lanza una excepción (ej. SSCC duplicado, EAN no encontrado),
			// la capturamos aquí.
			log.error("❌ Error procesando lectura del palet: {}", e.getMessage());
			log.error("    SSCC: {}", message.getSscc());

			// Re-lanzamos la excepción para que RabbitMQ la gestione
			// (reintentos y envío a DLQ).
			throw new RuntimeException("Error procesando mensaje: ", e);
		}
	}

	private void logMensajeRecibido(PaletLecturaMessageDTO mensaje) {
		log.info("╔════════════════════════════════════════════════════════");
		log.info("║ 📦 LECTURA DE PALET RECIBIDA");
		log.info("╠════════════════════════════════════════════════════════");
		log.info("║ SSCC:              {}", mensaje.getSscc());
		log.info("║ EAN:               {}", mensaje.getEan());
		log.info("║ Lote:              {}", mensaje.getBatchNumber());
		log.info("║ Empleado:          {}", mensaje.getEmployeeNumber());
		log.info("╚════════════════════════════════════════════════════════");
	}

	/**
	 * Listener para Dead Letter Queue
	 * Captura mensajes que:
	 * - Fallaron la validación
	 * - Tuvieron errores de procesamiento después de reintentos
	 */
	@RabbitListener(queues = RabbitMQConfig.QUEUE_DLQ)
	public void procesarPaletsFallidos(PaletLecturaMessageDTO mensaje) {
		log.error("☠️ Palet enviado a Dead Letter Queue");
		log.error("╔════════════════════════════════════════════════════════");
		log.error("║ SSCC:              {}", mensaje.getSscc());
		log.error("║ EAN:               {}", mensaje.getEan());
		log.error("║ Lote:              {}", mensaje.getBatchNumber());
		log.error("║ Empleado:          {}", mensaje.getEmployeeNumber());
		log.error("╚════════════════════════════════════════════════════════");

		// TODO: Implementar acciones de contingencia
		// - Guardar en tabla de errores para revisión
		// - Enviar email a supervisores
		// - Crear alerta en sistema de monitoreo
		// - Registrar incidencia en sistema de calidad
	}
}
