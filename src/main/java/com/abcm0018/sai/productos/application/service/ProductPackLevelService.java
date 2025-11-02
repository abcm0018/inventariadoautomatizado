package com.abcm0018.sai.productos.application.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.abcm0018.sai.productos.application.dtos.ProductPackLevelDetailResponseDTO;
import com.abcm0018.sai.productos.application.dtos.ProductPackLevelRequestDTO;
import com.abcm0018.sai.productos.application.dtos.ProductPackLevelResponseDTO;
import com.abcm0018.sai.productos.application.dtos.UpdateProductPackLevelDTO;
import com.abcm0018.sai.productos.domain.enums.PackingLevel;
import com.abcm0018.sai.productos.exceptions.ProductPackLevelServiceException;

/**
 * Interfaz de servicio para ProductPackLevel
 * <p>
 * Responsabilidades:
 * - CRUD completo de niveles de embalaje
 * - Validaciones de negocio (GTIN único, configuración válida, etc.)
 * - Búsquedas específicas por criterios
 * - Manejo de excepciones con mensajes descriptivos
 * <p>
 * Transaccionalidad:
 * - Escritura: @Transactional (con rollback)
 * - Lectura: @Transactional(readOnly = true)
 * <p>
 * Caché:
 * - Implementado a nivel de métodos (@Cacheable, @CacheEvict)
 * - TTL: 24 horas para datos estables
 */
public interface ProductPackLevelService {

	// ========== OPERACIONES CRUD ==========

	/**
	 * Crea un nuevo nivel de embalaje
	 * <p>
	 * Validaciones:
	 * - Producto debe existir
	 * - GTIN debe ser único
	 * - Configuración debe ser válida
	 * - No puede haber duplicados por producto+packingLevel
	 *
	 * @param requestDTO datos del nivel a crear
	 * @return DTO detallado del nivel creado
	 * @throws ProductPackLevelServiceException si hay error en creación
	 */
	ProductPackLevelDetailResponseDTO createProductPackLevel(ProductPackLevelRequestDTO requestDTO);

	/**
	 * Obtiene un nivel de embalaje por su ID
	 *
	 * @param id ID del nivel
	 * @return DTO detallado del nivel
	 * @throws ProductPackLevelServiceException si no existe
	 */
	ProductPackLevelDetailResponseDTO getProductPackLevelById(Long id);

	/**
	 * Obtiene un nivel de embalaje por su GTIN (clave única)
	 * <p>
	 * Uso: Búsqueda por escaneo RFID
	 *
	 * @param gtin código GTIN del nivel
	 * @return DTO detallado del nivel
	 * @throws ProductPackLevelServiceException si no existe
	 */
	ProductPackLevelDetailResponseDTO getProductPackLevelByGtin(String gtin);

	/**
	 * Obtiene todos los niveles con paginación
	 *
	 * @param pageable parámetros de paginación
	 * @return página de DTOs de respuesta
	 */
	Page<ProductPackLevelResponseDTO> getAllProductPackLevels(Pageable pageable);

	/**
	 * Actualiza un nivel de embalaje existente
	 * <p>
	 * Restricciones:
	 * - NO se puede actualizar GTIN
	 * - NO se puede actualizar productId
	 * - NO se puede actualizar packingLevel
	 * - Se recalculan automáticamente cálculos de negocio
	 * <p>
	 * Validaciones:
	 * - Configuración debe ser válida
	 *
	 * @param id ID del nivel a actualizar
	 * @param updateDTO datos a actualizar
	 * @return DTO detallado del nivel actualizado
	 * @throws ProductPackLevelServiceException si no existe o configuración inválida
	 */
	ProductPackLevelDetailResponseDTO updateProductPackLevel(Long id, UpdateProductPackLevelDTO updateDTO);

