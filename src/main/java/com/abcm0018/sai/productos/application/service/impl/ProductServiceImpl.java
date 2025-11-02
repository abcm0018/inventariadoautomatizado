package com.abcm0018.sai.productos.application.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abcm0018.sai.productos.application.dtos.ProductDetailResponseDTO;
import com.abcm0018.sai.productos.application.dtos.ProductRequestDTO;
import com.abcm0018.sai.productos.application.dtos.ProductResponseDTO;
import com.abcm0018.sai.productos.application.dtos.UpdateProductRequestDTO;
import com.abcm0018.sai.productos.application.mappers.ProductMapper;
import com.abcm0018.sai.productos.application.service.ProductService;
import com.abcm0018.sai.productos.domain.entity.Product;
import com.abcm0018.sai.productos.domain.enums.ProductStatus;
import com.abcm0018.sai.productos.domain.repository.ProductPackLevelRepository;
import com.abcm0018.sai.productos.domain.repository.ProductRepository;
import com.abcm0018.sai.productos.exceptions.ProductServiceException;
import com.abcm0018.sai.shared.constants.CustomErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final ProductPackLevelRepository productPackLevelRepository;
	private final ProductMapper mapper;

	private static final String CACHE_NAME = "products";
	private static final String CACHE_BY_BRAND_FORMAT = "productsByBrandFormat";
	private static final String CACHE_BY_STATUS = "productsByStatus";

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = CACHE_NAME, allEntries = true),
			@CacheEvict(value = CACHE_BY_STATUS, allEntries = true)
	})
	public ProductDetailResponseDTO createProduct(ProductRequestDTO requestDTO) {

		log.info("Creando nuevo producto - Brand: {}, FormatCode: {}", requestDTO.getBrand(), requestDTO.getFormatCode());

		try {

			Product entity = mapper.toEntity(requestDTO);
			Product saved = productRepository.save(entity);

			log.info("Producto creado exitosamente - ID: {}, Brand: {}, FormatCode: {}", saved.getId(), saved.getBrand(), saved.getFormatCode());

			return mapper.toDetailResponse(saved);

		} catch (ProductServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error inesperado al crear producto", e);
			throw new ProductServiceException(CustomErrorCode.INTERNAL_SERVER_ERROR, "Error al crear el producto", HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = CACHE_NAME, key = "#id", unless = "#result == null")
	public ProductDetailResponseDTO getProductById(Long id) {
		log.debug("Consultando producto con ID: {}", id);
		Product entity = getProductOrThrow(id);
		return mapper.toDetailResponse(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
		log.debug("Listando productos - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
		return productRepository.findAll(pageable).map(mapper::toResponse);
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = CACHE_NAME, allEntries = true),
			@CacheEvict(value = CACHE_BY_BRAND_FORMAT, allEntries = true),
			@CacheEvict(value = CACHE_BY_STATUS, allEntries = true)
	})
	public ProductDetailResponseDTO updateProduct(Long id, UpdateProductRequestDTO updateDTO) {
		log.info("Actualizando producto - ID: {}", id);

		try {
			Product entity = getProductOrThrow(id);
			mapper.updateEntityFromDTO(updateDTO, entity);

			Product updated = productRepository.save(entity);

			log.info("Producto actualizado exitosamente - ID: {}", id);

			return mapper.toDetailResponse(updated);

		} catch (ProductServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error inesperado al actualizar producto", e);
			throw new ProductServiceException(CustomErrorCode.INTERNAL_SERVER_ERROR, "Error al actualizar el producto", HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = CACHE_NAME, allEntries = true),
			@CacheEvict(value = CACHE_BY_BRAND_FORMAT, allEntries = true),
			@CacheEvict(value = CACHE_BY_STATUS, allEntries = true)
	})
	public void deleteProduct(Long id) {
		log.info("Eliminando producto - ID: {}", id);

		try {
			Product entity = getProductOrThrow(id);

			if (!canBeDeleted(id)) {
				throw new ProductServiceException(
						CustomErrorCode.CONFLICT, "No se puede eliminar el producto porque tiene niveles de embalaje asociados",
						HttpStatus.CONFLICT
				);
			}

			productRepository.delete(entity);

			log.info("Producto eliminado exitosamente - ID: {}", id);

		} catch (ProductServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error inesperado al eliminar producto", e);
			throw new ProductServiceException(CustomErrorCode.INTERNAL_SERVER_ERROR, "Error al eliminar el producto",
					HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = CACHE_BY_BRAND_FORMAT, key = "#brand + '_' + #formatCode", unless = "#result == null")
	public ProductDetailResponseDTO getProductByBrandAndFormatCode(String brand, String formatCode) {
		log.debug("Consultando producto - Brand: {}, FormatCode: {}", brand, formatCode);

		Product entity = productRepository.findByBrandAndFormatCode(brand, formatCode)
				.orElseThrow(() -> {
					log.warn("Producto no encontrado - Brand: {}, FormatCode: {}", brand, formatCode);
					return new ProductServiceException(
							CustomErrorCode.NOT_FOUND,
							"Producto no encontrado con brand: " + brand + " y formatCode: " + formatCode,
							HttpStatus.NOT_FOUND
					);
				});

		return mapper.toDetailResponse(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductResponseDTO> getProductsByName(String name) {
		log.debug("Buscando productos por nombre: {}", name);
		return mapper.toResponseList(productRepository.findByNameContainingIgnoreCase(name));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductResponseDTO> getProductsByBrand(String brand) {
		log.debug("Buscando productos por marca: {}", brand);
		return mapper.toResponseList(productRepository.findByBrand(brand));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductResponseDTO> getProductsByFormatCode(String formatCode) {
		log.debug("Buscando productos por formatCode: {}", formatCode);
		return mapper.toResponseList(productRepository.findByFormatCode(formatCode));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = CACHE_BY_STATUS, key = "#status", unless = "#result == null")
	public List<ProductResponseDTO> getProductsByStatus(ProductStatus status) {
		log.debug("Buscando productos por estado: {}", status);
		return mapper.toResponseList(productRepository.findByStatus(status));
	}

	@Override
	@Transactional(readOnly = true)
	public Long countByStatus(ProductStatus status) {
		log.debug("Contando productos por estado: {}", status);
		return productRepository.countByStatus(status);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductResponseDTO> getProductsByManufacturedIn(String country) {
		log.debug("Buscando productos fabricados en: {}", country);
		return mapper.toResponseList(productRepository.findByManufacturedIn(country));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductResponseDTO> getProductsWithPackLevels() {
		log.debug("Buscando productos con niveles de embalaje");
		return mapper.toResponseList(productRepository.findProductsWithPackLevels());
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductResponseDTO> getProductsWithoutPackLevels() {
		log.debug("Buscando productos sin niveles de embalaje");
		return mapper.toResponseList(productRepository.findProductsWithoutPackLevels());
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByBrandAndFormatCode(String brand, String formatCode) {
		log.debug("Verificando existencia - Brand: {}, FormatCode: {}", brand, formatCode);
		return productRepository.findByBrandAndFormatCode(brand, formatCode).isPresent();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByBrandAndFormatCodeExcluding(String brand, String formatCode, Long excludeId) {
		log.debug("Verificando existencia excluyendo ID: {} - Brand: {}, FormatCode: {}",
				excludeId, brand, formatCode);

		return productRepository.findByBrandAndFormatCode(brand, formatCode)
				.map(product -> !product.getId().equals(excludeId)).orElse(false);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean canBeDeleted(Long id) {
		log.debug("Validando si producto ID: {} puede ser eliminado", id);

		Product entity = getProductOrThrow(id);

		boolean hasPackLevels = entity.hasPackLevels();

		if (hasPackLevels) {
			log.warn("Producto ID: {} no puede ser eliminado - Tiene {} niveles de embalaje", id, entity.countPackLevels());
			return false;
		}

		return true;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductResponseDTO> getProductsCreatedBetween(LocalDate startDate, LocalDate endDate) {
		log.debug("Buscando productos creados entre {} y {}", startDate, endDate);
		return mapper.toResponseList(productRepository.findProductsCreatedBetween(startDate, endDate));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ProductResponseDTO> findWithFilters(ProductStatus status, String brand, String name, String country, Pageable pageable) {

		log.debug("Búsqueda avanzada con filtros - Status: {}, Brand: {}, Name: {}, Country: {}", status, brand, name, country);

		return productRepository.findWithFilters(status, brand, name, country, pageable).map(mapper::toResponse);
	}

	/**
	 * Obtiene un producto por ID o lanza excepción
	 * Reutilizable en múltiples métodos
	 */
	private Product getProductOrThrow(Long id) {
		return productRepository.findById(id)
				.orElseThrow(() -> {
					log.warn("Producto no encontrado - ID: {}", id);
					return new ProductServiceException(CustomErrorCode.NOT_FOUND, "Producto no encontrado con ID: " + id, HttpStatus.NOT_FOUND);
				});
	}

}
