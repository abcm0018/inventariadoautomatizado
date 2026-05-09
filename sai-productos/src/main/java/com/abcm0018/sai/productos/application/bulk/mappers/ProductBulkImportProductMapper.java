package com.abcm0018.sai.productos.application.bulk.mappers;

import com.abcm0018.sai.productos.application.bulk.dtos.ProductBulkImportDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ProductPackLevelDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ProductWithPackLevelsDTO;
import com.abcm0018.sai.productos.domain.entity.Product;
import com.abcm0018.sai.productos.domain.enums.ProductStatus;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * Mapper para convertir ProductBulkImportDTO a entidad Product.
 * <p>
 * Responsabilidades:
 * - Mapear datos del producto desde CSV DTO a entidad JPA
 * - Convertir string del status a enum ProductStatus
 * - Generar descripción si no existe
 * <p>
 * IMPORTANTE:
 * - No mapea relaciones (packLevels se asignan después)
 * - No mapea campos de auditoría (id, createdAt, updatedAt se generan)
 * - El productId en el DTO no se usa en este mapper
 */
@Mapper(componentModel = "spring")
public interface ProductBulkImportProductMapper {

	ProductBulkImportProductMapper INSTANCE = Mappers.getMapper(ProductBulkImportProductMapper.class);

	/**
	 * Mapea ProductBulkImportDTO a Product
	 * <p>
	 * Nota: Ignora campos específicos de PackLevel (packingLevel, gtin, etc.)
	 *
	 * @param dto DTO plano del CSV
	 * @return Product sin persistir, listo para guardar
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "packLevels", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "status", source = "status", qualifiedByName = "stringToProductStatus")
	Product toProductEntity(ProductBulkImportDTO dto);

	/**
	 * Convierte ProductPackLevelDTO a ProductBulkImportDTO temporalmente
	 * para reutilizar el mapper principal toProductPackLevelEntity.
	 *
	 * @param packLevelDto DTO de nivel de pack normalizado
	 * @return DTO de importación (CSV) con campos N/A
	 */
	@Mapping(target = "brand", constant = "N/A")
	@Mapping(target = "formatCode", constant = "N/A")
	@Mapping(target = "name", constant = "N/A")
	@Mapping(target = "status", constant = "ACTIVE")
	@Mapping(target = "manufacturedIn", constant = "N/A")
	@Mapping(target = "description", ignore = true) // Ignoramos campos no presentes
	ProductBulkImportDTO fromPackLevelDTO(ProductPackLevelDTO packLevelDto);

	/**
	 * Mapea el DTO normalizado (ProductWithPackLevelsDTO) a la entidad Product.
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "packLevels", ignore = true) // Los niveles se manejan por separado
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	// Reutilizamos el conversor de status que ya existe
	@Mapping(target = "status", source = "status", qualifiedByName = "stringToProductStatus")
	Product toProductEntity(ProductWithPackLevelsDTO dto);

	/**
	 * Convierte string de status a enum ProductStatus
	 * <p>
	 * Ejemplo: "ACTIVE" → ProductStatus.ACTIVE
	 *
	 * @param status string del status desde CSV
	 * @return enum ProductStatus
	 * @throws IllegalArgumentException si el status no es válido
	 */
	@Named("stringToProductStatus")
	default ProductStatus stringToProductStatus(String status) {
		if (status == null || status.isBlank()) {
			return ProductStatus.ACTIVE; // Default
		}
		try {
			// Usamos la misma lógica flexible que el validador
			return ProductStatus.fromDisplayNameOrName(status);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Estado (status) no válido procesado por el mapper: '" + status + "'");
		}
	}
}
