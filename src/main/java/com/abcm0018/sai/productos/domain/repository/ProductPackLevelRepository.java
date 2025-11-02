package com.abcm0018.sai.productos.domain.repository;

import com.abcm0018.sai.productos.domain.entity.ProductPackLevel;
import com.abcm0018.sai.productos.domain.enums.PackingLevel;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para ProductPackLevel
 * <p>
 * Responsabilidad: Acceso a datos de niveles de embalaje
 * <p>
 * Métodos limitados al MVP:
 * - Búsqueda por identificadores únicos (GTIN)
 * - Búsqueda por producto
 * - Búsqueda por tipo de embalaje
 * - Validaciones de unicidad
 * <p>
 * Nota: Se evitan métodos complejos que puedan requerir joins
 * con Palet o estadísticas de inventario (responsabilidad de PaletRepository)
 */
public interface ProductPackLevelRepository extends JpaRepository<ProductPackLevel, Long> {

	// ========== BÚSQUEDAS POR IDENTIFICADORES ÚNICOS ==========

	/**
	 * Busca un nivel de embalaje por su GTIN
	 * El GTIN es único en todo el sistema (constraint en BD)
	 *
	 * @param gtin código de identificación logística (GTIN-14 o EAN-13)
	 * @return Optional con el ProductPackLevel si existe
	 */
	@Cacheable(value = "product_pack_levels", key = "#gtin")
	Optional<ProductPackLevel> findByGtin(String gtin);

	/**
	 * Verifica si existe un nivel con el GTIN especificado
	 *
	 * @param gtin código GTIN
	 * @return true si existe, false en caso contrario
	 */
	boolean existsByGtin(String gtin);

	// ========== BÚSQUEDAS POR PRODUCTO ==========

	/**
	 * Obtiene todos los niveles de embalaje de un producto
	 * <p>
	 * Casos de uso:
	 * - Mostrar variantes de embalaje de un producto
	 * - Validar que un producto tiene al menos un nivel
	 * - Listar opciones de empaquetado disponibles
	 *
	 * @param productId ID del producto
	 * @return lista de ProductPackLevel del producto
	 */
	@Query("SELECT ppl FROM ProductPackLevel ppl WHERE ppl.product.id = :productId ORDER BY ppl.packingLevel")
	List<ProductPackLevel> findByProductId(@Param("productId") Long productId);

	/**
	 * Cuenta cuántos niveles de embalaje tiene un producto
	 *
	 * @param productId ID del producto
	 * @return cantidad de niveles
	 */
	@Query("SELECT COUNT(ppl) FROM ProductPackLevel ppl WHERE ppl.product.id = :productId")
	Long countByProductId(@Param("productId") Long productId);

	/**
	 * Verifica si un producto tiene al menos un nivel de embalaje
	 *
	 * @param productId ID del producto
	 * @return true si tiene niveles, false si no tiene
	 */
	@Query("SELECT CASE WHEN COUNT(ppl) > 0 THEN true ELSE false END FROM ProductPackLevel ppl WHERE ppl.product.id = :productId")
	boolean hasPackLevels(@Param("productId") Long productId);

	// ========== BÚSQUEDAS POR TIPO DE EMBALAJE ==========

	/**
	 * Obtiene todos los niveles de un tipo específico (UNIDAD, CAJA, PALET)
	 *
	 * Casos de uso:
	 * - Listar todos los palets disponibles en catálogo
	 * - Reportes por tipo de embalaje
	 *
	 * @param packingLevel tipo de embalaje (UNIDAD, CAJA, PALET)
	 * @return lista de ProductPackLevel del tipo especificado
	 */
	List<ProductPackLevel> findByPackingLevel(PackingLevel packingLevel);

	/**
	 * Obtiene niveles de un tipo específico para un producto
	 *
	 * Casos de uso:
	 * - Obtener solo los palets de un producto
	 * - Validar que un producto tiene presentación en CAJA
	 *
	 * @param productId ID del producto
	 * @param packingLevel tipo de embalaje
	 * @return lista de ProductPackLevel filtrados
	 */
	@Query("SELECT ppl FROM ProductPackLevel ppl WHERE ppl.product.id = :productId AND ppl.packingLevel = :packingLevel")
	List<ProductPackLevel> findByProductIdAndPackingLevel(@Param("productId") Long productId, @Param("packingLevel") PackingLevel packingLevel);

	// ========== BÚSQUEDAS POR CRITERIOS RECIENTES ==========

	/**
	 * Obtiene los niveles creados recientemente (últimos N registros)
	 * Útil para ver cambios recientes en configuración logística
	 *
	 * @param limit cantidad de registros a retornar
	 * @return lista de ProductPackLevel ordenados por fecha de creación descendente
	 */
	@Query("SELECT ppl FROM ProductPackLevel ppl ORDER BY ppl.createdAt DESC")
	List<ProductPackLevel> findRecentlyCreated(@Param("limit") int limit);

	/**
	 * Obtiene los niveles modificados recientemente
	 * Útil para auditoría y debug
	 *
	 * @param limit cantidad de registros a retornar
	 * @return lista de ProductPackLevel con UPDATED_AT no null, ordenados descendentemente
	 */
	@Query("SELECT ppl FROM ProductPackLevel ppl WHERE ppl.updatedAt IS NOT NULL ORDER BY ppl.updatedAt DESC")
	List<ProductPackLevel> findRecentlyModified(@Param("limit") int limit);

}
