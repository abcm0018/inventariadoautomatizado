package com.abcm0018.sai.palets.domain.repository;

import com.abcm0018.sai.palets.domain.entity.Palet;
import com.abcm0018.sai.productos.domain.entity.Product;
import com.abcm0018.sai.productos.domain.entity.ProductPackLevel;
import com.abcm0018.sai.shift.domain.enums.ShiftType;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Palet}.
 * <p>
 * Las búsquedas de inventario se centran en {@code ProductPackLevel} como clave
 * primaria de consulta, accediendo al producto a través de ella. La información
 * del operador y el turno se obtiene vía {@code PalletScan} (join a {@code scans}).
 */
public interface PaletRepository extends JpaRepository<Palet, Long> {

	/**
	 * Busca un palet por su código SSCC (único)
	 */
	Optional<Palet> findBySscc(String sscc);

	/**
	 * Verifica si existe un palet con ese SSCC
	 */
	boolean existsBySscc(String sscc);

	/**
	 * Busca palets de un nivel de embalaje específico
	 * Busca principal: ProductPackLevel es la clave de búsqueda
	 */
	List<Palet> findByProductPackLevel(ProductPackLevel packLevel);

	/**
	 * Busca palets de un nivel de embalaje con paginación
	 */
	Page<Palet> findByProductPackLevel(ProductPackLevel packLevel, Pageable pageable);

	/**
	 * Busca palets por GTIN del nivel de embalaje (mediante ProductPackLevel)
	 */
	@Query("SELECT p FROM Palet p WHERE p.productPackLevel.gtin = :gtin")
	List<Palet> findByGtin(@Param("gtin") String gtin);

	/**
	 * Busca palets por ID de ProductPackLevel
	 */
	@Query("SELECT p FROM Palet p WHERE p.productPackLevel.id = :packLevelId")
	List<Palet> findByPackLevelId(@Param("packLevelId") Long packLevelId);

	/**
	 * Busca todos los palets de un producto (a través de ProductPackLevel)
	 * Útil para reportes globales de inventario
	 */
	@Query("SELECT p FROM Palet p WHERE p.productPackLevel.product.id = :productId")
	List<Palet> findByProductId(@Param("productId") Long productId);

	/**
	 * Busca palets de un producto con paginación
	 */
	@Query("SELECT p FROM Palet p WHERE p.productPackLevel.product.id = :productId")
	Page<Palet> findByProductId(@Param("productId") Long productId, Pageable pageable);

	/**
	 * Cuenta palets de un producto
	 */
	@Query("SELECT COUNT(p) FROM Palet p WHERE p.productPackLevel.product.id = :productId")
	Long countByProductId(@Param("productId") Long productId);

	/**
	 * Busca palets de un producto ordenados por fecha de caducidad (FIFO)
	 */
	@Query("SELECT p FROM Palet p WHERE p.productPackLevel.product.id = :productId ORDER BY p.packagingDateTime ASC, p.productUseByDate ASC")
	List<Palet> findByProductIdOrderByExpiry(@Param("productId") Long productId);

	/**
	 * Palets más antiguos de un producto para gestión FIFO
	 */
	@Query("SELECT p FROM Palet p WHERE p.productPackLevel.product.id = :productId ORDER BY p.packagingDateTime ASC, p.createdAt ASC")
	List<Palet> findOldestPaletsByProductId(@Param("productId") Long productId, Pageable pageable);

	/**
	 * Palets más recientes de un producto
	 */
	@Query("SELECT p FROM Palet p WHERE p.productPackLevel.product.id = :productId ORDER BY p.packagingDateTime DESC, p.createdAt DESC")
	List<Palet> findNewestPaletsByProductId(@Param("productId") Long productId, Pageable pageable);

	/**
	 * Busca palets por número de lote
	 */
	List<Palet> findByBatchNumber(String batchNumber);

	/**
	 * Busca palets por lote con paginación
	 */
	Page<Palet> findByBatchNumber(String batchNumber, Pageable pageable);

	/**
	 * Cuenta palets por lote
	 */
	Long countByBatchNumber(String batchNumber);

