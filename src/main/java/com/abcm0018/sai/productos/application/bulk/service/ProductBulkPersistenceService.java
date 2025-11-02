package com.abcm0018.sai.productos.application.bulk.service;

import com.abcm0018.sai.productos.application.bulk.dtos.BulkImportResultDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ProductBulkImportDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ProductPackLevelDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ProductWithPackLevelsDTO;
import com.abcm0018.sai.productos.application.bulk.exceptions.ProductBulkPersistenceException;
import com.abcm0018.sai.productos.application.bulk.mappers.ProductBulkImportPackLevelMapper;
import com.abcm0018.sai.productos.application.bulk.mappers.ProductBulkImportProductMapper;
import com.abcm0018.sai.productos.domain.entity.Product;
import com.abcm0018.sai.productos.domain.entity.ProductPackLevel;
import com.abcm0018.sai.productos.domain.repository.ProductPackLevelRepository;
import com.abcm0018.sai.productos.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de persistencia transaccional para importación masiva de productos.
 * <p>
 * RESPONSABILIDADES:
 * - Persistir Products y ProductPackLevels de forma atómica
 * - Garantizar transaccionalidad
 * - Manejar errores de BD (constraint violations, etc.)
 * - Registrar operaciones
 * <p>
 * IMPORTANTE: Anotación @Transactional
 * - Si cualquier paso falla → ROLLBACK completo
 * - Si OK → COMMIT completo
 * - Garantiza consistencia de datos
 * <p>
 * ENTRADA: List<ProductWithPackLevelsDTO> (ya validados)
 * SALIDA: BulkImportResult (con counts de creados)
 * <p>
 * FLUJO PARA CADA PRODUCT:
 * 1. Convertir ProductWithPackLevelsDTO a entidad Product
 * 2. Guardar Product en BD (obtiene ID generado)
 * 3. Para cada ProductPackLevelDTO:
 *    - Convertir a entidad ProductPackLevel
 *    - Asignar relación con Product
 *    - Guardar ProductPackLevel en BD
 * 4. Si algo falla → Excepción → Rollback
 * 5. Si OK → Retornar BulkImportResult
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductBulkPersistenceService {

	private final ProductRepository productRepository;
	private final ProductPackLevelRepository productPackLevelRepository;
	private final ProductBulkImportProductMapper productBulkImportProductMapper;
	private final ProductBulkImportPackLevelMapper productBulkImportPackLevelMapper;

	/**
	 * Persiste de forma transaccional un lote de productos validados.
	 * <p>
	 * GARANTÍAS:
	 * - All-or-Nothing: Si falla uno, se hace el roolback completo
	 * - Atómico: No hay estado intermedio en BD
	 * - Consistente: Relaciones respetadas
	 *
	 * @param products lista de ProductWithPackLevelsDTO validados
	 * @return BulkImportResult con counts de creados
	 * @throws ProductBulkPersistenceException si hay error en BD
	 */
	@Transactional
	public BulkImportResultDTO persist(List<ProductWithPackLevelsDTO> products) {
		log.info("💾 Iniciando persistencia transaccional de {} productos", products.size());

		int productsCreated = 0;
		int packLevelsCreated = 0;

		try {
			// Lista para guardar TODOS los nivels de TODOS los productos en un solo lote
			List<ProductPackLevel> allPackLevelsToSave = new ArrayList<>();
			for (ProductWithPackLevelsDTO productDto : products) {
				log.debug("💾 Persistiendo producto: {}", productDto.getProductKey());

				// PASO 1: Crear y persistir Product
				Product product = persistProduct(productDto);
				productsCreated++;
				log.debug("✅ Product creado con ID: {}", product.getId());

				// PASO 2: Preparar ProductPackLevels para guardar en un solo lote
				for (ProductPackLevelDTO packLevelDto : productDto.getPackLevels()) {
					ProductPackLevel packLevel = mapPackLevel(packLevelDto, product);
					allPackLevelsToSave.add(packLevel);
					log.debug("✅ ProductPackLevel creado: {} - {}", packLevelDto.getPackingLevel(), packLevelDto.getGtin());
				}

				log.debug("✅ Producto {} completamente persistido con 3 niveles", productDto.getProductKey());
			}

			// PASO 3: Guardar TODOS los niveles de embalaje en UNA SOLA OPERACION BATCH
			log.info("💾 Guardando por lote {} niveles de embalaje...", allPackLevelsToSave.size());
			productPackLevelRepository.saveAll(allPackLevelsToSave);
			log.info("✅ Lote de niveles de embalaje guardado.");
			log.info("✅ Persistencia completada: {} productos, {} niveles", productsCreated, packLevelsCreated);

			return BulkImportResultDTO.builder()
					.productsCreated(productsCreated)
					.packLevelsCreated(packLevelsCreated)
					.timestamp(LocalDateTime.now())
					.success(true)
					.build();

		} catch (Exception e) {
			log.error("❌ Error crítico en persistencia (rollback activado): {}", e.getMessage(), e);
			// La anotación @Transactional hace rollback automático aquí
			throw new ProductBulkPersistenceException(
					String.format("Error al guardar en BD. Se han revertido los cambios. Detalle: %s", e.getMessage()), e);
		}
	}

	/**
	 * Persiste un único Product.
	 *
	 * @param productDto ProductWithPackLevelsDTO con datos del product
	 * @return Product persistido con ID generado
	 * @throws ProductBulkPersistenceException si hay error
	 */
	private Product persistProduct(ProductWithPackLevelsDTO productDto) {
		try {
			// Convertir DTO a entidad
			Product product = productBulkImportProductMapper.toProductEntity(productDto);

			// Persistir
			Product saved = productRepository.save(product);
			log.debug("💾 Product guardado: {} (ID: {})", saved.getName(), saved.getId());

			return saved;

		} catch (Exception e) {
			String errorMsg = String.format("Error persistiendo Product %s: %s", productDto.getProductKey(), e.getMessage());
			log.error("❌ {}", errorMsg);
			throw new ProductBulkPersistenceException(errorMsg, e);
		}
	}

	/**
	 * Mapea un DTO de nivel a una entidad ProductPackLevel y asigna el producto.
	 * NO guarda en la base de datos.
	 *
	 * @param packLevelDto DTO con datos del nivel
	 * @param product Product asociado (ya persistido)
	 * @return Entidad ProductPackLevel lista para ser guardada
	 * @throws ProductBulkPersistenceException si hay error de mapeo
	 */
	private ProductPackLevel mapPackLevel(ProductPackLevelDTO packLevelDto, Product product) {
		try {
			// Convertir DTO a entidad usando mapper
			ProductBulkImportDTO tempDto = productBulkImportProductMapper.fromPackLevelDTO(packLevelDto);
			ProductPackLevel packLevel = productBulkImportPackLevelMapper.toProductPackLevelEntity(tempDto);

			// Asignar relación con Product
			packLevel.setProduct(product);

			return packLevel;
		} catch (Exception e) {
			String errorMsg = String.format("Error mapeando ProductPackLevel %s (GTIN: %s): %s",
					packLevelDto.getPackingLevel(), packLevelDto.getGtin(), e.getMessage());

			log.error("❌ {}", errorMsg);
			// Lanzamos la excepción para que el @Transactional haga rollback
			throw new ProductBulkPersistenceException(errorMsg, e);
		}
	}

}