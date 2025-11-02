package com.abcm0018.sai.productos.infrastructure.bulk.listener;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.abcm0018.sai.productos.application.bulk.dtos.BulkImportResponseDTO;
import com.abcm0018.sai.productos.application.bulk.events.BulkImportStartedEvent;
import com.abcm0018.sai.productos.application.bulk.exceptions.ProductBulkImportException;
import com.abcm0018.sai.productos.application.bulk.job.ImportJob;
import com.abcm0018.sai.productos.application.bulk.job.ImportJobRepository;
import com.abcm0018.sai.productos.application.bulk.job.ImportJobStatus;
import com.abcm0018.sai.productos.application.bulk.service.ProductBulkImportService;
import com.abcm0018.sai.shared.constants.CustomErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkImportListener {

	private final ProductBulkImportService service;
	private final ImportJobRepository importJobRepository;
	private final ObjectMapper objectMapper;

	/**
	 * Escucha el evento de inicio de importación y procesa el trabajo
	 * de forma asíncrona.
	 * (Corresponde a la Fase 5 del PDF)
	 */
	@Async // ¡CLAVE! Esto lo ejecuta en un hilo separado
	@TransactionalEventListener // Solo se ejecuta cuando la transacción que publicó el evento se ha confirmado
	public void handlerImportEvent(BulkImportStartedEvent event) {
		UUID jobId = event.getJobId();
		ImportJob job = null;

		try {
			// 1. Actualizar el estado a PROCESSING
			job = importJobRepository.findById(jobId)
					.orElseThrow(() -> new ProductBulkImportException(CustomErrorCode.NOT_FOUND, "Job no encontrado", HttpStatus.NOT_FOUND));

			job.setStatus(ImportJobStatus.PROCESSING);
			job.setStartedAt(LocalDateTime.now());
			importJobRepository.save(job);

			log.info("[Job {}] Worker asíncrono ha recogido el trabajo.", jobId);

			// 2. Llamar al servicio para la ejecución LENTA
			BulkImportResponseDTO result = service.executorImport(jobId);

			// 3. Actualizar el estado a COMPLETE (o FAILED si el resultado es un error)
			if (result.isSuccess()) {
				job.setStatus(ImportJobStatus.COMPLETED);
			} else {
				job.setStatus(ImportJobStatus.FAILED);
			}

			job.setResultDetails(serializeResponse(result));
		} catch (Exception e) {
			log.error("[Job {}] Error fatal no controlado en el Listener: {}", jobId, e.getMessage());
			if (job != null) {
				job.setStatus(ImportJobStatus.FAILED);
				job.setResultDetails(serializeResponse(
						BulkImportResponseDTO.builder()
								.status("ERROR")
								.message("Error fatal del worker: " + e.getMessage())
								.build()
				));
			}
		} finally {
			// 4. Guardar el estado final
			if (job != null) {
				job.setFinishedAt(LocalDateTime.now());
				importJobRepository.save(job);
				log.info("[Job {}] Worker ha finalizado. Estado final: {}.", jobId, job.getStatus());
			}
		}
	}

	/**
	 * Serializa la respuesta DTO a un String JSON para guardarla en la BBDD.
	 */
	private String serializeResponse(BulkImportResponseDTO response) {
		try {
			return objectMapper.writeValueAsString(response);
		} catch (Exception e) {
			log.error("Error al serializar respuesta del Job", e);
			return "{\"status\":\"ERROR\", \"message\":\"Error serializando respuesta\"}";
		}
	}
}
