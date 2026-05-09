package com.abcm0018.sai.palets.infrastructure.messaging.consumer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.abcm0018.sai.palets.application.service.PaletService;
import com.abcm0018.sai.palets.infrastructure.messaging.dtos.PaletLecturaMessageDTO;
import com.abcm0018.sai.palets.infrastructure.messaging.config.MqttProperties;
import com.abcm0018.sai.palets.infrastructure.messaging.engine.ScanCorrelationEngine;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttPaletConsumerService {

//	private final PaletService paletService;
	private final ScanCorrelationEngine scanCorrelationEngine;
	private final MqttProperties mqttProperties;
	private final ObjectMapper objectMapper;

	/**
	 * Este es el Service Activator.
	 * Recibe el mensaje genérico de Spring Integration.
	 */
	public void handleMqttMessage(Message<?> message) {
		String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
		String payload = message.getPayload().toString();

		try {
			// PASO 1: Filtrar por Topic
			if (!StringUtils.isBlank(topic) && topic.equals(mqttProperties.getTopic().getEscaneos())) {
				// PASO 2: Deserializar el payload
				PaletLecturaMessageDTO paletLecturaMessage = objectMapper.readValue(payload, PaletLecturaMessageDTO.class);

				logMensajeRecibido(paletLecturaMessage, topic);

//				paletService.procesarNuevaLecturaPalet(paletLecturaMessage);
				scanCorrelationEngine.processIncomingScan(paletLecturaMessage);

				log.debug("Palet procesado exitosamente (MQTT) - SSCC: {}", paletLecturaMessage.getSscc());
			} else if (!StringUtils.isBlank(topic) && topic.equals(mqttProperties.getTopic().getAlertas())) {
				log.warn("Alerta MQTT recibida en Topic [{}]: {}", topic, payload);
			}
		} catch (Exception e) {
			// R8: Registrar el mensaje fallido con todo el contexto necesario para su análisis
			// o reintento manual. Punto de extensión para una DLQ real (Redis, tabla de auditoría).
			log.error("╔════════════ ERROR MQTT — MENSAJE NO PROCESADO ════════════");
			log.error("║ Topic:     {}", topic);
			log.error("║ Error:     {} — {}", e.getClass().getSimpleName(), e.getMessage());
			log.error("║ Payload:   {}", payload);
			log.error("╚═══════════════════════════════════════════════════════════");
		}
	}

	/**
	 * Helper de logging
	 */
	private void logMensajeRecibido(PaletLecturaMessageDTO mensaje, String topic) {
		log.info("╔════════════════════════════════════════════════════════");
		log.info("║ 📦 LECTURA DE PALET RECIBIDA (MQTT)");
		log.info("║ Topic:             {}", topic);
		log.info("╠════════════════════════════════════════════════════════");
		log.info("║ SSCC:              {}", mensaje.getSscc());
		log.info("║ EAN:               {}", mensaje.getEan());
		log.info("║ Lote:              {}", mensaje.getBatchNumber());
		log.info("║ Empleado:          {}", mensaje.getEmployeeNumber());
		log.info("╚════════════════════════════════════════════════════════");
	}
}
