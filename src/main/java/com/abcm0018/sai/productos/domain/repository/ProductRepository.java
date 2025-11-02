package com.abcm0018.sai.productos.domain.repository;

import com.abcm0018.sai.productos.domain.entity.Product;
import com.abcm0018.sai.productos.domain.enums.ProductStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para Product
 * <p>
 * Responsabilidad: Acceso a datos de Product únicamente
 * <p>
 * Métodos limitados al MVP:
 * - Búsqueda por identificadores únicos (GTIN, brand+formatCode)
 * - Búsqueda por atributos simples (nombre, marca, país)
 * - Búsqueda por estado del producto
 * - Búsqueda de niveles de embalaje configurados
 * - Auditoría temporal
 * <p>
 * Nota: Operaciones de inventario (palets, stock) pertenecen a PaletRepository
 * Nota: Lógica compleja y orquestación pertenecen a ProductService
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
	/**
	 * Busca un producto por marca y código de formato (constraint único)
	 * Búsqueda principal para identificar productos
	 */
	Optional<Product> findByBrandAndFormatCode(String brand, String formatCode);

	/**
	 * Busca productos por nombre (búsqueda parcial, case-insensitive)
	 */
	List<Product> findByNameContainingIgnoreCase(String name);

	/**
	 * Busca productos por marca exacta
	 */
	List<Product> findByBrand(String brand);

	/**
	 * Busca productos por código de formato exacto
	 */
	List<Product> findByFormatCode(String formatCode);

	/**
	 * Obtiene todos los productos con un estado específico
	 */
	List<Product> findByStatus(ProductStatus status);

	/**
	 * Obtiene productos con un estado específico con paginación
	 */
	Page<Product> findByStatus(ProductStatus status, Pageable pageable);

	/**
	 * Cuenta productos por estado
	 */
	Long countByStatus(ProductStatus status);

	/**
	 * Obtiene productos fabricados en un país específico
	 */
	@Query("SELECT p FROM Product p WHERE p.manufacturedIn = :country ORDER BY p.name ASC")
	List<Product> findByManufacturedIn(@Param("country") String country);

	/**
	 * Obtiene productos que tienen al menos un nivel de embalaje configurado
	 */
	@Query("SELECT p FROM Product p WHERE SIZE(p.packLevels) > 0 ORDER BY p.name ASC")
	List<Product> findProductsWithPackLevels();

	/**
	 * Obtiene productos sin niveles de embalaje configurados (potencialmente incompletos)
	 */
	@Query("SELECT p FROM Product p WHERE SIZE(p.packLevels) = 0 ORDER BY p.name ASC")
	List<Product> findProductsWithoutPackLevels();

	/**
	 * Obtiene productos creados entre dos fechas
	 */
	@Query("SELECT p FROM Product p WHERE CAST(p.createdAt AS DATE) BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
	List<Product> findProductsCreatedBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	/**
	 * Búsqueda con filtros múltiples opcionales
	 * Permite combinar: estado, marca, nombre, país de manufactura
	 *
	 * @param status estado del producto (null = ignorar)
	 * @param brand marca (búsqueda parcial, null = ignorar)
	 * @param name nombre (búsqueda parcial, null = ignorar)
	 * @param country país de manufactura (null = ignorar)
	 * @param pageable paginación y ordenamiento
	 * @return página de productos que cumplen los filtros
	 */
	@Query("SELECT p FROM Product p WHERE " +
			"(:status IS NULL OR p.status = :status) AND " +
			"(:brand IS NULL OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
			"(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
			"(:country IS NULL OR p.manufacturedIn = :country) " +
			"ORDER BY p.name ASC")
	Page<Product> findWithFilters(@Param("status") ProductStatus status, @Param("brand") String brand, @Param("name") String name, @Param("country") String country, Pageable pageable);

	boolean existsByBrandAndFormatCode(String brand, String formatCode);
}
