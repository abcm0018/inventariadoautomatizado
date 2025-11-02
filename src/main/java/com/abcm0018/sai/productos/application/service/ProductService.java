package com.abcm0018.sai.productos.application.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.abcm0018.sai.productos.application.dtos.ProductDetailResponseDTO;
import com.abcm0018.sai.productos.application.dtos.ProductRequestDTO;
import com.abcm0018.sai.productos.application.dtos.ProductResponseDTO;
import com.abcm0018.sai.productos.application.dtos.UpdateProductRequestDTO;
import com.abcm0018.sai.productos.domain.enums.ProductStatus;
import com.abcm0018.sai.productos.exceptions.ProductServiceException;

/**
 * Interfaz de servicio para Product
 * <p>
 * Responsabilidades:
 * - CRUD completo de productos
 * - Validaciones de negocio
 * - Búsquedas específicas por criterios
 * - Manejo de excepciones con ProductServiceException
 * <p>
 * Transaccionalidad:
 * - Escritura: @Transactional (con rollback)
 * - Lectura: @Transactional(readOnly = true)
 * <p>
 * Caché:
 * - Implementado a nivel de métodos (@Cacheable, @CacheEvict)
 * - TTL: 24 horas para datos estables
 */
public interface ProductService {

	/**
	 * Crea un nuevo producto
	 * <p>
	 * Validaciones:
	 * - Brand + formatCode debe ser único
	 * - Todos los campos obligatorios presentes
	 *
	 * @param requestDTO datos del producto a crear
	 * @return DTO detallado del producto creado
	 * @throws ProductServiceException si brand+formatCode ya existe o datos inválidos
	 */
	ProductDetailResponseDTO createProduct(ProductRequestDTO requestDTO);

	/**
	 * Obtiene un producto por su ID
	 *
	 * @param id ID del producto
	 * @return DTO detallado del producto
	 * @throws ProductServiceException si no existe
	 */
	ProductDetailResponseDTO getProductById(Long id);

	/**
	 * Obtiene todos los productos con paginación
	 *
	 * @param pageable parámetros de paginación
	 * @return página de DTOs de respuesta
	 */
	Page<ProductResponseDTO> getAllProducts(Pageable pageable);

	/**
	 * Actualiza un producto existente
	 * <p>
	 * Restricciones:
	 * - NO se puede actualizar: name, brand, formatCode, manufacturedIn
	 * - Se pueden actualizar: description, status
	 *
	 * @param id ID del producto a actualizar
	 * @param updateDTO datos a actualizar
	 * @return DTO detallado del producto actualizado
	 * @throws ProductServiceException si no existe o datos inválidos
	 */
	ProductDetailResponseDTO updateProduct(Long id, UpdateProductRequestDTO updateDTO);

	/**
	 * Elimina un producto
	 * <p>
	 * Restricción: No se puede eliminar si tiene ProductPackLevel asociados
	 *
	 * @param id ID del producto a eliminar
	 * @throws ProductServiceException si no existe o tiene niveles asociados
	 */
	void deleteProduct(Long id);

	// ========== BÚSQUEDAS POR IDENTIFICADORES ==========

	/**
	 * Busca un producto por marca y código de formato
	 * Búsqueda principal: utiliza constraint único
	 *
	 * @param brand marca del producto
	 * @param formatCode código de formato
	 * @return DTO detallado si existe
	 * @throws ProductServiceException si no existe
	 */
	ProductDetailResponseDTO getProductByBrandAndFormatCode(String brand, String formatCode);

	/**
	 * Busca productos por nombre (búsqueda parcial)
	 *
	 * @param name término de búsqueda (case-insensitive)
	 * @return lista de DTOs de respuesta
	 */
	List<ProductResponseDTO> getProductsByName(String name);

	/**
	 * Busca productos por marca exacta
	 *
	 * @param brand marca a buscar
	 * @return lista de DTOs de respuesta
	 */
	List<ProductResponseDTO> getProductsByBrand(String brand);

	/**
	 * Busca productos por código de formato
	 *
	 * @param formatCode código de formato
	 * @return lista de DTOs de respuesta
	 */
	List<ProductResponseDTO> getProductsByFormatCode(String formatCode);

	/**
	 * Obtiene productos con estado específico
	 *
	 * @param status estado del producto
	 * @return lista de DTOs de respuesta
	 */
	List<ProductResponseDTO> getProductsByStatus(ProductStatus status);

	/**
	 * Cuenta productos por estado
	 *
	 * @param status estado a contar
	 * @return cantidad de productos
	 */
	Long countByStatus(ProductStatus status);

	/**
	 * Obtiene productos fabricados en un país específico
	 *
	 * @param country país de manufactura
	 * @return lista de DTOs de respuesta
	 */
	List<ProductResponseDTO> getProductsByManufacturedIn(String country);

	/**
	 * Obtiene productos que tienen niveles de embalaje configurados
	 * Productos completos, listos para producción
	 *
	 * @return lista de DTOs de respuesta
	 */
	List<ProductResponseDTO> getProductsWithPackLevels();

	/**
	 * Obtiene productos SIN niveles de embalaje configurados
	 * Productos incompletos, requieren configuración
	 *
	 * @return lista de DTOs de respuesta
	 */
	List<ProductResponseDTO> getProductsWithoutPackLevels();

	/**
	 * Verifica si existe un producto con marca y formato específicos
	 *
	 * @param brand marca del producto
	 * @param formatCode código de formato
	 * @return true si existe
	 */
	boolean existsByBrandAndFormatCode(String brand, String formatCode);

	/**
	 * Verifica si existe un producto con marca y formato, excluyendo un ID
	 * Útil para validar en actualizaciones (evitar duplicados)
	 *
	 * @param brand marca del producto
	 * @param formatCode código de formato
	 * @param excludeId ID a excluir de la búsqueda
	 * @return true si existe otro producto con esos datos
	 */
	boolean existsByBrandAndFormatCodeExcluding(String brand, String formatCode, Long excludeId);

	/**
	 * Valida si un producto puede ser eliminado
	 * <p>
	 * Criterios:
	 * - NO debe tener ProductPackLevel asociados
	 *
	 * @param id ID del producto
	 * @return true si puede ser eliminado
	 * @throws ProductServiceException si no existe o no puede ser eliminado
	 */
	boolean canBeDeleted(Long id);

	/**
	 * Obtiene productos creados entre dos fechas
	 * Útil para auditoría y análisis histórico
	 *
	 * @param startDate fecha de inicio (inclusive)
	 * @param endDate fecha de fin (inclusive)
	 * @return lista de DTOs de respuesta
	 */
	List<ProductResponseDTO> getProductsCreatedBetween(LocalDate startDate, LocalDate endDate);

	/**
	 * Búsqueda con filtros múltiples opcionales
	 * Permite combinar: estado, marca, nombre, país de manufactura
	 *
	 * @param status estado del producto (null = ignorar)
	 * @param brand marca (búsqueda parcial, null = ignorar)
	 * @param name nombre (búsqueda parcial, null = ignorar)
	 * @param country país de manufactura (null = ignorar)
	 * @param pageable paginación y ordenamiento
	 * @return página de DTOs de respuesta que cumplen los filtros
	 */
	Page<ProductResponseDTO> findWithFilters(ProductStatus status, String brand, String name, String country, Pageable pageable);

}
