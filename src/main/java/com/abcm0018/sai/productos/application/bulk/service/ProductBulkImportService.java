package com.abcm0018.sai.productos.application.bulk.service;

import com.abcm0018.sai.productos.application.bulk.dtos.BulkImportErrorDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.BulkImportResponseDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.BulkImportResultDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ImportJobResponseDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ImportStatusResponseDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ProductBulkImportDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ProductWithPackLevelsDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ValidationResultDTO;
import com.abcm0018.sai.productos.application.bulk.enums.LoaderType;
import com.abcm0018.sai.productos.application.bulk.events.BulkImportStartedEvent;
import com.abcm0018.sai.productos.application.bulk.exceptions.ProductBulkImportException;
import com.abcm0018.sai.productos.application.bulk.exceptions.ProductBulkPersistenceException;
import com.abcm0018.sai.productos.application.bulk.job.ImportJob;
import com.abcm0018.sai.productos.application.bulk.job.ImportJobRepository;
import com.abcm0018.sai.productos.application.bulk.service.loader.ProductLoaderFactory;
import com.abcm0018.sai.productos.application.bulk.service.loader.ProductLoaderStrategy;
import com.abcm0018.sai.productos.application.bulk.mappers.ImportJobMapper;
import com.abcm0018.sai.productos.application.bulk.service.validator.ProductBulkValidationService;
import com.abcm0018.sai.productos.infrastructure.bulk.storage.FileStorageService;
import com.abcm0018.sai.shared.constants.CustomErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servicio orquestador principal para importación masiva de productos.
 * <p>
 * RESPONSABILIDADES:
 * - Coordinar el flujo completo de importación
 * - Ejecutar cada fase en orden
 * - Manejo centralizado de excepciones
 * - Invalidar cachés después de persistir
 * - Retornar respuesta completa al cliente
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductBulkImportService {

	// --- Dependencias de Fases ---
	private final ProductLoaderFactory loaderFactory;
	private final ProductBulkNormalizer normalizer;
	private final ProductBulkValidationService validationService;
	private final ProductBulkPersistenceService persistenceService;
	private final ErrorFileGeneratorService errorFileGeneratorService;
	private final ApplicationEventPublisher eventPublisher;
	private final ImportJobMapper importJobMapper;

	// --- Dependencias de Infraestructura ---
	private final FileStorageService fileStorageService;
	private final ImportJobRepository importJobRepository;

	/**
	 * FASE DE INICIO (SÍNCRONA)
	 * Valida y guarda el fichero, crea el Job y publica el evento de inicio.
	 *
	 * @param file Archivo subido
	 * @return El ID del Job creado (UUID)
	 */
	@Transactional
	public ImportJobResponseDTO initiateImportJob(MultipartFile file) {

		log.info("Iniciando trabajo de importación para: {}", file.getOriginalFilename());

		// 1. Guardar el fichero usando el service FileStorageService
		String storagePath = fileStorageService.save(file, file.getOriginalFilename());

		log.info("Archivo validado y guardado en: {}", storagePath);

		// 2. Crear la entidad Job
		ImportJob job = ImportJob.builder()
				.originalFilename(file.getOriginalFilename())
				.storagePath(storagePath)
				.loaderType(detectLoaderType(file.getOriginalFilename()))
				.userId(getAuthenticatedUserId())
				.build();

		// 3. Persistir el Job en la BBDD
		ImportJob savedJob = importJobRepository.save(job);
		UUID jobId = savedJob.getJobId();

		// 4. Publicar el evento para que el Listener asíncrono lo recoja
		eventPublisher.publishEvent(new BulkImportStartedEvent(this, jobId));

		log.info("[Job {}] Creado y evento publicado. Storage path: {}", jobId, storagePath);

		// 5. Mapear la respuesta (¡NUEVO!)
		ImportJobResponseDTO responseDTO = importJobMapper.toImportJobResponseDTO(savedJob);

		// 6. Añadir la lógica de mensaje (¡NUEVO!)
		responseDTO.setMessage(
				"Trabajo de importación iniciado. Consulte el estado en /api/v1/products/bulk/import/status/" + jobId);

		return responseDTO;
	}

	/**
	 * Función principal ejecutado por el Worker Asíncrono.
	 * Orquesta el proceso completo del pipeline ETL.
	 *
	 * @param jobId El ID del Job a procesar.
	 * @return Un DTO con el resumen del resultado (para ser guardado en el Job).
	 */
	public BulkImportResponseDTO executorImport(UUID jobId) {

		log.info("[Job {}] FASE 0: Recibido por el worker. Iniciando procesamiento.", jobId);

		// 1. Obtener Metadatos del Job
		ImportJob job = importJobRepository.findById(jobId)
				.orElseThrow(() -> new ProductBulkImportException(CustomErrorCode.NOT_FOUND, "El Job con ID: " + jobId + " no existe",
						HttpStatus.NOT_FOUND));

		String storagePath = job.getStoragePath();
		LoaderType loaderType = job.getLoaderType();

		try (InputStream fileStream = fileStorageService.loadAsInputStream(storagePath)) {

			// FASE 1: LOAD (usando la strategy correcta)
			log.info("[Job {}] FASE 1: LOAD (Tipo: {})", jobId, loaderType);
			ProductLoaderStrategy loader = loaderFactory.getLoaderStrategy(loaderType);
			List<ProductBulkImportDTO> loadedDTOs = loader.load(fileStream);
			if (loadedDTOs.isEmpty()) {
				return buildErrorResponse("El archivo no contiene datos para procesar", new ArrayList<>());
			}

			// FASE 2: NORMALIZE (normalizar cada fila)
			log.info("[Job {}] FASE 2: NORMALIZE", jobId);
			List<ProductWithPackLevelsDTO> normalizedDTOs = normalizer.normalize(loadedDTOs);

			// FASE 3: VALIDATE (Reglas de negocio)
			log.info("[Job {}] FASE 3: VALIDATE", jobId);
			ValidationResultDTO validationResult = validationService.validate(normalizedDTOs);

			List<BulkImportErrorDTO> errors = validationResult.getErrors();

			if (!errors.isEmpty()) {
				log.warn("[Job {}] Validación fallida. Se encontraron {} errores. Abortando persistencia.", jobId, errors.size());

				String errorFileId;
				try {
					log.info("[Job {}] Generando archivo de errores...", jobId);
					// 1. Generar el nombre del fichero
					errorFileId = "errores_" + jobId + ".csv";

					// 2. Crear el fichero CVS en memoria
					InputStream csvStream = errorFileGeneratorService.generateErrorCsv(errors);

					// 3. Guardarlo usando el FileStorageService
					fileStorageService.save(csvStream, errorFileId);

					log.info("[Job {}] Fichero de errores guardado como: {}", jobId, errorFileId);
				} catch (Exception e) {
					log.error("[Job {}] Imposible generar o guardar el fichero de errores: {}", jobId, e.getMessage());
					// Si falla la creación del fichero de error, no bloqueamos la respuesta.
					// Simplemente, no habrá fichero de descarga.
					errorFileId = null;
				}
				// Devolvemos el control al usuario con la lista de errores Y el ID del fichero.
				return buildErrorResponse(errors, errorFileId); // Pasamos el nuevo ID
			}

			List<ProductWithPackLevelsDTO> validProducts = validationResult.getValidProducts();

			// FASE 4: PERSIST
			log.info("[Job {}] FASE 4: PERSIST ({} productos válidos)", jobId, validProducts.size());
			if (validProducts.isEmpty()) {
				log.warn("[Job {}] No hay productos válidos para persistir.", jobId);
				return buildErrorResponse("Ningún producto pasó la validación.", errors);
			}

			// Declaramos los contadores fuera del try
			int totalProductsCreated;
			int totalPackLevelsCreated;

			try {
				// persist() devuelve BulkImportResultDTO o lanza ProductBulkPersistenceException
				BulkImportResultDTO persistenceResult = persistenceService.persist(validProducts);

				// Si llegamos aquí, la transacción fue EXITOSA (COMMIT)
				totalProductsCreated = persistenceResult.getProductsCreated();
				totalPackLevelsCreated = persistenceResult.getPackLevelsCreated();
				log.info("[Job {}] Persistencia atómica exitosa. Creados {} productos y {} niveles.", jobId, totalProductsCreated, totalPackLevelsCreated);

			} catch (ProductBulkPersistenceException e) {
				// Si llegamos aquí, @Transactional hizo ROLLBACK
				log.error("[Job {}] Falla en persistencia atómica. Se activó rollback.", jobId, e);

				// Añadimos el error de persistencia a la lista de errores (que ya podía tener errores de validación)
				errors.add(BulkImportErrorDTO.builder()
						.rowNumber(null) // Es un error transaccional, no de una fila específica
						.errorMessage("Error al guardar en base de datos, toda la operación fue revertida (Rollback). Causa: " + e.getMessage())
						.type("PERSISTENCE_ERROR")
						.build());

				// Los contadores se quedan en 0, ya que nada se guardó
				totalProductsCreated = 0;
				totalPackLevelsCreated = 0;
			}

			// --- FASE 5: CONSTRUIR RESPUESTA ---
			log.info("[Job {}] --- PROCESO COMPLETADO ---", jobId);

			if (errors.isEmpty()) {
				// Caso 1: Validación limpia Y Persistencia limpia
				return buildSuccessResponse(totalProductsCreated, totalPackLevelsCreated);
			} else {
				// Caso 2: Validación limpia PERO Persistencia falló
				return buildErrorResponse("Importación fallida. 0 productos creados.", errors);
			}
		} catch (Exception e) {
			// Captura genérica para errores fatales (ej. no se pudo leer el archivo, error de BD)
			log.error("[Job {}] !!! ERROR FATAL DURANTE LA EJECUCIÓN !!!", jobId, e);

			// Relanzamos la excepción para que el Listener (Fase 5)
			// la capture y marque el Job como FAILED.
			throw new ProductBulkImportException(CustomErrorCode.INTERNAL_SERVER_ERROR,
					"Error fatal en el pipeline de importación: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
		} finally {
			log.info("[Job {}] Limpiando archivo temporal {}...", jobId, storagePath);
			try {
				fileStorageService.delete(storagePath);
				log.info("[Job {}] Archivo temporal eliminado.", jobId);
			} catch (Exception ex) {
				// logueamos si falla el borrado, pero no relanzamos para no ocultar la excepción original
				log.error("[Job {}] Error al limpiar archivo después de la operación: {}", jobId, ex.getMessage());
			}
		}
	}

	/**
	 * Obtiene el estado y resultado de un trabajo de importación.
	 * Contiene la lógica de deserialización.
	 */
	public ImportStatusResponseDTO getImportProcessStatus(UUID jobId) {
		log.debug("Consultando estado del Job {}", jobId);

		ImportJob job = importJobRepository.findById(jobId)
				.orElseThrow(() -> new ProductBulkImportException(CustomErrorCode.NOT_FOUND,
						"Job no encontrado: " + jobId, HttpStatus.NOT_FOUND));

		// El mapper ahora se encarga de:
		// 1. Mapea campos base (id, status, dates)
		// 2. Llama a @AfterMapping y deserializa 'resultDetails'
		return importJobMapper.toImportStatusResponseDTO(job);
	}

	private LoaderType detectLoaderType(String filename) {
		if (filename == null) {
			return LoaderType.CSV; // Default
		}

		String lowerCaseFilename = filename.toLowerCase();

		if (lowerCaseFilename.endsWith(".xlsx")) {
			return LoaderType.XLSX;
		}
		// Asumir CSV por defecto para .csv o cualquier otra cosa
		return LoaderType.CSV;
	}

	private String getAuthenticatedUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()
				&& !(authentication.getPrincipal() instanceof String
				&& "anonymousUser".equals(authentication.getPrincipal()))) {

			return authentication.getName();
		}
		return "system"; // O null, si lo prefieres
	}

	/**
	 * Construye una respuesta de éxito total.
	 */
	private BulkImportResponseDTO buildSuccessResponse(Integer productsCreated, Integer packLevelsCreated) {

		String message = String.format("Importación completada exitosamente: %d productos y %d niveles de embalaje creados",
				productsCreated, packLevelsCreated);

		return BulkImportResponseDTO.builder()
				.status("SUCCESS")
				.message(message)
				.productsCreated(productsCreated)
				.packLevelsCreated(packLevelsCreated)
				.errors(new ArrayList<>())
				.timestamp(LocalDateTime.now())
				.build();
	}

	/**
	 * Construye una respuesta de error total con detalles.
	 */
	private BulkImportResponseDTO buildErrorResponse(String message, List<BulkImportErrorDTO> errors) {
		return BulkImportResponseDTO.builder()
				.status("ERROR")
				.message(message)
				.productsCreated(0)
				.packLevelsCreated(0)
				.errors(errors != null && !errors.isEmpty() ? errors : new ArrayList<>())
				.timestamp(LocalDateTime.now())
				.build();
	}

	/**
	 * Construye una respuesta de error total con detalles Y un fichero de errores.
	 */
	private BulkImportResponseDTO buildErrorResponse(List<BulkImportErrorDTO> errors, String errorFileId) {
		return BulkImportResponseDTO.builder()
				.status("ERROR")
				.message("El fichero contiene errores de validación y no se ha procesado.")
				.productsCreated(0) // Asumimos 0 si hay error
				.packLevelsCreated(0)
				.errors(!errors.isEmpty() ? errors : new ArrayList<>())
				.errorFileId(errorFileId)
				.timestamp(LocalDateTime.now())
				.build();
	}
}
