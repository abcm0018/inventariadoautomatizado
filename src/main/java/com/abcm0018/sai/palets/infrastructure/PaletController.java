package com.abcm0018.sai.palets.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abcm0018.sai.palets.application.dtos.CreatePaletRequestDTO;
import com.abcm0018.sai.palets.application.dtos.PaletDetailResponseDTO;
import com.abcm0018.sai.palets.application.dtos.PaletResponseDTO;
import com.abcm0018.sai.palets.application.dtos.PaletSummaryResponseDTO;
import com.abcm0018.sai.palets.application.dtos.UpdatePaletRequestDTO;
import com.abcm0018.sai.palets.application.service.PaletService;
import com.abcm0018.sai.shared.response.ResponseBuilder;
import com.abcm0018.sai.shared.response.StandardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para la gestión de palets con tecnología RFID
 * <p>
 * CARACTERÍSTICAS PRINCIPALES:
 * - Registro automático de palets escaneados
 * - Check-in automático en el primer escaneo del día
 * - Caché Redis de 3 niveles para optimizar rendimiento
 * - Trazabilidad completa por lote y producto
 * - Alertas de caducidad (FIFO)
 * - Estadísticas de producción en tiempo real
 * <p>
 * CAMBIOS CON NUEVA ARQUITECTURA:
 * - Métodos consistentes con PaletService refactorizado
 * - ProductId → PackLevelId en operaciones de creación
 * - Helpers estáticos para validaciones adicionales
 */
@RestController
@RequestMapping(value = "/api/v1/palets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Palets", description = "Endpoints para gestión de palets con tecnología RFID")
public class PaletController {

	private final PaletService paletService;

	// ========== OPERACIONES CRUD ==========

