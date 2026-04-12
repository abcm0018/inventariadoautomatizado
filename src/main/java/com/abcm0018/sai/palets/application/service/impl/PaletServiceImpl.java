package com.abcm0018.sai.palets.application.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abcm0018.sai.palets.application.dtos.CreatePaletRequestDTO;
import com.abcm0018.sai.palets.application.dtos.PaletDetailResponseDTO;
import com.abcm0018.sai.palets.application.dtos.PaletNotificationDTO;
import com.abcm0018.sai.palets.application.dtos.PaletResponseDTO;
import com.abcm0018.sai.palets.application.dtos.PaletSummaryResponseDTO;
import com.abcm0018.sai.palets.application.dtos.UpdatePaletRequestDTO;
import com.abcm0018.sai.palets.application.mappers.PaletMapper;
import com.abcm0018.sai.palets.application.service.PaletService;
import com.abcm0018.sai.palets.application.service.helpers.ExpiryHelper;
import com.abcm0018.sai.palets.application.service.helpers.InventoryHelper;
import com.abcm0018.sai.palets.application.service.helpers.StatsHelper;
import com.abcm0018.sai.palets.application.validation.PaletValidationContext;
import com.abcm0018.sai.palets.application.validation.PaletValidationService;
import com.abcm0018.sai.palets.domain.PaletCreatedEvent;
import com.abcm0018.sai.palets.domain.entity.Palet;
import com.abcm0018.sai.palets.domain.repository.PaletRepository;
import com.abcm0018.sai.palets.exceptions.PaletsServiceException;
import com.abcm0018.sai.palets.infrastructure.messaging.dtos.PaletLecturaMessageDTO;
import com.abcm0018.sai.productos.domain.entity.Product;
import com.abcm0018.sai.productos.domain.entity.ProductPackLevel;
import com.abcm0018.sai.productos.domain.repository.ProductPackLevelRepository;
import com.abcm0018.sai.shared.constants.CustomErrorCode;
import com.abcm0018.sai.shift.domain.enums.ShiftType;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetDetailResponseDTO;
import com.abcm0018.sai.timesheet.application.service.TimesheetService;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.users.domain.repository.UserRepository;
import com.abcm0018.sai.workshift.application.service.WorkshiftService;
import com.abcm0018.sai.workshift.domain.entity.Workshift;
import com.abcm0018.sai.workshift.domain.repository.WorkshiftRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de aplicación para gestión de palets
 * <p>
 * Arquitectura: Orquestador con helpers estáticos
 * <p>
 * Responsabilidades:
 * - Orquestar operaciones CRUD de palets
 * - Delegar cálculos a helpers estáticos (ExpiryHelper, InventoryHelper, StatsHelper)
 * - Gestionar caché centralizada
 * - Validar datos de entrada
 * - Coordinar con repositorios y mappers
 * <p>
 * VENTAJAS:
 * - Una sola interfaz pública (PaletService)
 * - Código organizado por dominio sin exceso de complejidad
 * - Caché funciona perfectamente (@Cacheable en métodos públicos)
 * - Helpers reutilizables en tests
 * - Sin componentes innecesarios
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaletServiceImpl implements PaletService {

	// ========== REPOSITORIOS ==========
	private final UserRepository userRepository;
	private final PaletRepository paletRepository;
	private final WorkshiftService workshiftService;
	private final WorkshiftRepository workshiftRepository;
	private final ProductPackLevelRepository productPackLevelRepository;
	private final PaletValidationService validationService;

	private final PaletMapper paletMapper;

	private final ApplicationEventPublisher eventPublisher;
	private final TimesheetService timesheetService;
	private final RedisTemplate<String, Long> workshiftRedisTemplate;

	@Value("${app.email.alerts.send-to}")
	private String supervisorEmail;

	private static final String WORKSHIFT_CACHE_KEY_PATTERN = "workshift:user:%d:date:%s";
	private static final Duration CACHE_TTL = Duration.ofHours(24);
	private final ApplicationEventPublisher applicationEventPublisher;

	/**
	 * Crea un nuevo palet con check-in automático optimizado
	 */
	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "totalStock", allEntries = true),
			@CacheEvict(value = "stockByProduct", allEntries = true),
			@CacheEvict(value = "stockByPackLevel", allEntries = true),
			@CacheEvict(value = "expiringPalets", allEntries = true),
	})
	public PaletDetailResponseDTO createPalet(CreatePaletRequestDTO request) {
		try {
			log.info("Creando nuevo palet - SSCC: {}, PackLevel: {}", request.getSscc(), request.getPackLevelId());

			validateUniqueSscc(request.getSscc());
			validateDates(request.getPackagingDate(), request.getProductUseByDate());

			// 1. Obtener workshift optimizado (Redis → BD → Check-in automático)
			Long workshiftId = getWorkshiftIdOptimized(request.getUserId());

			// 2. Crear entidad palet desde DTO
			Palet palet = paletMapper.toPalet(request);

			// 3. Cargar relaciones desde base de datos
			ProductPackLevel packLevel = getProductPackLevelById(request.getPackLevelId());
			Workshift workshift = getWorkshiftById(workshiftId);

			palet.setProductPackLevel(packLevel);
			palet.setWorkshift(workshift);

			if (request.getUserId() != null) {
				palet.setUser(getUserById(request.getUserId()));
			}

			// 4. Guardar palet
			Palet saved = paletRepository.save(palet);

			log.info("✅ Palet creado exitosamente - ID: {}, SSCC: {}, Producto: {}", saved.getId(), saved.getSscc(), packLevel.getProduct().getName());

			return paletMapper.toResponseDetail(saved);

		} catch (PaletsServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error inesperado al crear palet", e);
			throw new PaletsServiceException(CustomErrorCode.INTERNAL_SERVER_ERROR, "Error al guardar los datos del palet",
					HttpStatus.INTERNAL_SERVER_ERROR
			);
		}
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "palets", key = "#id", unless = "#result == null")
	public PaletDetailResponseDTO findById(Long id) {
		log.debug("Buscando palet por ID: {}", id);
		return paletMapper.toResponseDetail(getPaletById(id));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "paletsBySscc", key = "#sscc", unless = "#result == null")
	public PaletDetailResponseDTO findBySscc(String sscc) {
		log.debug("Buscando palet por SSCC: {}", sscc);
		return paletMapper.toResponseDetail(
				paletRepository.findBySscc(sscc).orElseThrow(() -> new PaletsServiceException(
								CustomErrorCode.NOT_FOUND, "Palet no encontrado con SSCC: " + sscc, HttpStatus.NOT_FOUND))
		);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<PaletSummaryResponseDTO> findAll(Pageable pageable) {
		log.debug("Listando palets - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
		return paletRepository.findAll(pageable).map(paletMapper::toResponseSummary);
	}

	@Override
	public Page<PaletNotificationDTO> findRecent7Palets(Pageable pageable) {
		return paletRepository.findAll(pageable).map(paletMapper::toResponsePaletNotification);
	}

	@Override
	@Transactional
	@Caching(
			put = @CachePut(value = "palets", key = "#id"),
			evict = {
					@CacheEvict(value = "paletsBySscc", allEntries = true),
					@CacheEvict(value = "totalStock", allEntries = true),
					@CacheEvict(value = "stockByProduct", allEntries = true),
					@CacheEvict(value = "stockByPackLevel", allEntries = true),
					@CacheEvict(value = "expiringPalets", allEntries = true),
					@CacheEvict(value = "paletFilters", allEntries = true)
			}
	)
	public PaletDetailResponseDTO updatePalet(Long id, UpdatePaletRequestDTO request) {
		log.info("Actualizando palet {}", id);

		Palet existingPalet = getPaletById(id);

		// Validar cambio de SSCC
		if (request.getSscc() != null && !existingPalet.getSscc().equals(request.getSscc())) {
			validateUniqueSscc(request.getSscc());
		}

		// Validar fechas si se modifican
		if (request.getPackagingDate() != null && request.getProductUseByDate() != null) {
			validateDates(request.getPackagingDate(), request.getProductUseByDate());
		}

		// Actualizar desde request
		paletMapper.updateEntityFromRequest(request, existingPalet);

		// Guardar cambios
		Palet updated = paletRepository.save(existingPalet);

		log.info("✅ Palet {} actualizado exitosamente", id);
		return paletMapper.toResponseDetail(updated);
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "palets", key = "#id"),
			@CacheEvict(value = "paletsBySscc", allEntries = true),
			@CacheEvict(value = "totalStock", allEntries = true),
			@CacheEvict(value = "stockByProduct", allEntries = true),
			@CacheEvict(value = "stockByPackLevel", allEntries = true),
			@CacheEvict(value = "expiringPalets", allEntries = true),
			@CacheEvict(value = "paletFilters", allEntries = true)
	})
	public void deletePalet(Long id) {
		log.info("Eliminando palet {}", id);

		Palet palet = getPaletById(id);
		paletRepository.delete(palet);

		log.info("✅ Palet {} eliminado exitosamente", id);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "paletsByProduct", key = "#productId")
	public List<PaletResponseDTO> findByProductId(Long productId) {
		log.debug("Buscando palets del producto ID: {}", productId);
		return paletMapper.toResponseList(paletRepository.findByProductId(productId));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "paletsByProductOrderedByExpiry", key = "#productId")
	public List<PaletResponseDTO> findByProductIdOrderedByExpiry(Long productId) {
		log.debug("Buscando palets del producto ID: {} ordenados por caducidad", productId);
		return paletMapper.toResponseList(paletRepository.findByProductIdOrderByExpiry(productId));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "paletsByProductOldest", key = "#productId")
	public List<PaletResponseDTO> findOldestPaletsByProductId(Long productId, int limit) {
		log.debug("Buscando {} palets más antiguos del producto ID: {}", limit, productId);
		return paletMapper.toResponseList(paletRepository.findOldestPaletsByProductId(productId, PageRequest.of(0, limit)));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "paletsByProductNewest", key = "#productId")
	public List<PaletResponseDTO> findNewestPaletsByProductId(Long productId, int limit) {
		log.debug("Buscando {} palets más recientes del producto ID: {}", limit, productId);
		return paletMapper.toResponseList(
				paletRepository.findNewestPaletsByProductId(productId, PageRequest.of(0, limit))
		);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "paletsByPackLevel", key = "#packLevelId")
	public List<PaletResponseDTO> findByPackLevelId(Long packLevelId) {
		log.debug("Buscando palets del nivel de embalaje ID: {}", packLevelId);
		return paletMapper.toResponseList(paletRepository.findByPackLevelId(packLevelId));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "paletsByPackLevelOrdered", key = "#packLevelId")
	public List<PaletResponseDTO> findByPackLevelIdOrderedByExpiry(Long packLevelId) {
		log.debug("Buscando palets del nivel {} ordenados por caducidad", packLevelId);

		ProductPackLevel packLevel = getProductPackLevelById(packLevelId);
		List<Palet> palets = paletRepository.findByProductPackLevel(packLevel);

		// Ordenar por caducidad (FIFO)
		palets.sort((p1, p2) -> p1.getProductUseByDate().compareTo(p2.getProductUseByDate()));

		return paletMapper.toResponseList(palets);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "paletsByGtin", key = "#gtin")
	public List<PaletResponseDTO> findByGtin(String gtin) {
		log.debug("Buscando palets por GTIN: {}", gtin);
		return paletMapper.toResponseList(paletRepository.findByGtin(gtin));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "paletsByBatch", key = "#batchNumber")
	public List<PaletResponseDTO> findByBatchNumber(String batchNumber) {
		log.debug("Buscando palets por lote: {}", batchNumber);
		return paletMapper.toResponseList(paletRepository.findByBatchNumber(batchNumber));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "paletsByProductBatch", key = "#productId + '_' + #batchNumber")
	public List<PaletResponseDTO> findByProductIdAndBatchNumber(Long productId, String batchNumber) {
		log.debug("Buscando palets - Producto: {}, Lote: {}", productId, batchNumber);
		return paletMapper.toResponseList(
				paletRepository.findByProductIdAndBatchNumber(productId, batchNumber)
		);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "paletsByPackLevelBatch", key = "#packLevelId + '_' + #batchNumber")
	public List<PaletResponseDTO> findByPackLevelIdAndBatchNumber(Long packLevelId, String batchNumber) {
		log.debug("Buscando palets - PackLevel: {}, Lote: {}", packLevelId, batchNumber);
		return paletMapper.toResponseList(
				paletRepository.findByPackLevelIdAndBatchNumber(packLevelId, batchNumber)
		);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "paletsByUser", key = "#userId")
	public List<PaletResponseDTO> findByUserId(Long userId) {
		log.debug("Buscando palets escaneados por usuario ID: {}", userId);
		User user = getUserById(userId);
		return paletMapper.toResponseList(paletRepository.findByUser(user));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "paletsByWorkshift", key = "#workshiftId")
	public List<PaletResponseDTO> findByWorkshiftId(Long workshiftId) {
		log.debug("Buscando palets del turno ID: {}", workshiftId);
		Workshift workshift = getWorkshiftById(workshiftId);
		return paletMapper.toResponseList(paletRepository.findByWorkshift(workshift));
	}


	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "expiringPalets", key = "'soon'")
	public List<PaletResponseDTO> findPaletsExpiringSoon() {
		log.debug("Buscando palets próximos a caducar");
		LocalDate threshold = ExpiryHelper.getWarningThreshold();

		List<Palet> palets = paletRepository.findPaletsExpiringSoon(threshold, LocalDate.now());

		log.info("Encontrados {} palets próximos a caducar", palets.size());
		return paletMapper.toResponseList(palets);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "expiringPalets", key = "'critical'")
	public List<PaletResponseDTO> findCriticalExpiryPalets() {
		log.debug("Buscando palets con caducidad crítica");
		LocalDate threshold = ExpiryHelper.getCriticalThreshold();

		List<Palet> palets = paletRepository.findPaletsExpiringSoon(threshold, LocalDate.now());

		log.info("Encontrados {} palets en estado crítico", palets.size());
		return paletMapper.toResponseList(palets);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "expiringPalets", key = "'expired'")
	public List<PaletResponseDTO> findExpiredPalets() {
		log.debug("Buscando palets caducados");

		List<Palet> palets = paletRepository.findExpiredPalets(LocalDate.now());

		log.info("Encontrados {} palets caducados", palets.size());
		return paletMapper.toResponseList(palets);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "expiringPaletsCount", key = "'soon'")
	public Long countPaletsExpiringSoon() {
		log.debug("Contando palets próximos a caducar");
		LocalDate threshold = ExpiryHelper.getWarningThreshold();

		Long count = paletRepository.countPaletsExpiringSoon(threshold, LocalDate.now());

		log.debug("Count de palets expirando pronto: {}", count);
		return count;
	}

	@Override
	@Transactional(readOnly = true)
	//@Cacheable(value = "expiringPaletsCount", key = "'expired'")
	public Long countExpiredPalets() {
		log.debug("Contando palets caducados");

		Long count = paletRepository.countExpiredPalets(LocalDate.now());

		// Si 'count' es nulo, devuelve explícitamente 0L (un Long),
		// no 0 (que Java podría interpretar como Integer).
		if (count == null) {
			log.debug("Conteo de caducados es nulo, devolviendo 0L");
			return 0L;
		}

		log.debug("Count de palets expirados: {}", count);
		return count;
	}

	@Override
	@Transactional(readOnly = true)
	//@Cacheable(value = "totalStock")
	public Long getTotalStock() {
		log.debug("Obteniendo stock total");

		Long totalStock = paletRepository.getTotalStock();

		log.debug("Stock total: {} palets", totalStock);
		return totalStock;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "stockByProduct")
	public Map<String, Object> getStockByProduct() {
		log.debug("Obteniendo stock agrupado por producto");

		List<Object[]> results = paletRepository.getStockByProduct();

		List<Map<String, Object>> stockList = results.stream()
				.map(result -> {
					Map<String, Object> item = new HashMap<>();
					Product product = (Product) result[0];
					Long count = (Long) result[1];

					item.put("productId", product.getId());
					item.put("productName", product.getName());
					item.put("quantity", count);

					return item;
				}).toList();

		Map<String, Object> response = new HashMap<>();
		response.put("products", stockList);
		response.put("totalProducts", stockList.size());
		response.put("totalPalets", stockList.stream()
				.mapToLong(m -> (Long) m.get("quantity"))
				.sum());

		log.info("Stock por producto: {} productos, {} palets total", stockList.size(), response.get("totalPalets"));
		return response;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "stockByPackLevel")
	public Map<String, Object> getStockByPackLevel() {
		log.debug("Obteniendo stock agrupado por nivel de embalaje");

		List<Object[]> results = paletRepository.getStockByPackLevel();

		List<Map<String, Object>> stockList = results.stream()
				.map(result -> {
					Map<String, Object> item = new HashMap<>();
					Long count = (Long) result[1];

					item.put("packLevelId", result[0]);
					item.put("quantity", count);

					return item;
				})
				.toList();

		Map<String, Object> response = new HashMap<>();
		response.put("packLevels", stockList);
		response.put("totalPackLevels", stockList.size());
		response.put("totalPalets", stockList.stream()
				.mapToLong(m -> (Long) m.get("quantity"))
				.sum());

		log.info("Stock por nivel embalaje: {} niveles, {} palets total", stockList.size(), response.get("totalPalets"));
		return response;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "productInventoryDetails", key = "#productId")
	public Map<String, Object> getProductInventoryDetails(Long productId) {
		log.debug("Obteniendo detalles de inventario del producto ID: {}", productId);

		// 1. Stock total
		Long totalStock = paletRepository.countByProductId(productId);

		// 2. Palets más antiguos (FIFO)
		List<Palet> oldestPalets = paletRepository.findOldestPaletsByProductId(productId, PageRequest.of(0, 5));

		// 3. Palets próximos a caducar
		LocalDate warningDate = ExpiryHelper.getWarningThreshold();
		List<Palet> expiringPalets = paletRepository.findByProductIdOrderByExpiry(productId)
				.stream().filter(p -> !p.getProductUseByDate().isAfter(warningDate)).limit(10).toList();

		// 4. Análisis de caducidad
		long expiredCount = expiringPalets.stream().filter(p -> ExpiryHelper.isExpired(p.getProductUseByDate())).count();

		long criticalCount = expiringPalets.stream().filter(p -> ExpiryHelper.isCritical(p.getProductUseByDate())).count();

		long warningCount = expiringPalets.stream()
				.filter(p -> ExpiryHelper.isExpiringSoon(p.getProductUseByDate()) && !ExpiryHelper.isCritical(p.getProductUseByDate()))
				.count();

		// 5. Usar InventoryHelper para construir detalles
		Map<String, Object> details = new HashMap<>();
		details.put("product", Map.of("productId", productId));
		details.put("totalStock", totalStock);
		details.put("hasStock", totalStock > 0);
		details.put("oldestPalets", paletMapper.toSummaryResponseList(oldestPalets));
		details.put("oldestPaletsCount", oldestPalets.size());
		details.put("expiringPalets", paletMapper.toResponseList(expiringPalets));
		details.put("expiringCount", expiringPalets.size());
		details.put("hasExpiredPalets", expiredCount > 0);
		details.put("hasWarnings", !expiringPalets.isEmpty());

		Map<String, Long> expiryBreakdown = new HashMap<>();
		expiryBreakdown.put("expired", expiredCount);
		expiryBreakdown.put("critical", criticalCount);
		expiryBreakdown.put("warning", warningCount);
		details.put("expiryBreakdown", expiryBreakdown);

		// Recomendaciones usando helper
		List<String> recommendations = InventoryHelper.generateInventoryRecommendations(totalStock, expiredCount, criticalCount, warningCount);
		details.put("recommendations", recommendations);

		log.info("Detalles de inventario: {} palets, {} próximos a caducar", totalStock, expiringPalets.size());
		return details;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "dailyProductionStats",
			key = "#startDate.toString() + '_' + #endDate.toString()")
	public Map<String, Object> getDailyProductionStats(LocalDate startDate, LocalDate endDate) {
		log.debug("Obteniendo estadísticas de producción del {} al {}", startDate, endDate);

		validateDateRange(startDate, endDate);

		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		List<Object[]> dailyCounts = paletRepository.countPaletsByDay(startDateTime, endDateTime);

		List<Map<String, Object>> dailyStats = new ArrayList<>();
		long totalPalets = 0;

		for (Object[] result : dailyCounts) {
			Map<String, Object> dayStat = new HashMap<>();
			LocalDate date = (LocalDate) result[0];
			Long count = (Long) result[1];

			dayStat.put("date", date);
			dayStat.put("count", count);
			dayStat.put("dayOfWeek", date.getDayOfWeek().name());

			dailyStats.add(dayStat);
			totalPalets += count;
		}

		long daysCount = dailyStats.size();
		double averagePerDay = daysCount > 0 ? (double) totalPalets / daysCount : 0.0;

		var maxDay = dailyStats.stream().max((a, b) -> Long.compare((Long) a.get("count"), (Long) b.get("count"))).orElse(null);

		var minDay = dailyStats.stream().min((a, b) -> Long.compare((Long) a.get("count"), (Long) b.get("count"))).orElse(null);

		Map<String, Object> stats = new HashMap<>();
		stats.put("startDate", startDate);
		stats.put("endDate", endDate);
		stats.put("totalPalets", totalPalets);
		stats.put("totalDays", daysCount);
		stats.put("averagePerDay", StatsHelper.calculateAverageRounded(averagePerDay));
		stats.put("dailyStats", dailyStats);
		stats.put("maxProductionDay", maxDay);
		stats.put("minProductionDay", minDay);

		log.info("Estadísticas diarias: {} palets en {} días, promedio {}", totalPalets, daysCount, StatsHelper.calculateAverageRounded(averagePerDay));

		return stats;
	}

	@Override
	@Transactional(readOnly = true)
	//@Cacheable(value = "productionByWorkshift",
	//		key = "#startDate.toString() + '_' + #endDate.toString()")
	public Map<String, Object> getProductionByWorkshift(LocalDate startDate, LocalDate endDate) {
		log.debug("Obteniendo producción por turno del {} al {}", startDate, endDate);

		validateDateRange(startDate, endDate);

		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

		List<Object[]> workshiftCounts = paletRepository.countPaletsByWorkshiftName(startDateTime, endDateTime);

		Map<String, Long> shiftCounts = new HashMap<>();
		for (Object[] row : workshiftCounts) {
			ShiftType shiftType = (ShiftType) row[0];
			String shiftName = shiftType.name();
			Long count = (Long) row[1];
			shiftCounts.put(shiftName, count);
		}

		for (ShiftType shift : ShiftType.values()) {
			shiftCounts.putIfAbsent(shift.name(), 0L);
		}

		Map<String, Object> finalResponse = new HashMap<>();
		finalResponse.put("startDate", startDate);
		finalResponse.put("endDate", endDate);
		finalResponse.put("totalPalets", shiftCounts.values().stream().mapToLong(Long::longValue).sum());

		// Convertir el Map { "MAÑANA": 100 } en el Array [ { "shift": "MAÑANA", "count": 100 } ]
		// que el frontend (paletService.js) está esperando
		List<Map<String, Object>> workshiftStats = shiftCounts.entrySet().stream()
				.map(entry -> {
					Map<String, Object> item = new HashMap<>();
					item.put("shift", entry.getKey());
					item.put("count", entry.getValue());
					return item;
				}).toList();

		finalResponse.put("workshiftStats", workshiftStats);
		log.debug("Estadísticas procesadas: {}", finalResponse);

		return finalResponse;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "productionByUser", key = "#startDate.toString() + '_' + #endDate.toString()")
	public Map<String, Object> getProductionByUser(LocalDate startDate, LocalDate endDate) {

		log.debug("Obteniendo producción por usuario del {} al {}", startDate, endDate);

		validateDateRange(startDate, endDate);

		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		List<Object[]> userCounts = paletRepository.countPaletsByUser(startDateTime, endDateTime);

		List<Map<String, Object>> userStats = new ArrayList<>();
		long totalPalets = 0;

		for (Object[] result : userCounts) {
			User user = (User) result[0];
			Long count = (Long) result[1];

			Map<String, Object> stat = new HashMap<>();
			stat.put("userId", user.getId());
			stat.put("employeeNumber", user.getEmployeeNumber());
			stat.put("fullName", user.getName() + " " + user.getSurname());
			stat.put("count", count);

			userStats.add(stat);
			totalPalets += count;
		}

		userStats.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));

		Map<String, Object> stats = new HashMap<>();
		stats.put("startDate", startDate);
		stats.put("endDate", endDate);
		stats.put("totalPalets", totalPalets);
		stats.put("totalUsers", userStats.size());
		stats.put("userStats", userStats);
		stats.put("topUsers", userStats.stream().limit(3).toList());

		log.info("Producción por usuario: {} palets de {} usuarios", totalPalets, userStats.size());

		return stats;
	}

	@Override
	@Transactional(readOnly = true)
	public Double getAveragePaletsPerDay(LocalDate startDate, LocalDate endDate) {
		validateDateRange(startDate, endDate);

		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		Double average = paletRepository.getAveragePaletsPerDay(startDateTime, endDateTime);
		return average != null ? average : 0.0;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "globalStats", key = "'current'")
	public Map<String, Object> getGlobalProductionStatistics() {
		log.debug("Obteniendo estadísticas globales de producción");

		Long totalStock = getTotalStock();
		Long expiringSoon = countPaletsExpiringSoon();
		Long expired = countExpiredPalets();

		Map<String, Object> stats = new HashMap<>();
		stats.put("totalStock", totalStock);
		stats.put("paletsExpiringSoon", expiringSoon);
		stats.put("expiredPalets", expired);

		List<Object[]> stockByProduct = paletRepository.getStockByProduct();
		stats.put("totalProducts", stockByProduct.size());

		LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
		LocalDate today = LocalDate.now();

		List<Palet> paletsThisMonth = paletRepository.findByCreatedAtBetween(startOfMonth.atStartOfDay(),
				today.atTime(23, 59, 59));

		stats.put("paletsThisMonth", paletsThisMonth.size());

		Double avgPerDay = getAveragePaletsPerDay(startOfMonth, today);
		stats.put("averagePaletsPerDay", StatsHelper.calculateAverageRounded(avgPerDay));

		boolean hasAlerts = expiringSoon > 0 || expired > 0;
		stats.put("hasAlerts", hasAlerts);
		stats.put("healthStatus", InventoryHelper.determineHealthStatus(totalStock, expiringSoon, expired));

		log.info("Estadísticas globales: {} stock, {} expirando, {} expirados", totalStock, expiringSoon, expired);

		return stats;
	}


	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "paletTraceability", key = "#sscc")
	public Map<String, Object> getFullTraceability(String sscc) {
		log.debug("Obteniendo trazabilidad completa para SSCC: {}", sscc);

		Palet palet = paletRepository.findBySscc(sscc)
				.orElseThrow(() -> new PaletsServiceException(CustomErrorCode.NOT_FOUND,
						"Palet no encontrado con SSCC: " + sscc, HttpStatus.NOT_FOUND));

		Map<String, Object> trace = new HashMap<>();

		// Información del palet
		trace.put("palet", paletMapper.toResponseDetail(palet));

		// Palets relacionados del mismo lote
		List<Palet> relatedPalets = paletRepository.findByProductIdAndBatchNumber(palet.getProductPackLevel().getProduct().getId(),
				palet.getBatchNumber());

		trace.put("relatedPalets", paletMapper.toSummaryResponseList(relatedPalets));
		trace.put("batchSize", relatedPalets.size());

		// Información de trazabilidad
		Map<String, Object> traceInfo = new HashMap<>();
		traceInfo.put("scannedAt", palet.getCreatedAt());
		traceInfo.put("packagingDate", palet.getPackagingDate());
		traceInfo.put("expiryDate", palet.getProductUseByDate());
		traceInfo.put("daysInInventory", ChronoUnit.DAYS.between(palet.getCreatedAt().toLocalDate(), LocalDate.now()));
		trace.put("traceabilityInfo", traceInfo);

		log.info("Trazabilidad obtenida - SSCC: {}, Lote: {}, {} palets relacionados", sscc, palet.getBatchNumber(), relatedPalets.size());

		return trace;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Palet> getProductHistory(Long productId, int limit) {
		log.debug("Obteniendo historial del producto ID: {} (últimos {} palets)", productId, limit);

		List<Palet> history = paletRepository.findProductHistory(productId, PageRequest.of(0, limit));

		log.debug("Historial: {} palets encontrados", history.size());
		return history;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Palet> findRelatedPalets(String batchNumber, Long productId, LocalDate packagingDate) {
		log.debug("Buscando palets relacionados - Lote: {}, Producto: {}, Fecha: {}", batchNumber, productId, packagingDate);

		List<Palet> relatedPalets = paletRepository.findRelatedPalets(batchNumber, productId, packagingDate);

		log.info("Encontrados {} palets relacionados", relatedPalets.size());
		return relatedPalets;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(
			value = "paletFilters",
			key = "T(java.util.Objects).hash(#productId, #packLevelId, #userId, #workshiftId, #shiftTypeStr, #batchNumber, #brand, #startDate, #endDate, #gtin, #startTime, #endTime, #pageable.pageNumber, #pageable.pageSize)",
			unless = "#result == null || #result.isEmpty()"
	)
	public Page<PaletResponseDTO> findWithFilters(Long productId, Long packLevelId, Long userId, Long workshiftId, String shiftTypeStr,
												  String batchNumber, String brand, LocalDate startDate, LocalDate endDate, String gtin, LocalTime startTime, LocalTime endTime, Pageable pageable) {

		log.debug("Búsqueda con filtros - Producto: {}, PackLevel: {}, Usuario: {}, Turno: {}, Descripción Turno: {},  Lote: {}, Gtin: {}, Marca: {}", productId,
				packLevelId, userId, workshiftId, shiftTypeStr, batchNumber, brand, gtin);

		validateDateRange(startDate, endDate);
		validateTimeRange(startTime, endTime);

		LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
		LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

		ShiftType shiftType = convertToShiftType(shiftTypeStr);
		String startStr = (startTime != null) ? startTime.toString() : null; // "HH:mm"
		String endStr = (endTime != null) ? endTime.toString() : null;

		Page<Palet> palets = paletRepository.findWithFilters(productId, packLevelId, userId, workshiftId, shiftType, batchNumber, brand,
				startDateTime, endDateTime, gtin, startStr, endStr, pageable);

		log.info("Búsqueda completada - {} palets encontrados", palets.getNumberOfElements());

		return palets.map(paletMapper::toResponse);
	}



	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "paletSearch", key = "#search.toLowerCase().trim()", unless = "#result == null || #result.isEmpty()")
	public List<PaletSummaryResponseDTO> searchPalets(String search) {
		log.debug("Búsqueda general: '{}'", search);

		if (StringUtils.isEmpty(search)) {
			log.warn("Término de búsqueda vacío");
			return List.of();
		}

		String searchTerm = search.toLowerCase().trim();
		List<Palet> palets = paletRepository.searchPalets(searchTerm);

		log.info("Búsqueda completada - {} palets encontrados para '{}'", palets.size(), searchTerm);

		return paletMapper.toSummaryResponseList(palets);
	}

	@Override
	@Transactional
	public void procesarNuevaLecturaPalet(PaletLecturaMessageDTO message) {

		log.info("📦 LECTURA DE PALET RECIBIDA [SSCC: {}]", message.getSscc());

		// 1. Validar y enriquecer
		// Esta única línea ejecuta las 3 reglas (SSCC, Workshift, Producto)
		// y nos devuelve el contexto con los datos listos.
		PaletValidationContext context = validationService.validateAndContextualize(message);

		// 2. Construir la entidad
		Palet paletToSave = Palet.createNewPalet(
				message.getSscc(),
				message.getBatchNumber(),
				context.getPackLevel(),
				message.getPackagingDate(),
				message.getProductUseByDate(),
				message.getProductionTime(),
				message.getScanDate(),
				context.getWorkshift().getUser(),
				context.getWorkshift()
		);

		log.info("Datos de RabbitMQ adaptados. Llamando a createPalet para SSCC: {}", message.getSscc());
		// --- 3. PERSISTIR Y PUBLICAR EVENTO ---
		Palet savedPalet = paletRepository.save(paletToSave);

		applicationEventPublisher.publishEvent(new PaletCreatedEvent(savedPalet.getId()));
	}

	/**
	 * Estrategia optimizada de 3 niveles para obtener workshift_id
	 * NIVEL 1: Redis (caché in-memory) → ~1ms
	 * NIVEL 2: Timesheet abierto (BD) → ~50ms
	 * NIVEL 3: Check-in automático → ~200ms
	 */
	private Long getWorkshiftIdOptimized(Long userId) {
		LocalDate today = LocalDate.now();
		String cacheKey = buildWorkshiftCacheKey(userId, today);

		// NIVEL 1: Redis
		Long cachedWorkshiftId = workshiftRedisTemplate.opsForValue().get(cacheKey);
		if (cachedWorkshiftId != null) {
			log.debug("[REDIS] Workshift obtenido de caché - Usuario: {}", userId);
			return cachedWorkshiftId;
		}

		// NIVEL 2: Timesheet abierto
		TimesheetDetailResponseDTO openTimesheet = timesheetService.getOpenTimesheet(userId);
		if (openTimesheet != null) {
			Long workshiftId = openTimesheet.getWorkshift().getId();
			log.debug("[BD] Timesheet abierto encontrado - Workshift: {}", workshiftId);
			cacheWorkshiftId(cacheKey, workshiftId);
			return workshiftId;
		}

		// NIVEL 3: Primer escaneo del día → Check-in automático
		log.info("[CHECK-IN] Primer escaneo del día - Usuario: {}", userId);
		try {
			TimesheetDetailResponseDTO newTimesheet = timesheetService.checkIn(userId);
			Long workshiftId = newTimesheet.getWorkshift().getId();

			log.info("✅ Check-in automático completado - Workshift: {}, Turno: {}", workshiftId, newTimesheet.getShift().getShiftType());

			cacheWorkshiftId(cacheKey, workshiftId);
			return workshiftId;

		} catch (Exception e) {
			log.error("❌ Error en check-in automático - Usuario: {}", userId, e);
			throw new PaletsServiceException(CustomErrorCode.INTERNAL_SERVER_ERROR, "Error al realizar check-in automático para el usuario", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private String buildWorkshiftCacheKey(Long userId, LocalDate date) {
		return String.format(WORKSHIFT_CACHE_KEY_PATTERN, userId, date);
	}

	private void cacheWorkshiftId(String key, Long workshiftId) {
		try {
			workshiftRedisTemplate.opsForValue().set(key, workshiftId, CACHE_TTL);
			log.debug("Workshift cacheado en Redis - TTL: 24h");
		} catch (Exception e) {
			log.warn("No se pudo cachear en Redis (continuando): {}", e.getMessage());
		}
	}

	public void invalidateWorkshiftCache(Long userId, LocalDate date) {
		String cacheKey = buildWorkshiftCacheKey(userId, date);
		Boolean deleted = workshiftRedisTemplate.delete(cacheKey);

		if (deleted) {
			log.info("Caché de workshift invalidada - Usuario: {}, Fecha: {}", userId, date);
		}
	}


	private void validateUniqueSscc(String sscc) {
		if (paletRepository.existsBySscc(sscc)) {
			throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "Ya existe un palet con SSCC: " + sscc, HttpStatus.BAD_REQUEST);
		}
	}

	private void validateDates(LocalDate packagingDate, LocalDate expiryDate) {
		if (packagingDate != null && expiryDate != null) {
			if (packagingDate.isAfter(expiryDate)) {
				throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "Fecha de empaquetado no puede ser posterior a fecha de caducidad", HttpStatus.BAD_REQUEST);
			}
		}
	}

	private void validateDateRange(LocalDate startDate, LocalDate endDate) {
		if (startDate != null && endDate != null) {
			if (startDate.isAfter(endDate)) {
				throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "Fecha inicio no puede ser posterior a fecha fin", HttpStatus.BAD_REQUEST);
			}

			long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
			if (daysBetween > 365) {
				log.warn("Rango de fechas muy amplio: {} días", daysBetween);
			}
		}
	}

	private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
		if (startTime != null && endTime != null) {
			if (startTime.isAfter(endTime)) {
				throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "Hora inicio no puede ser posterior a hora fin", HttpStatus.BAD_REQUEST);
			}
		}
	}

	private ShiftType convertToShiftType(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		for (ShiftType type : ShiftType.values()) {
			if (type.name().equalsIgnoreCase(value) || type.getDisplayName().equalsIgnoreCase(value)) {
				return type;
			}
		}

		log.warn("No se pudo mapear el valor de turno: {}", value);
		return null;
	}

	private Palet getPaletById(Long id) {
		return paletRepository.findById(id).orElseThrow(() -> new PaletsServiceException(CustomErrorCode.NOT_FOUND, "Palet no encontrado con ID: " + id, HttpStatus.NOT_FOUND));
	}

	private ProductPackLevel getProductPackLevelById(Long packLevelId) {
		return productPackLevelRepository.findById(packLevelId).orElseThrow(() -> new PaletsServiceException(CustomErrorCode.NOT_FOUND, "Nivel de embalaje no encontrado con ID: " + packLevelId, HttpStatus.NOT_FOUND));
	}

	private User getUserById(Long userId) {
		return userRepository.findById(userId).orElseThrow(() -> new PaletsServiceException(CustomErrorCode.NOT_FOUND, "Usuario no encontrado con ID: " + userId, HttpStatus.NOT_FOUND));
	}

	private Workshift getWorkshiftById(Long workshiftId) {
		return workshiftRepository.findById(workshiftId).orElseThrow(() -> new PaletsServiceException(CustomErrorCode.NOT_FOUND, "Turno no encontrado con ID: " + workshiftId, HttpStatus.NOT_FOUND));
	}
}