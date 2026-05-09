package com.abcm0018.sai.productos.application.bulk.service.loader;

import com.abcm0018.sai.productos.application.bulk.enums.LoaderType;
import com.abcm0018.sai.productos.application.bulk.exceptions.ProductBulkImportException;
import com.abcm0018.sai.shared.constants.CustomErrorCode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * La Fábrica (Factory) de Estrategias de carga.
 * <p>
 * PATRÓN: Strategy Pattern + Factory Pattern
 * <p>
 * RESPONSABILIDADES:
 * - Registrar loaders disponibles (inyectados por Spring)
 * - Seleccionar el loader correcto según tipo de archivo
 * - Manejar casos donde no hay loader disponible
 * - Extraer extensión del nombre del archivo
 * <p>
 * FLUJO:
 * 1. Cliente envía archivo CSV
 * 2. ProductLoaderContext extrae extensión: "csv"
 * 3. Context busca loader que soporte "csv"
 * 4. Encuentra CsvProductLoader
 * 5. Delega el parsing a CsvProductLoader
 * <p>
 * EJEMPLO DE USO:
 * <p>
 * String fileType = ProductLoaderContext.extractFileType("productos.csv");
 * ProductLoaderHelper loader = context.getLoaderStrategy(fileType);
 * List<ProductBulkImportDTO> products = loader.load(multipartFile);
 */
@Slf4j
@Component
public class ProductLoaderFactory {

	/**
	 * Constructor de la Fábrica.
	 * <p>
	 * Spring inyecta automáticamente una lista de todos los beans
	 * que implementen ProductLoaderStrategy.
	 * <p>
	 * Por ahora: CsvProductLoader
	 * En futuro: JsonProductLoader, ExcelProductLoader, etc.
	 */
	private final Map<LoaderType, ProductLoaderStrategy> strategies;

	public ProductLoaderFactory(List<ProductLoaderStrategy> loaders) {
		strategies = new EnumMap<>(LoaderType.class);

		loaders.forEach(loader -> strategies.put(loader.getLoaderType(), loader));
	}

	/**
	 * Obtiene el loader apropiado para el tipo de archivo especificado.
	 *
	 * @param loaderType tipo de archivo (extensión sin punto, ej.: "csv", "json")
	 * @return ProductLoaderHelper que soporta el tipo
	 * @throws ProductBulkImportException si no hay loader disponible
	 */
	public ProductLoaderStrategy getLoaderStrategy(LoaderType loaderType) {

		log.debug("🔍 Buscando loader para tipo: {}", loaderType);

		// Buscar el primer loader que soporte este tipo
		if (strategies.isEmpty()) {
			throw new ProductBulkImportException(CustomErrorCode.BAD_REQUEST, "Estrategías no encontratas para el tipo de loader", HttpStatus.BAD_REQUEST);
		}

		ProductLoaderStrategy loaderStrategy = strategies.get(loaderType);

		if (loaderStrategy == null) {
			log.warn("No se encontró un loader para el tipo: {}", loaderType);
			throw new ProductBulkImportException(CustomErrorCode.BAD_REQUEST, "No hay loader disponible para el tipo de loader: " + loaderType, HttpStatus.BAD_REQUEST);
		}

		return loaderStrategy;
	}

	/**
	 * Útil para saber qué tipos de carga están disponible.
	 */
	public Set<LoaderType> getAvailableLoaderTypes() {
		return strategies.keySet();
	}

}