	/**
	 * Crear un nuevo palet (escaneo etiqueta)
	 * <p>
	 * PROCESO AUTOMÁTICO:
	 * 1. Determina el workshift usando caché Redis (3 niveles)
	 * 2. Si es el primer escaneo del día → Check-in automático
	 * 3. Registra el palet con trazabilidad completa
	 * <p>
	 * CAMBIO: Ahora requiere packLevelId en lugar de productId
	 */
	@CrossOrigin
	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'OPERATOR')")
	@Operation(
			summary = "Registrar nuevo palet (escaneo etiqueta)",
			description = "Registra un palet escaneado. Incluye check-in automático si es el primer escaneo del día."
	)
	public StandardResponse<PaletDetailResponseDTO> createPalet(@Valid @RequestBody CreatePaletRequestDTO requestDTO) {

		log.info("Registrando nuevo palet - SSCC: {}, Usuario: {}, PackLevel: {}",
				requestDTO.getSscc(), requestDTO.getUserId(), requestDTO.getPackLevelId());

		PaletDetailResponseDTO created = paletService.createPalet(requestDTO);

		return ResponseBuilder.withCreatedElements(
				HttpStatus.CREATED,
				true,
				1,
				"Palet registrado exitosamente",
				created
		);
	}

	@CrossOrigin
	@GetMapping("/{id}")
	@Operation(summary = "Obtener palet por ID")
	public StandardResponse<PaletDetailResponseDTO> getPaletById(
			@PathVariable @Parameter(description = "ID del palet") Long id) {

		log.debug("Consultando palet con ID: {}", id);

		PaletDetailResponseDTO palet = paletService.findById(id);

		return ResponseBuilder.with(HttpStatus.OK, true, "Palet encontrado", palet);
	}

	@CrossOrigin
	@GetMapping("/sscc/{sscc}")
	@Operation(
			summary = "Obtener palet por código SSCC",
			description = "Busca un palet usando su código SSCC único (18 dígitos)"
	)
	public StandardResponse<PaletDetailResponseDTO> getPaletBySscc(
			@PathVariable @Parameter(description = "Código SSCC (18 dígitos)") String sscc) {

		log.debug("Consultando palet con SSCC: {}", sscc);

		PaletDetailResponseDTO palet = paletService.findBySscc(sscc);

		return ResponseBuilder.with(HttpStatus.OK, true, "Palet encontrado", palet);
	}

	@CrossOrigin
	@GetMapping
	@Operation(summary = "Listar todos los palets con paginación")
	public StandardResponse<Page<PaletSummaryResponseDTO>> getAllPalets(
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		log.debug("Listando palets - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());

		Page<PaletSummaryResponseDTO> palets = paletService.findAll(pageable);

		String message = String.format("Página %d de %d (Total: %d palets)",
				palets.getNumber() + 1, palets.getTotalPages(), palets.getTotalElements());

		return ResponseBuilder.with(HttpStatus.OK, true, message, palets);
	}

	@CrossOrigin
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Actualizar un palet",
			description = "Solo supervisores y administradores pueden modificar palets"
	)
	public StandardResponse<PaletDetailResponseDTO> updatePalet(
			@PathVariable Long id,
			@Valid @RequestBody UpdatePaletRequestDTO requestDTO) {

		log.info("Actualizando palet {} - SSCC: {}", id, requestDTO.getSscc());

		PaletDetailResponseDTO updated = paletService.updatePalet(id, requestDTO);

		return ResponseBuilder.withUpdatedElements(
				HttpStatus.OK,
				true,
				1,
				"Palet actualizado exitosamente",
				updated
		);
	}

	@CrossOrigin
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Eliminar un palet",
			description = "Solo administradores pueden eliminar palets"
	)
	public StandardResponse<Void> deletePalet(
			@PathVariable @Parameter(description = "ID del palet") Long id) {

		log.warn("Eliminando palet con ID: {}", id);

		paletService.deletePalet(id);

		return ResponseBuilder.withDeletedElements(
				HttpStatus.OK,
				true,
				1,
				"Palet eliminado exitosamente"
		);
	}

	// ========== BÚSQUEDAS POR PRODUCTO ==========

	@CrossOrigin
	@GetMapping("/product/{productId}")
	@Operation(summary = "Obtener todos los palets de un producto")
	public StandardResponse<List<PaletResponseDTO>> getPaletsByProduct(
			@PathVariable @Parameter(description = "ID del producto") Long productId) {

		log.debug("Consultando palets del producto: {}", productId);

		List<PaletResponseDTO> palets = paletService.findByProductId(productId);

		String message = String.format("Encontrados %d palets del producto", palets.size());
		return ResponseBuilder.with(HttpStatus.OK, true, message, palets);
	}

	@CrossOrigin
	@GetMapping("/product/{productId}/by-expiry")
	@Operation(
			summary = "Obtener palets por fecha de caducidad (FIFO)",
			description = "Retorna palets ordenados del más antiguo al más nuevo para gestión FIFO"
	)
	public StandardResponse<List<PaletResponseDTO>> getPaletsByProductOrderedByExpiry(
			@PathVariable Long productId) {

		log.debug("Consultando palets del producto {} ordenados por caducidad", productId);

		List<PaletResponseDTO> palets = paletService.findByProductIdOrderedByExpiry(productId);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				"Palets ordenados por fecha de caducidad (FIFO)",
				palets
		);
	}

	@CrossOrigin
	@GetMapping("/product/{productId}/oldest")
	@Operation(
			summary = "Obtener palets más antiguos de un producto",
			description = "Útil para priorizar despachos según FIFO"
	)
	public StandardResponse<List<PaletResponseDTO>> getOldestPalets(
			@PathVariable Long productId,
			@RequestParam(defaultValue = "10") @Parameter(description = "Cantidad de palets") int limit) {

		log.debug("Consultando {} palets más antiguos del producto {}", limit, productId);

		List<PaletResponseDTO> palets = paletService.findOldestPaletsByProductId(productId, limit);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				String.format("%d palets más antiguos obtenidos", palets.size()),
				palets
		);
	}

	@CrossOrigin
	@GetMapping("/product/{productId}/newest")
	@Operation(summary = "Obtener palets más recientes de un producto")
	public StandardResponse<List<PaletResponseDTO>> getNewestPalets(
			@PathVariable Long productId,
			@RequestParam(defaultValue = "10") int limit) {

		log.debug("Consultando {} palets más recientes del producto {}", limit, productId);

		List<PaletResponseDTO> palets = paletService.findNewestPaletsByProductId(productId, limit);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				String.format("%d palets más recientes obtenidos", palets.size()),
				palets
		);
	}

	// ========== BÚSQUEDAS POR NIVEL DE EMBALAJE ==========

	@CrossOrigin
	@GetMapping("/pack-level/{packLevelId}")
	@Operation(summary = "Obtener todos los palets de un nivel de embalaje")
	public StandardResponse<List<PaletResponseDTO>> getPaletsByPackLevel(
			@PathVariable @Parameter(description = "ID del nivel de embalaje") Long packLevelId) {

		log.debug("Consultando palets del nivel de embalaje: {}", packLevelId);

		List<PaletResponseDTO> palets = paletService.findByPackLevelId(packLevelId);

		String message = String.format("Encontrados %d palets del nivel", palets.size());
		return ResponseBuilder.with(HttpStatus.OK, true, message, palets);
	}

	@CrossOrigin
	@GetMapping("/pack-level/{packLevelId}/by-expiry")
	@Operation(
			summary = "Obtener palets del nivel ordenados por caducidad",
			description = "Gestión FIFO a nivel de embalaje"
	)
	public StandardResponse<List<PaletResponseDTO>> getPaletsByPackLevelOrderedByExpiry(
			@PathVariable Long packLevelId) {

		log.debug("Consultando palets del nivel {} ordenados por caducidad", packLevelId);

		List<PaletResponseDTO> palets = paletService.findByPackLevelIdOrderedByExpiry(packLevelId);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				"Palets ordenados por caducidad",
				palets
		);
	}

	@CrossOrigin
	@GetMapping("/gtin/{gtin}")
	@Operation(summary = "Obtener palets por GTIN del nivel de embalaje")
	public StandardResponse<List<PaletResponseDTO>> getPaletsByGtin(
			@PathVariable String gtin) {

		log.debug("Consultando palets por GTIN: {}", gtin);

		List<PaletResponseDTO> palets = paletService.findByGtin(gtin);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				String.format("Encontrados %d palets", palets.size()),
				palets
		);
	}

	// ========== BÚSQUEDAS POR LOTE ==========

	@CrossOrigin
	@GetMapping("/batch/{batchNumber}")
	@Operation(
			summary = "Obtener palets por número de lote",
			description = "Útil para trazabilidad y gestión de calidad"
	)
	public StandardResponse<List<PaletResponseDTO>> getPaletsByBatch(
			@PathVariable @Parameter(description = "Número de lote") String batchNumber) {

		log.debug("Consultando palets del lote: {}", batchNumber);

		List<PaletResponseDTO> palets = paletService.findByBatchNumber(batchNumber);

		String message = String.format("Encontrados %d palets del lote %s", palets.size(), batchNumber);
		return ResponseBuilder.with(HttpStatus.OK, true, message, palets);
	}

	@CrossOrigin
	@GetMapping("/product/{productId}/batch/{batchNumber}")
	@Operation(summary = "Obtener palets de un producto y lote específico")
	public StandardResponse<List<PaletResponseDTO>> getPaletsByProductAndBatch(
			@PathVariable Long productId,
			@PathVariable String batchNumber) {

		log.debug("Consultando palets del producto {} y lote {}", productId, batchNumber);

		List<PaletResponseDTO> palets = paletService.findByProductIdAndBatchNumber(productId, batchNumber);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				String.format("Encontrados %d palets", palets.size()),
				palets
		);
	}

	@CrossOrigin
	@GetMapping("/pack-level/{packLevelId}/batch/{batchNumber}")
	@Operation(summary = "Obtener palets de un nivel de embalaje y lote específico")
	public StandardResponse<List<PaletResponseDTO>> getPaletsByPackLevelAndBatch(
			@PathVariable Long packLevelId,
			@PathVariable String batchNumber) {

		log.debug("Consultando palets del nivel {} y lote {}", packLevelId, batchNumber);

		List<PaletResponseDTO> palets = paletService.findByPackLevelIdAndBatchNumber(packLevelId, batchNumber);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				String.format("Encontrados %d palets", palets.size()),
				palets
		);
	}

	// ========== BÚSQUEDAS POR USUARIO Y TURNO ==========

	@CrossOrigin
	@GetMapping("/user/{userId}")
	@Operation(summary = "Obtener palets escaneados por un usuario")
	public StandardResponse<List<PaletResponseDTO>> getPaletsByUser(
			@PathVariable @Parameter(description = "ID del usuario") Long userId) {

		log.debug("Consultando palets escaneados por usuario: {}", userId);

		List<PaletResponseDTO> palets = paletService.findByUserId(userId);

		String message = String.format("Usuario ha escaneado %d palets", palets.size());
		return ResponseBuilder.with(HttpStatus.OK, true, message, palets);
	}

	@CrossOrigin
	@GetMapping("/workshift/{workshiftId}")
	@Operation(summary = "Obtener palets producidos en un turno específico")
	public StandardResponse<List<PaletResponseDTO>> getPaletsByWorkshift(
			@PathVariable @Parameter(description = "ID del turno") Long workshiftId) {

		log.debug("Consultando palets del turno: {}", workshiftId);

		List<PaletResponseDTO> palets = paletService.findByWorkshiftId(workshiftId);

		String message = String.format("Encontrados %d palets del turno", palets.size());
		return ResponseBuilder.with(HttpStatus.OK, true, message, palets);
	}

	// ========== OPERACIONES DE CADUCIDAD ==========

	@CrossOrigin
	@GetMapping("/expiring-soon")
	@Operation(
			summary = "Obtener palets próximos a caducar",
			description = "Retorna palets que caducan en los próximos 7 días"
	)
	public StandardResponse<List<PaletResponseDTO>> getPaletsExpiringSoon() {

		log.debug("Consultando palets próximos a caducar");

		List<PaletResponseDTO> palets = paletService.findPaletsExpiringSoon();

		String message = palets.isEmpty()
				? "No hay palets próximos a caducar"
				: String.format("⚠️ %d palets caducan en los próximos 7 días", palets.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, palets);
	}

	@CrossOrigin
	@GetMapping("/expiring-critical")
	@Operation(
			summary = "Obtener palets con caducidad crítica",
			description = "Retorna palets que caducan en los próximos 3 días - URGENTE"
	)
	public StandardResponse<List<PaletResponseDTO>> getCriticalExpiryPalets() {

		log.debug("Consultando palets con caducidad crítica");

		List<PaletResponseDTO> palets = paletService.findCriticalExpiryPalets();

		String message = palets.isEmpty()
				? "No hay palets en estado crítico"
				: String.format("🚨 %d palets en estado crítico (caducan en 3 días)", palets.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, palets);
	}

	@CrossOrigin
	@GetMapping("/expired")
	@Operation(
			summary = "Obtener palets caducados",
			description = "Retorna palets que ya han caducado - RETIRAR DE INVENTARIO"
	)
	public StandardResponse<List<PaletResponseDTO>> getExpiredPalets() {

		log.debug("Consultando palets caducados");

		List<PaletResponseDTO> palets = paletService.findExpiredPalets();

		String message = palets.isEmpty()
				? "No hay palets caducados"
				: String.format("❌ %d palets caducados - RETIRAR INMEDIATAMENTE", palets.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, palets);
	}

	@CrossOrigin
	@GetMapping("/expiring-soon/count")
	@Operation(summary = "Contar palets próximos a caducar")
	public StandardResponse<Long> countPaletsExpiringSoon() {

		Long count = paletService.countPaletsExpiringSoon();

		String message = count == 0
				? "No hay palets próximos a caducar"
				: String.format("%d palets caducan pronto", count);

		return ResponseBuilder.with(HttpStatus.OK, true, message, count);
	}

	@CrossOrigin
	@GetMapping("/expired/count")
	@Operation(summary = "Contar palets caducados")
	public StandardResponse<Long> countExpiredPalets() {

		Long count = paletService.countExpiredPalets();

		String message = count == 0
				? "No hay palets caducados"
				: String.format("%d palets caducados", count);

		return ResponseBuilder.with(HttpStatus.OK, true, message, count);
	}

	// ========== OPERACIONES DE INVENTARIO ==========

	@CrossOrigin
	@GetMapping("/stock/total")
	@Operation(summary = "Obtener stock total de palets en inventario")
	public StandardResponse<Long> getTotalStock() {

		Long totalStock = paletService.getTotalStock();

		String message = String.format("Stock actual: %d palets", totalStock);
		return ResponseBuilder.with(HttpStatus.OK, true, message, totalStock);
	}

	@CrossOrigin
	@GetMapping("/stock/by-product")
	@Operation(
			summary = "Obtener stock agrupado por producto",
			description = "Retorna cantidad de palets por cada producto en inventario"
	)
	public StandardResponse<Map<String, Object>> getStockByProduct() {

		log.debug("Consultando stock agrupado por producto");

		Map<String, Object> stockByProduct = paletService.getStockByProduct();

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				"Stock por producto obtenido exitosamente",
				stockByProduct
		);
	}

	@CrossOrigin
	@GetMapping("/stock/by-pack-level")
	@Operation(
			summary = "Obtener stock agrupado por nivel de embalaje",
			description = "Retorna cantidad de palets por cada nivel de embalaje"
	)
	public StandardResponse<Map<String, Object>> getStockByPackLevel() {

		log.debug("Consultando stock agrupado por nivel de embalaje");

		Map<String, Object> stockByPackLevel = paletService.getStockByPackLevel();

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				"Stock por nivel de embalaje obtenido exitosamente",
				stockByPackLevel
		);
	}

	@CrossOrigin
	@GetMapping("/product/{productId}/inventory-details")
	@Operation(
			summary = "Obtener detalles completos de inventario de un producto",
			description = "Incluye stock, palets antiguos, próximos a caducar y recomendaciones"
	)
	public StandardResponse<Map<String, Object>> getProductInventoryDetails(
			@PathVariable Long productId) {

		log.debug("Consultando detalles de inventario del producto: {}", productId);

		Map<String, Object> details = paletService.getProductInventoryDetails(productId);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				"Detalles de inventario obtenidos exitosamente",
				details
		);
	}

	// ========== ESTADÍSTICAS DE PRODUCCIÓN ==========

	@CrossOrigin
	@GetMapping("/statistics/daily")
	@Operation(
			summary = "Obtener estadísticas de producción diaria",
			description = "Analiza producción por día en un rango de fechas"
	)
	public StandardResponse<Map<String, Object>> getDailyProductionStats(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.debug("Consultando estadísticas de producción del {} al {}", startDate, endDate);

		Map<String, Object> stats = paletService.getDailyProductionStats(startDate, endDate);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				"Estadísticas de producción obtenidas exitosamente",
				stats
		);
	}

	@CrossOrigin
	@GetMapping("/statistics/by-workshift")
	@Operation(summary = "Obtener producción agrupada por turno")
	public StandardResponse<Map<String, Object>> getProductionByWorkshift(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.debug("Consultando producción por turno del {} al {}", startDate, endDate);

		Map<String, Object> stats = paletService.getProductionByWorkshift(startDate, endDate);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				"Producción por turno obtenida exitosamente",
				stats
		);
	}

	@CrossOrigin
	@GetMapping("/statistics/by-user")
	@Operation(
			summary = "Obtener producción por usuario",
			description = "Muestra productividad de cada operador"
	)
	public StandardResponse<Map<String, Object>> getProductionByUser(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.debug("Consultando producción por usuario del {} al {}", startDate, endDate);

		Map<String, Object> stats = paletService.getProductionByUser(startDate, endDate);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				"Producción por usuario obtenida exitosamente",
				stats
		);
	}

	@CrossOrigin
	@GetMapping("/statistics/average-per-day")
	@Operation(summary = "Obtener promedio de palets producidos por día")
	public StandardResponse<Double> getAveragePaletsPerDay(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		Double average = paletService.getAveragePaletsPerDay(startDate, endDate);

		String message = String.format("Promedio: %.2f palets por día", average);
		return ResponseBuilder.with(HttpStatus.OK, true, message, average);
	}

	@CrossOrigin
	@GetMapping("/statistics/global")
	@Operation(
			summary = "Obtener estadísticas globales de producción",
			description = "Dashboard completo: stock, alertas, métricas de producción"
	)
	public StandardResponse<Map<String, Object>> getGlobalProductionStatistics() {

		log.debug("Consultando estadísticas globales de producción");

		Map<String, Object> stats = paletService.getGlobalProductionStatistics();

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				"Estadísticas globales obtenidas exitosamente",
				stats
		);
	}

	// ========== TRAZABILIDAD ==========

	@CrossOrigin
	@GetMapping("/traceability/sscc/{sscc}")
	@Operation(
			summary = "Obtener trazabilidad completa de un palet",
			description = "Incluye información del palet, lote, producto y palets relacionados"
	)
	public StandardResponse<Map<String, Object>> getFullTraceability(
			@PathVariable String sscc) {

		log.debug("Consultando trazabilidad completa del SSCC: {}", sscc);

		Map<String, Object> traceability = paletService.getFullTraceability(sscc);

		return ResponseBuilder.with(
				HttpStatus.OK,
				true,
				"Trazabilidad obtenida exitosamente",
				traceability
		);
	}

	// ========== BÚSQUEDAS AVANZADAS ==========

	@CrossOrigin
	@GetMapping("/search")
	@Operation(
			summary = "Búsqueda avanzada con filtros múltiples",
			description = "Permite combinar filtros: producto, nivel embalaje, usuario, turno, lote, fechas"
	)
	public StandardResponse<Page<PaletResponseDTO>> searchPaletsWithFilters(
			@RequestParam(required = false) Long productId,
			@RequestParam(required = false) Long packLevelId,
			@RequestParam(required = false) Long userId,
			@RequestParam(required = false) Long workshiftId,
			@RequestParam(required = false) String batchNumber,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		log.debug("Búsqueda avanzada - Producto: {}, PackLevel: {}, Usuario: {}, Turno: {}, Lote: {}, Fechas: {} a {}",
				productId, packLevelId, userId, workshiftId, batchNumber, startDate, endDate);

		Page<PaletResponseDTO> palets = paletService.findWithFilters(
				productId, packLevelId, userId, workshiftId, batchNumber, startDate, endDate, pageable
		);

		String message = String.format("Encontrados %d palets que cumplen los filtros", palets.getTotalElements());
		return ResponseBuilder.with(HttpStatus.OK, true, message, palets);
	}

	@CrossOrigin
	@GetMapping("/quick-search")
	@Operation(
			summary = "Búsqueda rápida por SSCC o número de lote",
			description = "Busca palets cuyo SSCC o lote contenga el término"
	)
	public StandardResponse<List<PaletSummaryResponseDTO>> quickSearch(
			@RequestParam @Parameter(description = "Término de búsqueda") String search) {

		log.debug("Búsqueda rápida: '{}'", search);

		List<PaletSummaryResponseDTO> palets = paletService.searchPalets(search);

		String message = String.format("Encontrados %d palets", palets.size());
		return ResponseBuilder.with(HttpStatus.OK, true, message, palets);
	}
}
