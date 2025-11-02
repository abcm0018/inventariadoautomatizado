package com.abcm0018.sai.palets.infrastructure.test;

import java.time.LocalDate;
import java.util.Locale;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.abcm0018.sai.palets.infrastructure.messaging.config.RabbitMQConfig;
import com.abcm0018.sai.palets.infrastructure.messaging.dtos.PaletLecturaMessageDTO;
import com.github.javafaker.Faker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"simulation-low", "simulation-stress"})
public class PaletSimulationService {

	private final RabbitTemplate rabbitTemplate;

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
			PaletLecturaMessageDTO message = createMockPalet();
			rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
			log.info("✅ (LOW) SIMULADOR: Mensaje enviado con SSCC: {}", message.getSscc());
		} catch (Exception e) {
			log.error("❌ (LOW) SIMULADOR: Error al enviar mensaje: {}", e.getMessage(), e);
		}
	}

	/**
	 * SIMULACIÓN DE CARGA ALTA (Estrés)
	 * Se activa con el perfil "simulation-stress"
	 * Envía 1 palet cada 10 segundos (10,000 ms)
	 */
//	@Scheduled(fixedRate = 10000)
//	@Profile("simulation-stress")
//	public void simulatePaletScanStress() {
//		log.info("🚀 (STRESS) SIMULADOR: Generando nuevo palet (cada 10 seg)...");
//		try {
//			PaletLecturaMessageDTO message = createMockPalet();
//			rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
//			log.info("🔥 (STRESS) SIMULADOR: Mensaje enviado con SSCC: {}", message.getSscc());
//		} catch (Exception e) {
//			log.error("❌ (STRESS) SIMULADOR: Error al enviar mensaje: {}", e.getMessage(), e);
//		}
//	}

	private static PaletLecturaMessageDTO createMockPalet() {

		String uniqueSscc = String.format("00%s", faker.number().randomNumber(16, true));
		String uniqueBatch = "LOTE-SIM-" + faker.number().digits(5);
		PaletLecturaMessageDTO paletData = new PaletLecturaMessageDTO();
		paletData.setSscc(uniqueSscc);
		paletData.setBatchNumber(uniqueBatch);

		// Usamos los datos VALIDOS de tu BBDD
		paletData.setEan(VALID_EAN_IN_DB);
		paletData.setEmployeeNumber(VALID_EMPLOYEE_IN_DB);
		paletData.setScanDate(VALID_SHIFT_DATE); // Usamos la fecha válida

		// Fechas relativas
		paletData.setPackagingDate(VALID_SHIFT_DATE.minusDays(1));
		paletData.setProductUseByDate(VALID_SHIFT_DATE.plusMonths(6));
		paletData.setProductionTime("08:30");
		return paletData;
	}
}
