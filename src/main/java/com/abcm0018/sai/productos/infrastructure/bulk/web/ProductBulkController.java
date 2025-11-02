package com.abcm0018.sai.productos.infrastructure.bulk.web;

import com.abcm0018.sai.productos.application.bulk.service.ProductBulkImportService;
import com.abcm0018.sai.shared.response.ResponseBuilder;
import com.abcm0018.sai.shared.response.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.abcm0018.sai.productos.application.bulk.dtos.ImportJobResponseDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ImportStatusResponseDTO;
import com.abcm0018.sai.productos.application.bulk.job.ImportJobRepository;
import com.abcm0018.sai.productos.infrastructure.bulk.storage.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/products/bulk")
@RequiredArgsConstructor
@Tag(name = "Products - Bulk Import", description = "Endpoints para importación masiva de productos")
public class ProductBulkController {

	private final ProductBulkImportService bulkImportService;
	private final ImportJobRepository jobRepository;
	private final FileStorageService fileStorageService;
	private final ObjectMapper objectMapper;

	/**
	 * Inicia un trabajo de importación masiva asíncrona.
	 * La respuesta es inmediata y devuelve un StandardResponse (HTTP 202).
	 */
	@CrossOrigin
	@PostMapping("/import")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Iniciar importación masiva asíncrona")
	public StandardResponse<ImportJobResponseDTO> importProducts(
			@RequestParam(value = "file") @Parameter(description = "Archivo CSV/XLSX con productos")
			@NotNull(message = "El archivo es requerido") MultipartFile file) {

		log.info("📥 Recibida solicitud de importación: {}", file.getOriginalFilename());

		// 1. Llamar al servicio (ahora devuelve el DTO)
		ImportJobResponseDTO jobResponse = bulkImportService.initiateImportJob(file);

		// 2. Devolver StandardResponse con HTTP 202
		return ResponseBuilder.with(HttpStatus.ACCEPTED, true, "Importación iniciada", jobResponse);
	}

	/**
	 * Consulta el estado de un trabajo de importación.
	 */
	@CrossOrigin
	@GetMapping("/import/status/{jobId}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Consultar estado de importación (Polling)")
	public StandardResponse<ImportStatusResponseDTO> getImportStatus(@PathVariable UUID jobId) {

		log.debug("Consultando estado del Job {}", jobId);

		// 1. Llamar al servicio (ahora devuelve el DTO)
		ImportStatusResponseDTO statusResponse = bulkImportService.getImportProcessStatus(jobId);

		// Devolver StandardResponse con HTTP 200 (OK)
		return ResponseBuilder.with(HttpStatus.OK, true, "Respuesta satisfactoria", statusResponse);
	}

	/**
	 * Endpoint para descargar un fichero de errores de importación.
	 * (Excepción: Debe devolver ResponseEntity para forzar la descarga)
	 */
	@CrossOrigin
	@GetMapping("/errors/{filename:.+}")
	@Operation(summary = "Descargar fichero de errores")
	public ResponseEntity<Resource> downloadErrorFile(@PathVariable String filename) {
		try {
			// Asumo que tu FileStorageService tiene 'loadAsResource'
			Resource resource = (Resource) fileStorageService.loadAsInputStream(filename);

			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");
			headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8");

			return ResponseEntity.ok()
					.headers(headers)
					.body(resource);

		} catch (Exception e) {
			log.warn("Intento de descarga de fichero de error no encontrado: {}", filename, e);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}
}
