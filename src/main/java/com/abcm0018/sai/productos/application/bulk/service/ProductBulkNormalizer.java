package com.abcm0018.sai.productos.application.bulk.service;

import com.abcm0018.sai.productos.application.bulk.dtos.ProductBulkImportDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ProductPackLevelDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ProductWithPackLevelsDTO;
import com.abcm0018.sai.productos.application.bulk.exceptions.InvalidGtinFormatException;
import com.abcm0018.sai.productos.application.bulk.exceptions.ProductNormalizationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de normalización que convierte datos planos del CSV a estructura jerárquica validada.
 * <p>
 * ENTRADA: List<ProductBulkImportDTO> (estructura plana, sin validar)
 * SALIDA: List<ProductWithPackLevelsDTO> (estructura jerárquica, validada)
 * <p>
 * RESPONSABILIDADES:
 * 1. Agrupar por brand+formatCode para identificar productos únicos
 * 2. Para cada producto:
 *    - Validar que tiene exactamente 3 niveles (UNIDAD, CAJA, PALET)
 *    - Validar que están en orden correcto
 *    - Validar que las quantities son válidas para cada tipo
 *    - Validar que los GTINs tienen el formato correcto
 *    - Validar que dimensiones y peso son válidos
 * 3. Convertir a estructura jerárquica ProductWithPackLevelsDTO
 * 4. Retornar lista normalizada o lanzar excepciones detalladas
 * <p>
 * VALIDACIONES ESTRUCTURALES:
 * - 3 niveles exactos por producto
 * - Orden: UNIDAD → CAJA → PALET
 * - UNIDAD: quantity = 1
 * - CAJA: quantity > 1
 * - PALET: quantity > 1
 * - GTIN-13 para UNIDAD/CAJA, GTIN-14 para PALET
 * - Peso > 0
 * - Dimensiones > 0
 */
@Slf4j
@Service
public class ProductBulkNormalizer {

	private static final String LEVEL_UNIDAD = "UNIDAD";
	private static final String LEVEL_CAJA = "CAJA";
	private static final String LEVEL_PALET = "PALET";
	private static final int GTIN_13_LENGTH = 13;
	private static final int GTIN_14_LENGTH = 14;

	/**
	 * Normaliza y valida datos planos del CSV a estructura jerárquica.
	 *
	 * @param flatDtos lista plana de ProductBulkImportDTO
	 * @return lista normalizada de ProductWithPackLevelsDTO
	 * @throws ProductNormalizationException si hay error en normalización
	 */
	public List<ProductWithPackLevelsDTO> normalize(List<ProductBulkImportDTO> flatDtos) {
		log.info("🔄 Iniciando normalización de {} filas CSV", flatDtos.size());

		// PASO 1: Agrupar por productKey (brand-formatCode)
		Map<String, List<ProductBulkImportDTO>> groupedByProduct = groupByProduct(flatDtos);
		log.info("📦 {} productos únicos encontrados", groupedByProduct.size());

		List<ProductWithPackLevelsDTO> normalized = new ArrayList<>();

		// PASO 2: Procesar cada producto
		for (Map.Entry<String, List<ProductBulkImportDTO>> entry : groupedByProduct.entrySet()) {
			String productKey = entry.getKey();
			List<ProductBulkImportDTO> productRows = entry.getValue();

			try {
				log.debug("🔍 Normalizando producto: {}", productKey);

				// Validar estructura (3 niveles, orden correcto)
				validateProductStructure(productKey, productRows);

				// Validar cada nivel
				for (ProductBulkImportDTO row : productRows) {
					validatePackLevel(row);
				}

				// Convertir a DTO normalizado
				ProductWithPackLevelsDTO product = convertToNormalized(productRows);
				normalized.add(product);

				log.debug("✅ Producto normalizado: {}", productKey);

			} catch (ProductNormalizationException e) {
				log.error("❌ Error normalizando producto {}: {}", productKey, e.getMessage());
				throw e;
			}
		}

		log.info("✅ Normalización completada: {} productos", normalized.size());
		return normalized;
	}

	/**
	 * Agrupa filas planas por productKey (brand-formatCode).
	 * <p>
	 * IMPORTANTE: Preserva el orden usando LinkedHashMap
	 */
	private Map<String, List<ProductBulkImportDTO>> groupByProduct(List<ProductBulkImportDTO> flatDtos) {
		return flatDtos.stream()
				.collect(Collectors.groupingBy(
						ProductBulkImportDTO::getProductKey,
						LinkedHashMap::new,  // Preserva orden de inserción
						Collectors.toList()
				));
	}

