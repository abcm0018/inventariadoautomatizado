package com.abcm0018.sai.productos.application.service.impl;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abcm0018.sai.productos.application.dtos.ProductPackLevelDetailResponseDTO;
import com.abcm0018.sai.productos.application.dtos.ProductPackLevelRequestDTO;
import com.abcm0018.sai.productos.application.dtos.ProductPackLevelResponseDTO;
import com.abcm0018.sai.productos.application.dtos.UpdateProductPackLevelDTO;
import com.abcm0018.sai.productos.application.mappers.ProductPackLevelMapper;
import com.abcm0018.sai.productos.application.service.ProductPackLevelService;
import com.abcm0018.sai.productos.domain.entity.Product;
import com.abcm0018.sai.productos.domain.entity.ProductPackLevel;
import com.abcm0018.sai.productos.domain.enums.PackingLevel;
import com.abcm0018.sai.productos.domain.repository.ProductPackLevelRepository;
import com.abcm0018.sai.productos.domain.repository.ProductRepository;
import com.abcm0018.sai.productos.exceptions.ProductPackLevelServiceException;
import com.abcm0018.sai.shared.constants.CustomErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio ProductPackLevel
 * <p>
 * Responsabilidades:
 * - Orquestación de operaciones CRUD
 * - Validaciones de negocio
 * - Manejo de excepciones
 * - Caché centralizado
 * - Logging de operaciones
 * <p>
 * Principios SOLID:
 * - S: Solo responsable de ProductPackLevel
 * - O: Abierto para extensión (interfaz clara)
 * - L: Sustitución de Liskov (implementa contrato)
 * - I: Segregación de interfaces (métodos específicos)
 * - D: Inyección de dependencias (repositories y mappers)
 * <p>
 * DRY:
 * - Métodos helpers reutilizables
 * - Validaciones centralizadas
 * - Caché invalidación consistente
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPackLevelServiceImpl implements ProductPackLevelService {

	private final ProductPackLevelRepository productPackLevelRepository;
	private final ProductRepository productRepository;
	private final ProductPackLevelMapper mapper;

	private static final String CACHE_NAME = "productPackLevels";
	private static final String CACHE_BY_GTIN = "productPackLevelsByGtin";
	private static final String CACHE_BY_PRODUCT = "productPackLevelsByProduct";

	@Override
	@Transactional
	@Caching(evict = { @CacheEvict(value = CACHE_NAME, allEntries = true), @CacheEvict(value = CACHE_BY_PRODUCT, allEntries = true) })
	public ProductPackLevelDetailResponseDTO createProductPackLevel(ProductPackLevelRequestDTO requestDTO) {
		log.info("Iniciando creación de ProductPackLevel - GTIN: {}, Producto: {}", requestDTO.getGtin(), requestDTO.getProductId());

		try {
			// 1. Validar que el producto existe
			Product product = getProductOrThrow(requestDTO.getProductId());

			// 2. Validar GTIN único
			if (!isGtinUnique(requestDTO.getGtin())) {
				log.warn("GTIN duplicado detectado: {}", requestDTO.getGtin());
				throw new ProductPackLevelServiceException(CustomErrorCode.CONFLICT, "Ya existe un nivel de embalaje con GTIN: " + requestDTO.getGtin(), HttpStatus.CONFLICT);
			}

			// 3. Crear entidad
			ProductPackLevel entity = mapper.toEntity(requestDTO);
			entity.setProduct(product);

			// 4. Validar con método de negocio (double check)
			// Las anotaciones @Min, @Max, etc. en DTO previenen entrada inválida,
			// pero validamos en la entidad también por robustez
			if (!entity.isValidConfiguration()) {
				throw new ProductPackLevelServiceException(CustomErrorCode.BAD_REQUEST, "La configuración logística no es válida: " + entity.getFullDescription(), HttpStatus.BAD_REQUEST);
			}

			// 5. Persistir
			ProductPackLevel saved = productPackLevelRepository.save(entity);

			log.info("✅ ProductPackLevel creado exitosamente - ID: {}, GTIN: {}", saved.getId(), saved.getGtin());

			return mapper.toDetailResponse(saved);

		} catch (ProductPackLevelServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("❌ Error inesperado al crear ProductPackLevel", e);
			throw new ProductPackLevelServiceException(CustomErrorCode.INTERNAL_SERVER_ERROR, "Error al crear el nivel de embalaje", HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = CACHE_NAME, key = "#id", unless = "#result == null")
	public ProductPackLevelDetailResponseDTO getProductPackLevelById(Long id) {
		log.debug("Buscando ProductPackLevel por ID: {}", id);
		ProductPackLevel entity = getProductPackLevelOrThrow(id);
		return mapper.toDetailResponse(entity);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = CACHE_BY_GTIN, key = "#gtin", unless = "#result == null")
	public ProductPackLevelDetailResponseDTO getProductPackLevelByGtin(String gtin) {
		log.debug("Buscando ProductPackLevel por GTIN: {}", gtin);

		ProductPackLevel entity = productPackLevelRepository.findByGtin(gtin).orElseThrow(() -> {
			log.warn("ProductPackLevel no encontrado - GTIN: {}", gtin);
			return new ProductPackLevelServiceException(CustomErrorCode.NOT_FOUND, "Nivel de embalaje no encontrado con GTIN: " + gtin, HttpStatus.NOT_FOUND);
		});

		return mapper.toDetailResponse(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ProductPackLevelResponseDTO> getAllProductPackLevels(Pageable pageable) {
		log.debug("Listando ProductPackLevels - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
		return productPackLevelRepository.findAll(pageable).map(mapper::toResponse);
	}

	@Override
	@Transactional
	@Caching(evict = { @CacheEvict(value = CACHE_NAME, allEntries = true), @CacheEvict(value = CACHE_BY_GTIN, allEntries = true), @CacheEvict(value = CACHE_BY_PRODUCT, allEntries = true) })
	public ProductPackLevelDetailResponseDTO updateProductPackLevel(Long id, UpdateProductPackLevelDTO updateDTO) {
		log.info("Actualizando ProductPackLevel - ID: {}", id);

		try {
			// 1. Obtener entidad existente
			ProductPackLevel entity = getProductPackLevelOrThrow(id);

			// 2. Actualizar campos
			// Las anotaciones @Min, @Max, etc. en UpdateDTO validan datos de entrada,
			// así que no necesitamos re-validar aquí
			mapper.updateEntityFromDTO(updateDTO, entity);

			// 3. Validar con métodos de negocio (double check)
			// Double check por robustez: datos fueron validados en DTO,
			// pero validamos en la entidad también
			if (!entity.isValidConfiguration()) {
				throw new ProductPackLevelServiceException(CustomErrorCode.BAD_REQUEST, "La configuración actualizada no es válida: " + entity.getFullDescription(), HttpStatus.BAD_REQUEST);
			}

			// 4. Persistir cambios
			ProductPackLevel updated = productPackLevelRepository.save(entity);

			log.info("✅ ProductPackLevel actualizado exitosamente - ID: {}", id);

			return mapper.toDetailResponse(updated);

		} catch (ProductPackLevelServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("❌ Error inesperado al actualizar ProductPackLevel", e);
			throw new ProductPackLevelServiceException(CustomErrorCode.INTERNAL_SERVER_ERROR, "Error al actualizar el nivel de embalaje", HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}

	@Override
	@Transactional
	@Caching(evict = { @CacheEvict(value = CACHE_NAME, allEntries = true), @CacheEvict(value = CACHE_BY_GTIN, allEntries = true), @CacheEvict(value = CACHE_BY_PRODUCT, allEntries = true) })
	public void deleteProductPackLevel(Long id) {
		log.info("Eliminando ProductPackLevel - ID: {}", id);

		try {
			// 1. Obtener entidad
			ProductPackLevel entity = getProductPackLevelOrThrow(id);

			// 2. Validar que puede ser eliminado
			if (!canBeDeleted(id)) {
				throw new ProductPackLevelServiceException(CustomErrorCode.CONFLICT, "No se puede eliminar el nivel de embalaje porque tiene palets asociados", HttpStatus.CONFLICT);
			}

			// 3. Eliminar
			productPackLevelRepository.delete(entity);

			log.info("✅ ProductPackLevel eliminado exitosamente - ID: {}", id);

		} catch (ProductPackLevelServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("❌ Error inesperado al eliminar ProductPackLevel", e);
			throw new ProductPackLevelServiceException(CustomErrorCode.INTERNAL_SERVER_ERROR, "Error al eliminar el nivel de embalaje", HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}

	// ========== BÚSQUEDAS POR PRODUCTO ==========

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = CACHE_BY_PRODUCT, key = "#productId", unless = "#result == null")
	public List<ProductPackLevelResponseDTO> getProductPackLevelsByProductId(Long productId) {
		log.debug("Buscando ProductPackLevels por Producto ID: {}", productId);

		// Validar que el producto existe
		getProductOrThrow(productId);

		return mapper.toResponseList(productPackLevelRepository.findByProductId(productId));
	}

	@Override
	@Transactional(readOnly = true)
	public Long countProductPackLevelsByProductId(Long productId) {
		log.debug("Contando ProductPackLevels del Producto ID: {}", productId);

		// Validar que el producto existe
		getProductOrThrow(productId);

		return productPackLevelRepository.countByProductId(productId);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasProductPackLevels(Long productId) {
		log.debug("Verificando si Producto ID: {} tiene niveles de embalaje", productId);

		// Validar que el producto existe
		getProductOrThrow(productId);

		return productPackLevelRepository.hasPackLevels(productId);
	}

	// ========== BÚSQUEDAS POR TIPO DE EMBALAJE ==========

	@Override
	@Transactional(readOnly = true)
	public List<ProductPackLevelResponseDTO> getProductPackLevelsByPackingLevel(PackingLevel packingLevel) {
		log.debug("Buscando ProductPackLevels por Packing Level: {}", packingLevel);

		return mapper.toResponseList(productPackLevelRepository.findByPackingLevel(packingLevel));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductPackLevelResponseDTO> getProductPackLevelsByProductIdAndPackingLevel(Long productId, PackingLevel packingLevel) {
		log.debug("Buscando ProductPackLevels - Producto: {}, Packing Level: {}", productId, packingLevel);

		// Validar que el producto existe
		getProductOrThrow(productId);

		return mapper.toResponseList(productPackLevelRepository.findByProductIdAndPackingLevel(productId, packingLevel));
	}


	@Override
	@Transactional(readOnly = true)
	public boolean canBeDeleted(Long id) {
		log.debug("Validando si ProductPackLevel ID: {} puede ser eliminado", id);

		ProductPackLevel entity = getProductPackLevelOrThrow(id);

		// TODO: Implementar lógica para verificar si tiene palets asociados
		// Por ahora retorna true (se permitirá eliminar)
		// Cuando se implemente relación bidireccional Palet↔ProductPackLevel:
		// return entity.getPalets().isEmpty();

		return true;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isGtinUnique(String gtin) {
		log.debug("Validando unicidad de GTIN: {}", gtin);
		return !productPackLevelRepository.existsByGtin(gtin);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isGtinUniqueExcluding(String gtin, Long excludeId) {
		log.debug("Validando unicidad de GTIN: {} excluyendo ID: {}", gtin, excludeId);

		return productPackLevelRepository.findByGtin(gtin).map(entity -> entity.getId().equals(excludeId)).orElse(true);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isValidConfiguration(ProductPackLevelRequestDTO requestDTO) {
		log.debug("Validando configuración de ProductPackLevel");

		return requestDTO != null && requestDTO.getNetWeight() != null && requestDTO.getNetWeight().signum() > 0 && requestDTO.getHeightMM() != null && requestDTO.getHeightMM().signum() > 0 && requestDTO.getWidthMM() != null && requestDTO.getWidthMM().signum() > 0
				&& requestDTO.getUnitsInLevel() != null && requestDTO.getUnitsInLevel() >= 1 && requestDTO.getStackingLimit() != null && requestDTO.getStackingLimit() >= 1 && requestDTO.getBoxesPerPalet() != null && requestDTO.getBoxesPerPalet() >= 0;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductPackLevelResponseDTO> getRecentlyCreatedProductPackLevels(int limit) {
		log.debug("Obteniendo últimos {} ProductPackLevels creados", limit);

		return mapper.toResponseList(productPackLevelRepository.findRecentlyCreated(limit));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductPackLevelResponseDTO> getRecentlyModifiedProductPackLevels(int limit) {
		log.debug("Obteniendo últimos {} ProductPackLevels modificados", limit);

		return mapper.toResponseList(productPackLevelRepository.findRecentlyModified(limit));
	}

	/**
	 * Obtiene un ProductPackLevel por ID o lanza excepción Reutilizable en múltiples métodos
	 */
	private ProductPackLevel getProductPackLevelOrThrow(Long id) {
		return productPackLevelRepository.findById(id).orElseThrow(() -> {
			log.warn("ProductPackLevel no encontrado - ID: {}", id);
			return new ProductPackLevelServiceException(CustomErrorCode.NOT_FOUND, "Nivel de embalaje no encontrado con ID: " + id, HttpStatus.NOT_FOUND);
		});
	}

	/**
	 * Obtiene un Product por ID o lanza excepción Reutilizable en múltiples métodos
	 */
	private Product getProductOrThrow(Long productId) {
		return productRepository.findById(productId).orElseThrow(() -> {
			log.warn("Producto no encontrado - ID: {}", productId);
			return new ProductPackLevelServiceException(CustomErrorCode.NOT_FOUND, "Producto no encontrado con ID: " + productId, HttpStatus.NOT_FOUND);
		});
	}
}