	/**
	 * Busca palets de un producto en un lote específico
	 */
	@Query("SELECT p FROM Palet p WHERE p.productPackLevel.product.id = :productId AND p.batchNumber = :batchNumber")
	List<Palet> findByProductIdAndBatchNumber(@Param("productId") Long productId, @Param("batchNumber") String batchNumber);

	/**
	 * Busca palets de un PackLevel en un lote específico
	 */
	@Query("SELECT p FROM Palet p WHERE p.productPackLevel.id = :packLevelId " +
			"AND p.batchNumber = :batchNumber")
	List<Palet> findByPackLevelIdAndBatchNumber(@Param("packLevelId") Long packLevelId, @Param("batchNumber") String batchNumber);

	/**
	 * Verifica si existe un lote
	 */
	boolean existsByBatchNumber(String batchNumber);

	/**
	 * Busca palets escaneados por un usuario (vía pallet_scans)
	 */
	@Query("SELECT DISTINCT p FROM Palet p JOIN p.scans s JOIN s.workshift w WHERE w.user = :user")
	List<Palet> findByUser(@Param("user") User user);

	/**
	 * Busca palets escaneados por un usuario con paginación (vía pallet_scans)
	 */
	@Query("SELECT DISTINCT p FROM Palet p JOIN p.scans s JOIN s.workshift w WHERE w.user = :user")
	Page<Palet> findByUser(@Param("user") User user, Pageable pageable);

	/**
	 * Cuenta palets escaneados por un usuario (vía pallet_scans)
	 */
	@Query("SELECT COUNT(DISTINCT p) FROM Palet p JOIN p.scans s JOIN s.workshift w WHERE w.user = :user")
	Long countByUser(@Param("user") User user);

	/**
	 * Busca palets escaneados por un usuario en un período (vía pallet_scans)
	 */
	@Query("SELECT DISTINCT p FROM Palet p JOIN p.scans s JOIN s.workshift w WHERE w.user.id = :userId AND s.scannedAt BETWEEN :startDate AND :endDate")
	List<Palet> findByUserAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

	/**
	 * Busca palets de un turno específico (vía pallet_scans)
	 */
	@Query("SELECT DISTINCT p FROM Palet p JOIN p.scans s WHERE s.workshift = :workshift")
	List<Palet> findByWorkshift(@Param("workshift") Workshift workshift);

	/**
	 * Busca palets de un turno con paginación (vía pallet_scans)
	 */
	@Query("SELECT DISTINCT p FROM Palet p JOIN p.scans s WHERE s.workshift = :workshift")
	Page<Palet> findByWorkshift(@Param("workshift") Workshift workshift, Pageable pageable);

	/**
	 * Cuenta palets producidos en un turno (vía pallet_scans)
	 */
	@Query("SELECT COUNT(DISTINCT p) FROM Palet p JOIN p.scans s WHERE s.workshift = :workshift")
	Long countByWorkshift(@Param("workshift") Workshift workshift);

	/**
	 * Busca palets por ID de turno (vía pallet_scans)
	 */
	@Query("SELECT DISTINCT p FROM Palet p JOIN p.scans s WHERE s.workshift.id = :workshiftId")
	List<Palet> findByWorkshiftId(@Param("workshiftId") Long workshiftId);

	/**
	 * Busca palets empaquetados en una fecha (coincidencia de parte DATE)
	 */
	@Query("SELECT p FROM Palet p WHERE CAST(p.packagingDateTime AS date) = :date")
	List<Palet> findByPackagingDate(@Param("date") LocalDate date);

	/**
	 * Busca palets empaquetados en un rango de fechas (comparación de parte DATE)
	 */
	@Query("SELECT p FROM Palet p WHERE CAST(p.packagingDateTime AS date) BETWEEN :startDate AND :endDate")
	List<Palet> findByPackagingDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	/**
	 * Cuenta palets empaquetados en una fecha (coincidencia de parte DATE)
	 */
	@Query("SELECT COUNT(p) FROM Palet p WHERE CAST(p.packagingDateTime AS date) = :date")
	Long countByPackagingDate(@Param("date") LocalDate date);