	/**
	 * Valida que el producto tiene estructura correcta.
	 * <p>
	 * Validaciones:
	 * - Exactamente 3 niveles
	 * - Orden correcto: UNIDAD → CAJA → PALET
	 */
	private void validateProductStructure(String productKey, List<ProductBulkImportDTO> rows) {
		// Validación 1: Debe tener exactamente 3 filas
		if (rows.size() != 3) {
			throw new ProductNormalizationException(productKey, String.format("Esperados 3 niveles, encontrados %d", rows.size()));
		}

		// Validación 2: Orden correcto
		if (!LEVEL_UNIDAD.equalsIgnoreCase(rows.get(0).getPackingLevel())) {
			throw new ProductNormalizationException(productKey, "Primer nivel debe ser UNIDAD, encontrado: " + rows.get(0).getPackingLevel());
		}

		if (!LEVEL_CAJA.equalsIgnoreCase(rows.get(1).getPackingLevel())) {
			throw new ProductNormalizationException(productKey, "Segundo nivel debe ser CAJA, encontrado: " + rows.get(1).getPackingLevel());
		}

		if (!LEVEL_PALET.equalsIgnoreCase(rows.get(2).getPackingLevel())) {
			throw new ProductNormalizationException(productKey, "Tercer nivel debe ser PALET, encontrado: " + rows.get(2).getPackingLevel());
		}
	}

	/**
	 * Valida un ProductPackLevel individual.
	 * <p>
	 * Validaciones:
	 * - Quantity válida para su tipo
	 * - GTIN formato correcto
	 * - Dimensiones y peso válidos
	 */
	private void validatePackLevel(ProductBulkImportDTO dto) {
		// Validación 1: Quantity válida para tipo
		if (!dto.isValidQuantityForPackLevel()) {
			throw new ProductNormalizationException(dto.getPackingLevel(), dto.getUnitsInLevel());
		}

		// Validación 2: GTIN formato correcto
		validateGtinFormat(dto);

		// Validación 3: Dimensiones y peso > 0
		if (dto.getNetWeight() == null || dto.getNetWeight().signum() <= 0) {
			throw new ProductNormalizationException("NetWeight debe ser mayor a 0");
		}

		if (dto.getHeightMM() == null || dto.getHeightMM().signum() <= 0) {
			throw new ProductNormalizationException("HeightMM debe ser mayor a 0");
		}

		if (dto.getWidthMM() == null || dto.getWidthMM().signum() <= 0) {
			throw new ProductNormalizationException("WidthMM debe ser mayor a 0");
		}
	}

	/**
	 * Valida que el GTIN tiene el formato correcto para su tipo de nivel.
	 * <p>
	 * Reglas:
	 * - UNIDAD y CAJA: GTIN-13 (13 dígitos)
	 * - PALET: GTIN-14 (14 dígitos)
	 */
	private void validateGtinFormat(ProductBulkImportDTO dto) {
		int gtinLength = dto.getGtin().length();

		if (dto.isPaletPackLevel()) {
			// PALET debe ser GTIN-14
			if (gtinLength != GTIN_14_LENGTH) {
				throw new InvalidGtinFormatException(dto.getGtin(), LEVEL_PALET);
			}
		} else {
			// UNIDAD y CAJA deben ser GTIN-13
			if (gtinLength != GTIN_13_LENGTH) {
				throw new InvalidGtinFormatException(dto.getGtin(), dto.getPackingLevel());
			}
		}
	}

	/**
	 * Convierte lista de ProductBulkImportDTO a ProductWithPackLevelsDTO normalizado.
	 *
	 * @param rows exactamente 3 filas (UNIDAD, CAJA, PALET)
	 * @return ProductWithPackLevelsDTO normalizado
	 */
	private ProductWithPackLevelsDTO convertToNormalized(List<ProductBulkImportDTO> rows) {
		// Tomar información del producto desde la primera fila (todas tienen lo mismo)
		ProductBulkImportDTO first = rows.get(0);

		// Convertir cada fila a ProductPackLevelDTO
		List<ProductPackLevelDTO> packLevels = rows.stream().map(this::convertToPackLevel).toList();

		return ProductWithPackLevelsDTO.builder()
				.productKey(first.getProductKey())
				.brand(first.getBrand())
				.formatCode(first.getFormatCode())
				.name(first.getName())
				.description(first.getDescription())
				.manufacturedIn(first.getManufacturedIn())
				.status(first.getStatus())
				.packLevels(packLevels)
				.build();
	}

	/**
	 * Convierte ProductBulkImportDTO a ProductPackLevelDTO.
	 */
	private ProductPackLevelDTO convertToPackLevel(ProductBulkImportDTO dto) {
		return ProductPackLevelDTO.builder()
				.packingLevel(dto.getPackingLevel())
				.gtin(dto.getGtin())
				.unitsInLevel(dto.getUnitsInLevel())
				.netWeight(dto.getNetWeight())
				.heightMM(dto.getHeightMM())
				.widthMM(dto.getWidthMM())
				.stackingLimit(dto.getStackingLimit())
				.boxesPerPalet(dto.getBoxesPerPalet())
				.build();
	}
}