	/**
	 * Elimina un nivel de embalaje
	 * <p>
	 * Restricciones:
	 * - NO se puede eliminar si tiene palets asociados
	 *
	 * @param id ID del nivel a eliminar
	 * @throws ProductPackLevelServiceException si no existe o tiene palets asociados
	 */
	void deleteProductPackLevel(Long id);

	/**
	 * Obtiene todos los niveles de embalaje de un producto
	 *
	 * @param productId ID del producto
	 * @return lista de DTOs de respuesta
	 * @throws ProductPackLevelServiceException si el producto no existe
	 */
	List<ProductPackLevelResponseDTO> getProductPackLevelsByProductId(Long productId);

	/**
	 * Cuenta cuántos niveles tiene un producto
	 *
	 * @param productId ID del producto
	 * @return cantidad de niveles
	 * @throws ProductPackLevelServiceException si el producto no existe
	 */
	Long countProductPackLevelsByProductId(Long productId);

	/**
	 * Verifica si un producto tiene al menos un nivel de embalaje
	 *
	 * @param productId ID del producto
	 * @return true si tiene niveles, false en caso contrario
	 * @throws ProductPackLevelServiceException si el producto no existe
	 */
	boolean hasProductPackLevels(Long productId);

	/**
	 * Obtiene todos los niveles de un tipo específico (UNIDAD, CAJA, PALET)
	 *
	 * @param packingLevel tipo de embalaje
	 * @return lista de DTOs de respuesta
	 */
	List<ProductPackLevelResponseDTO> getProductPackLevelsByPackingLevel(PackingLevel packingLevel);

	/**
	 * Obtiene los niveles de un tipo específico para un producto concreto
	 * <p>
	 * Ejemplo: Obtener solo los palets de un producto
	 *
	 * @param productId ID del producto
	 * @param packingLevel tipo de embalaje
	 * @return lista de DTOs de respuesta
	 * @throws ProductPackLevelServiceException si el producto no existe
	 */
	List<ProductPackLevelResponseDTO> getProductPackLevelsByProductIdAndPackingLevel(Long productId, PackingLevel packingLevel);

	/**
	 * Valida si un nivel de embalaje puede ser eliminado
	 * <p>
	 * Criterios:
	 * - No debe tener palets asociados
	 *
	 * @param id ID del nivel
	 * @return true si puede ser eliminado
	 * @throws ProductPackLevelServiceException si no existe o tiene palets
	 */
	boolean canBeDeleted(Long id);

	/**
	 * Valida si un GTIN es único en el sistema
	 *
	 * @param gtin código GTIN
	 * @return true si es único (no existe)
	 */
	boolean isGtinUnique(String gtin);

	/**
	 * Valida si un GTIN es único excluyendo un ID específico
	 * Útil para validar en actualizaciones
	 *
	 * @param gtin código GTIN
	 * @param excludeId ID a excluir de la búsqueda
	 * @return true si es único
	 */
	boolean isGtinUniqueExcluding(String gtin, Long excludeId);

	/**
	 * Valida que la configuración logística del nivel sea válida
	 * <p>
	 * Criterios:
	 * - Peso positivo
	 * - Dimensiones dentro de límites
	 * - Unidades y límites válidos
	 *
	 * @param requestDTO datos a validar
	 * @return true si la configuración es válida
	 */
	boolean isValidConfiguration(ProductPackLevelRequestDTO requestDTO);

	// ========== BÚSQUEDAS POR AUDITORÍA ==========

	/**
	 * Obtiene los niveles creados recientemente
	 *
	 * @param limit cantidad de registros a retornar
	 * @return lista de DTOs de respuesta ordenados por fecha descendente
	 */
	List<ProductPackLevelResponseDTO> getRecentlyCreatedProductPackLevels(int limit);

	/**
	 * Obtiene los niveles modificados recientemente
	 *
	 * @param limit cantidad de registros a retornar
	 * @return lista de DTOs de respuesta ordenados por fecha descendente
	 */
	List<ProductPackLevelResponseDTO> getRecentlyModifiedProductPackLevels(int limit);

}
