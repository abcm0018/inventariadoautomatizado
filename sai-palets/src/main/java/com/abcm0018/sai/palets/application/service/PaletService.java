package com.abcm0018.sai.palets.application.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.abcm0018.sai.palets.application.dtos.CreatePaletRequestDTO;
import com.abcm0018.sai.palets.application.dtos.PaletDetailResponseDTO;
import com.abcm0018.sai.palets.application.dtos.PaletNotificationDTO;
import com.abcm0018.sai.palets.application.dtos.PaletResponseDTO;
import com.abcm0018.sai.palets.application.dtos.PaletSummaryResponseDTO;
import com.abcm0018.sai.palets.application.dtos.UpdatePaletRequestDTO;
import com.abcm0018.sai.palets.domain.entity.Palet;
import com.abcm0018.sai.palets.domain.enums.ScanQuality;
import com.abcm0018.sai.palets.infrastructure.messaging.dtos.PaletLecturaMessageDTO;
import org.springframework.transaction.annotation.Transactional;

public interface PaletService {

	// ========== OPERACIONES CRUD ==========

	PaletDetailResponseDTO createPalet(CreatePaletRequestDTO requestDTO);
	PaletDetailResponseDTO findById(Long id);
	PaletDetailResponseDTO findBySscc(String sscc);
	Page<PaletSummaryResponseDTO> findAll(Pageable pageable);
	Page<PaletNotificationDTO> findRecent7Palets(Pageable pageable);

	List<PaletNotificationDTO> findRecentPaletsInCurrentShift();

	Map<String, Long> getKpisCurrentShift();
	PaletDetailResponseDTO updatePalet(Long id, UpdatePaletRequestDTO request);
	void deletePalet(Long id);

	// ========== BÚSQUEDAS POR PRODUCTO ==========

	List<PaletResponseDTO> findByProductId(Long productId);
	List<PaletResponseDTO> findByProductIdOrderedByExpiry(Long productId);
	List<PaletResponseDTO> findOldestPaletsByProductId(Long productId, int limit);
	List<PaletResponseDTO> findNewestPaletsByProductId(Long productId, int limit);

	// ========== BÚSQUEDAS POR NIVEL DE EMBALAJE (PRIMARY) ==========

	List<PaletResponseDTO> findByPackLevelId(Long packLevelId);
	List<PaletResponseDTO> findByPackLevelIdOrderedByExpiry(Long packLevelId);
	List<PaletResponseDTO> findByGtin(String gtin);

	// ========== BÚSQUEDAS POR LOTE ==========

	List<PaletResponseDTO> findByBatchNumber(String batchNumber);
	List<PaletResponseDTO> findByProductIdAndBatchNumber(Long productId, String batchNumber);
	List<PaletResponseDTO> findByPackLevelIdAndBatchNumber(Long packLevelId, String batchNumber);

	// ========== BÚSQUEDAS POR USUARIO Y TURNO ==========

	List<PaletResponseDTO> findByUserId(Long userId);
	List<PaletResponseDTO> findByWorkshiftId(Long workshiftId);

	// ========== OPERACIONES DE CADUCIDAD ==========

	List<PaletResponseDTO> findPaletsExpiringSoon();
	List<PaletResponseDTO> findCriticalExpiryPalets();
	List<PaletResponseDTO> findExpiredPalets();
	Long countPaletsExpiringSoon();
	Long countExpiredPalets();

	// ========== OPERACIONES DE INVENTARIO ==========

	Long getTotalStock();
	Map<String, Object> getStockByProduct();
	Map<String, Object> getStockByPackLevel();
	Map<String, Object> getProductInventoryDetails(Long productId);

	// ========== ESTADÍSTICAS DE PRODUCCIÓN ==========

	Map<String, Object> getDailyProductionStats(LocalDate startDate, LocalDate endDate);
	Map<String, Object> getProductionByWorkshift(LocalDate startDate, LocalDate endDate);
	Map<String, Object> getProductionByUser(LocalDate startDate, LocalDate endDate);
	Double getAveragePaletsPerDay(LocalDate startDate, LocalDate endDate);
	Map<String, Object> getGlobalProductionStatistics();

	// ========== TRAZABILIDAD ==========

	Map<String, Object> getFullTraceability(String sscc);
	List<Palet> getProductHistory(Long productId, int limit);
	List<Palet> findRelatedPalets(String batchNumber, Long productId, LocalDate packagingDate);

	// ========== BÚSQUEDAS AVANZADAS ==========

	@Transactional(readOnly = true)
	@Cacheable(
			value = "paletFilters",
			key = "T(java.util.Objects).hash(#productId, #packLevelId, #userId, #workshiftId, #shiftTypeStr, #batchNumber, #brand, #startDate, #endDate, #gtin, #startTime, #endTime, #pageable.pageNumber, #pageable.pageSize)",
			unless = "#result == null || #result.isEmpty()"
	)
	Page<PaletResponseDTO> findWithFilters(Long productId, Long packLevelId, Long userId, Long workshiftId, String shiftTypeStr,
										   String batchNumber, String brand, LocalDate startDate, LocalDate endDate, String gtin, LocalTime startTime, LocalTime endTime, Pageable pageable);

	List<PaletSummaryResponseDTO> searchPalets(String search);

	void procesarNuevaLecturaPalet(PaletLecturaMessageDTO message, ScanQuality scanQuality);
}
