package com.abcm0018.sai.palets.infrastructure.messaging.engine;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.abcm0018.sai.palets.application.service.PaletService;
import com.abcm0018.sai.palets.domain.enums.ScanQuality;
import com.abcm0018.sai.palets.infrastructure.messaging.dtos.PaletLecturaMessageDTO;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanCorrelationEngine {

	private final PaletService paletService;

	private final ConcurrentHashMap<String, PalletAggregationState> activeWindows = new ConcurrentHashMap<>();

	// Claves finalizadas recientemente: evita procesar mensajes tardíos que llegan
	// justo después de que el timeout cierra la ventana (R1).
	private final Set<String> recentlyFinalized = ConcurrentHashMap.newKeySet();

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

	private static final int WINDOW_TIMEOUT_SECONDS = 2;

	/**
	 * Punto de entrada principal llamado por el consumidor MQTT.
	 */
	public void processIncomingScan(PaletLecturaMessageDTO palletData) {
		if (palletData.getSscc() == null || palletData.getStationId() == null) {
			log.warn("Motor Correlación: Mensaje ignorado. Falta SSCC o StationId");
			return;
		}

		// Clave única: Ej. "IN_01_003841123456789012"
		String correlationKey = palletData.getStationId() + "_" + palletData.getSscc();

		// R1: Si esta clave ya fue finalizada recientemente (p.ej. el timeout disparó
		// antes de que llegara este mensaje tardío), ignorarlo en lugar de abrir una
		// segunda ventana que causaría un intento de persistencia duplicado.
		if (recentlyFinalized.contains(correlationKey)) {
			log.warn("Motor Correlación: Mensaje tardío ignorado para [{}] cámara [{}]. La ventana ya fue cerrada.",
					correlationKey, palletData.getCameraId());
			return;
		}

		// compute() bloquea la clave específica, garantizando que no haya condiciones de carrera
		activeWindows.compute(correlationKey, (key, state) -> {
			if (state == null) {
				// ESCENARIO A: Primera cámara en reportar
				log.info("Iniciando ventana de correlación para [{}] desde [{}]", key, palletData.getCameraId());
				PalletAggregationState newState = new PalletAggregationState(palletData);
				scheduleTimeout(key);
				return newState;
			} else {
				// ESCENARIO B: Segunda cámara reporta dentro del margen de 2 segundos
				log.info("Fusionando datos de [{}] para [{}]", palletData.getCameraId(), key);
				state.merge(palletData);
				return state;
			}
		});

		// Verificamos si con este nuevo mensaje ya podemos cerrar el palet
		evaluateAndFinalize(correlationKey, false);
	}

	/**
	 * Tarea programada que se ejecutará a los N segundos.
	 */
	private void scheduleTimeout(String correlationKey) {
		scheduler.schedule(() -> {
			evaluateAndFinalize(correlationKey, true);
		}, WINDOW_TIMEOUT_SECONDS, TimeUnit.SECONDS);
	}

	/**
	 * Evalúa si debe enviar el DTO fusionado al servicio de negocio.
	 */
	private void evaluateAndFinalize(String correlationKey, boolean isTimeout) {
		// computeIfPresent nos permite procesar y borrar de forma atómica
		activeWindows.computeIfPresent(correlationKey, (key, state) -> {
			if (state.isComplete() || isTimeout) {

				// R2: Timeout alcanzado con datos incompletos → descartar sin llamar al servicio.
				// Llamar a procesarNuevaLecturaPalet con EAN o lote nulos causaría una excepción
				// de validación predecible; es más correcto descartar aquí y alertar.
				if (isTimeout && !state.isComplete()) {
					log.warn("Motor Correlación: Timeout de {}s para [{}]. Datos incompletos — escaneo descartado. "
							+ "Cámaras reportadas: {}. EAN presente: {}, Lote presente: {}",
							WINDOW_TIMEOUT_SECONDS, key,
							state.getCamerasReported(),
							state.getMessage().getEan() != null,
							state.getMessage().getBatchNumber() != null);
					markAsFinalized(key);
					return null;
				}

				if (isTimeout) {
					log.warn("Timeout de {}s alcanzado para [{}]. Forzando finalización de fusión.",
							WINDOW_TIMEOUT_SECONDS, key);
				} else {
					log.info("Fusión completada con éxito para [{}]. Enviando a persistencia.", key);
				}

				try {
					ScanQuality quality = state.computeScanQuality();
					paletService.procesarNuevaLecturaPalet(state.getMessage(), quality);
				} catch (Exception e) {
					log.error("Error al persistir el palet fusionado [{}]: {}", key, e.getMessage());
				}

				// R1: Marcar como finalizado para que mensajes tardíos sean ignorados
				markAsFinalized(key);

				// Retornar 'null' dentro de computeIfPresent ELIMINA la clave del ConcurrentHashMap
				return null;
			}

			// Si no está completo y no hay timeout, devolvemos el estado para que siga en el mapa
			return state;
		});
	}

	/**
	 * Registra la clave como finalizada y programa su expiración tras el doble de
	 * la ventana, tiempo suficiente para absorber mensajes tardíos (R1).
	 */
	private void markAsFinalized(String correlationKey) {
		recentlyFinalized.add(correlationKey);
		scheduler.schedule(
				() -> recentlyFinalized.remove(correlationKey),
				WINDOW_TIMEOUT_SECONDS * 2L,
				TimeUnit.SECONDS);
	}

	/**
	 * Limpieza al detener la aplicación Spring Boot.
	 */
	@PreDestroy
	public void shutdown() {
		scheduler.shutdown();
	}
}