	/**
	 * Busca palets creados en un rango de fechas
	 */
	@Query("SELECT p FROM Palet p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
	List<Palet> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	/**
	 * Busca palets que caducan en una fecha específica
	 */
	List<Palet> findByProductUseByDate(LocalDate date);

	/**
	 * Busca palets que ya han caducado
	 */
	List<Palet> findByProductUseByDateBefore(LocalDate date);

	/**
	 * Busca palets que caduca después de una fecha
	 */
	List<Palet> findByProductUseByDateAfter(LocalDate date);

	/**
	 * Busca palets que caducan en un rango de fechas
	 */
	List<Palet> findByProductUseByDateBetween(LocalDate startDate, LocalDate endDate);

	/**
	 * Busca palets próximos a caducar (7 días o menos)
	 */
	@Query("SELECT p FROM Palet p WHERE p.productUseByDate <= :expiryDate " +
			"AND p.productUseByDate > :today " +
			"ORDER BY p.productUseByDate ASC")
	List<Palet> findPaletsExpiringSoon(@Param("expiryDate") LocalDate expiryDate,
			@Param("today") LocalDate today);

	/**
	 * Busca palets ya caducados
	 */
	@Query("SELECT p FROM Palet p WHERE p.productUseByDate < :today ORDER BY p.productUseByDate ASC")
	List<Palet> findExpiredPalets(@Param("today") LocalDate today);

	/**
	 * Cuenta palets próximos a caducar
	 */
	@Query("SELECT COUNT(p) FROM Palet p WHERE p.productUseByDate <= :expiryDate AND p.productUseByDate > :today")
	Long countPaletsExpiringSoon(@Param("expiryDate") LocalDate expiryDate,
			@Param("today") LocalDate today);

	/**
	 * Cuenta palets caducados
	 */
	@Query("SELECT COUNT(p) FROM Palet p WHERE p.productUseByDate < :today")
	Long countExpiredPalets(@Param("today") LocalDate today);

	/**
	 * Obtiene el stock actual total (todos los palets)
	 */
	@Query("SELECT COUNT(p) FROM Palet p")
	Long getTotalStock();

	/**
	 * Obtiene palets agrupados por producto con conteo
	 */
	@Query("SELECT p.productPackLevel.product, COUNT(p) FROM Palet p " +
			"GROUP BY p.productPackLevel.product.id ORDER BY COUNT(p) DESC")
	List<Object[]> getStockByProduct();

	/**
	 * Obtiene palets agrupados por PackLevel con conteo
	 */
	@Query("SELECT p.productPackLevel, COUNT(p) FROM Palet p " +
			"GROUP BY p.productPackLevel.id ORDER BY COUNT(p) DESC")
	List<Object[]> getStockByPackLevel();

	/**
	 * Cuenta palets producidos por día
	 */
	@Query("SELECT CAST(p.createdAt AS DATE), COUNT(p) FROM Palet p " +
			"WHERE p.createdAt BETWEEN :startDate AND :endDate " +
			"GROUP BY CAST(p.createdAt AS DATE) " +
			"ORDER BY CAST(p.createdAt AS DATE)")
	List<Object[]> countPaletsByDay(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	/**
	 * Cuenta palets por turno en un período (vía pallet_scans)
	 */
	@Query("SELECT s.workshift, COUNT(DISTINCT p) FROM Palet p " +
			"JOIN p.scans s " +
			"WHERE p.createdAt BETWEEN :startDate AND :endDate " +
			"GROUP BY s.workshift.id ORDER BY COUNT(DISTINCT p) DESC")
	List<Object[]> countPaletsByWorkshift(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	/**
	 * Cuenta palets por nombre de turno en un período (vía pallet_scans).
	 * Devuelve una lista de [String shiftName, Long count]
	 */
	@Query("""
		SELECT s.workshift.shift.shiftType, COUNT(DISTINCT p)
		FROM Palet p
		JOIN p.scans s
		WHERE p.createdAt BETWEEN :startDate AND :endDate
		GROUP BY s.workshift.shift.shiftType""")
	List<Object[]> countPaletsByWorkshiftName(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	/**
	 * Cuenta palets por usuario (operadores más productivos) (vía pallet_scans)
	 */
	@Query("SELECT w.user, COUNT(DISTINCT p) FROM Palet p " +
			"JOIN p.scans s JOIN s.workshift w " +
			"WHERE s.scannedAt BETWEEN :startDate AND :endDate " +
			"GROUP BY w.user.id ORDER BY COUNT(DISTINCT p) DESC")
	List<Object[]> countPaletsByUser(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	/**
	 * Cuenta palets por producto en un período
	 */
	@Query("SELECT p.productPackLevel.product, COUNT(p) FROM Palet p " +
			"WHERE p.createdAt BETWEEN :startDate AND :endDate " +
			"GROUP BY p.productPackLevel.product.id ORDER BY COUNT(p) DESC")
	List<Object[]> countPaletsByProductInPeriod(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	/**
	 * Promedio de palets por día
	 */
	@Query(value = "SELECT AVG(dailyCount) FROM " +
			"(SELECT CAST(p.created_at AS DATE), COUNT(p.id) as dailyCount FROM PALETS p " +
			"WHERE p.created_at BETWEEN :startDate AND :endDate " +
			"GROUP BY CAST(p.created_at AS DATE)) as daily",
			nativeQuery = true)
	Double getAveragePaletsPerDay(@Param("startDate") LocalDateTime startDate,
								  @Param("endDate") LocalDateTime endDate);

	/**
	 * Top N productos más producidos
	 */
	@Query("SELECT p.productPackLevel.product FROM Palet p GROUP BY p.productPackLevel.product.id ORDER BY COUNT(p) DESC")
	List<Product> findTopProducts(Pageable pageable);

	/**
	 * Top N usuarios más productivos (vía pallet_scans)
	 */
	@Query("SELECT w.user FROM Palet p JOIN p.scans s JOIN s.workshift w WHERE w.user IS NOT NULL GROUP BY w.user.id ORDER BY COUNT(DISTINCT p) DESC")
	List<User> findTopUsers(Pageable pageable);

	/**
	 * Trazabilidad completa: busca todos los palets relacionados
	 */
	@Query("SELECT p FROM Palet p WHERE " +
			"p.batchNumber = :batchNumber OR " +
			"p.productPackLevel.product.id = :productId OR " +
			"CAST(p.packagingDateTime AS date) = :packagingDate")
	List<Palet> findRelatedPalets(@Param("batchNumber") String batchNumber,
			@Param("productId") Long productId, @Param("packagingDate") LocalDate packagingDate);

	/**
	 * Historial de un producto
	 */
	@Query("SELECT p FROM Palet p WHERE p.productPackLevel.product.id = :productId ORDER BY p.createdAt DESC")
	List<Palet> findProductHistory(@Param("productId") Long productId, Pageable pageable);

	/**
	 * Palets escaneados por un usuario en una fecha (vía pallet_scans)
	 */
	@Query("SELECT DISTINCT p FROM Palet p JOIN p.scans s JOIN s.workshift w " +
			"WHERE w.user.id = :userId AND CAST(s.scannedAt AS DATE) = :date " +
			"ORDER BY s.scannedAt DESC")
	List<Palet> findByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

	/**
	 * Búsqueda con filtros múltiples
	 */
	@Query("SELECT DISTINCT p FROM Palet p " +
			"LEFT JOIN p.scans s " +
			"WHERE (:productId IS NULL OR p.productPackLevel.product.id = :productId) AND " +
			"(:packLevelId IS NULL OR p.productPackLevel.id = :packLevelId) AND " +
			"(:gtin IS NULL OR p.productPackLevel.gtin = :gtin) AND " +
			"(:batchNumber IS NULL OR p.batchNumber = :batchNumber) AND " +
			"(:brand IS NULL OR p.productPackLevel.product.brand = :brand) AND " +
			"(:userId IS NULL OR s.workshift.user.id = :userId) AND " +
			"(:workshiftId IS NULL OR s.workshift.id = :workshiftId) AND " +
			"(:shiftType IS NULL OR s.workshift.shift.shiftType = :shiftType) AND " +
			"(:startDate IS NULL OR p.createdAt >= :startDate) AND " +
			"(:endDate IS NULL OR p.createdAt <= :endDate) AND " +
			"(:startTime IS NULL OR FUNCTION('DATE_FORMAT', p.packagingDateTime, '%H:%i') >= :startTime) AND " +
			"(:endTime IS NULL OR FUNCTION('DATE_FORMAT', p.packagingDateTime, '%H:%i') <= :endTime)")
	Page<Palet> findWithFilters(@Param("productId") Long productId,
			@Param("packLevelId") Long packLevelId,
			@Param("userId") Long userId,
			@Param("workshiftId") Long workshiftId,
			@Param("shiftType") ShiftType shiftType,
			@Param("batchNumber") String batchNumber,
			@Param("brand") String brand,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate,
			@Param("gtin") String gtin,
			@Param("startTime") String startTime,
			@Param("endTime") String endTime,
			Pageable pageable);

	/**
	 * Búsqueda general por SSCC o lote
	 */
	@Query("SELECT p FROM Palet p WHERE " +
			"LOWER(p.sscc) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
			"LOWER(p.batchNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
	List<Palet> searchPalets(@Param("search") String search);

	/**
	 * Cuenta palets duplicados por SSCC (debería ser 0)
	 */
	@Query("SELECT p.sscc, COUNT(p) FROM Palet p GROUP BY p.sscc HAVING COUNT(p) > 1")
	List<Object[]> findDuplicateSSCC();

	/**
	 * Palets antiguos para archivar
	 */
	@Query("SELECT p FROM Palet p WHERE p.createdAt < :cutoffDate")
	List<Palet> findOldPaletsForArchiving(@Param("cutoffDate") LocalDateTime cutoffDate);

	/**
	 * Palets modificados recientemente
	 */
	@Query("SELECT p FROM Palet p WHERE p.updatedAt IS NOT NULL ORDER BY p.updatedAt DESC")
	List<Palet> findRecentlyModified(Pageable pageable);

	/**
	 * Carga un {@link Palet} por ID con todas las relaciones necesarias para construir
	 * un {@link com.abcm0018.sai.palets.application.dtos.PaletNotificationDTO} sin
	 * {@code LazyInitializationException}.
	 */
	@Query("SELECT DISTINCT p FROM Palet p "
			+ "LEFT JOIN FETCH p.scans sc "
			+ "LEFT JOIN FETCH sc.workshift w "
			+ "LEFT JOIN FETCH w.shift sh "
			+ "LEFT JOIN FETCH w.user wu "
			+ "LEFT JOIN FETCH sc.station st "
			+ "JOIN FETCH p.productPackLevel pl "
			+ "JOIN FETCH pl.product prod "
			+ "WHERE p.id = ?1")
	Optional<Palet> findWithDetailsById(Long id);

	/**
	 * Devuelve los palets más recientes con todas las relaciones necesarias para
	 * construir {@link com.abcm0018.sai.palets.application.dtos.PaletNotificationDTO}
	 * en una sola consulta SQL, evitando el problema N+1.
	 * <p>
	 * Al incluir una colección ({@code scans}), Hibernate aplica la paginación en
	 * memoria. El tamaño máximo del {@link Pageable} debe mantenerse pequeño (≤ 20)
	 * para que esto no sea un problema de rendimiento.
	 */
	@Query(
			value = """
					SELECT DISTINCT p FROM Palet p
					LEFT JOIN FETCH p.scans sc
					LEFT JOIN FETCH sc.workshift w
					LEFT JOIN FETCH w.shift sh
					LEFT JOIN FETCH w.user wu
					JOIN FETCH p.productPackLevel pl
					JOIN FETCH pl.product prod
					""",
			countQuery = "SELECT COUNT(DISTINCT p) FROM Palet p"
	)
	Page<Palet> findRecentPaletsWithDetails(Pageable pageable);
}

