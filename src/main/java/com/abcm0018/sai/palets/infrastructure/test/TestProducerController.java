package com.abcm0018.sai.palets.infrastructure.test;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abcm0018.sai.palets.infrastructure.messaging.config.RabbitMQConfig;
import com.abcm0018.sai.palets.infrastructure.messaging.dtos.PaletLecturaMessageDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/test-producer")
@Profile("dev")
@RequiredArgsConstructor
public class TestProducerController {

	private final RabbitTemplate rabbitTemplate;

	@RequestMapping("/send-palet")
	public ResponseEntity<String> sendPalet(@RequestBody PaletLecturaMessageDTO message) {

		log.info("--- SIMULADOR: Recibida petición para enviar mensaje a RabbitMQ ---");

		try {
			// Usamos el convertAndSend que deserialozará el DTO a JSON automaticamente
			rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);

			String logMsg = "✅ SIMULADOR: Mensaje enviado a RabbitMQ para SSCC: " + message.getSscc();
			log.info(logMsg);
			return ResponseEntity.ok(logMsg);
		} catch (Exception e) {
			String errorMsg = "❌ SIMULADOR: Error enviando mensaje: " + e.getMessage();
			log.error(errorMsg, e);
			return ResponseEntity.internalServerError().body(errorMsg);
		}
	}
}
