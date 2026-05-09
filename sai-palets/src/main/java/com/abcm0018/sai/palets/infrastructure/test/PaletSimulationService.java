package com.abcm0018.sai.palets.infrastructure.test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.abcm0018.sai.palets.infrastructure.messaging.dtos.PaletLecturaMessageDTO;
import com.abcm0018.sai.palets.infrastructure.messaging.config.MqttProperties;
import com.abcm0018.sai.palets.infrastructure.messaging.config.MqttProducerConfig.MqttOutboundGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"simulation-low", "simulation-stress"})
public class PaletSimulationService {

	private final ObjectMapper objectMapper;
	private final MqttProperties mqttProperties;
	private final MqttOutboundGateway mqttGateway;

	// Usamos Faker para generar datos únicos
	private static final Faker faker = new Faker(new Locale("es-ES"));

	private static final String VALID_EAN_IN_DB = "28410000101014";
	private static final String VALID_EMPLOYEE_IN_DB = "1001";
	private static final LocalDate VALID_SHIFT_DATE = LocalDate.of(2025, 11, 4);

	/**
	 * SIMULACIÓN DE CARGA BAJA (Goteo)
	 * Se activa con el perfil "simulation-low"
	 * Envía 1 palet cada 5 minutos (300,000 ms)
	 */
	@Scheduled(fixedRate = 120000)
	@Profile("simulation-low")
	public void simulatePaletScanLow() {
		log.info("⏱️ (LOW) SIMULADOR: Generando nuevo palet (cada 5 min)...");
		try {
			// 1. Crear el DTO
			PaletLecturaMessageDTO message = createMockPalet();

			// 2. Serializar DTO a JSON (MQTT envía Strings o bytes, no objetos Java)
			String payload = objectMapper.writeValueAsString(message);

			// 3. Obtener el topic de las propiedades
			String topic = mqttProperties.getTopic().getEscaneos();

			// 4. Enviar usando la Gateway de MQTT
			mqttGateway.sendToMqtt(payload, topic);

			log.info("✅ (LOW) SIMULADOR: Mensaje enviado con SSCC: {}", message.getSscc());
		} catch (Exception e) {
			log.error("❌ (LOW) SIMULADOR: Error al enviar mensaje: {}", e.getMessage(), e);
		}
	}

	private static PaletLecturaMessageDTO createMockPalet() {

		String uniqueSscc = String.format("00%s", faker.number().randomNumber(16, true));
		String uniqueBatch = "LOTE-SIM-" + faker.number().digits(5);
		PaletLecturaMessageDTO paletData = new PaletLecturaMessageDTO();
		paletData.setSscc(uniqueSscc);
		paletData.setBatchNumber(uniqueBatch);

		// Usamos los datos VALIDOS de tu BBDD
		paletData.setEan(VALID_EAN_IN_DB);
		paletData.setEmployeeNumber(VALID_EMPLOYEE_IN_DB);
		paletData.setScanDate(VALID_SHIFT_DATE.atStartOfDay()); // Usamos la fecha válida

		// Fechas relativas
		paletData.setPackagingDateTime(LocalDateTime.of(VALID_SHIFT_DATE.minusDays(1), java.time.LocalTime.of(8, 30)));
		paletData.setProductUseByDate(VALID_SHIFT_DATE.plusMonths(6));
		return paletData;
	}
}
