package com.abcm0018.sai.productos.application.bulk.mappers;

import com.abcm0018.sai.productos.application.bulk.dtos.ProductBulkImportDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ProductWithPackLevelsDTO;
import com.abcm0018.sai.productos.domain.entity.Product;
import com.abcm0018.sai.productos.domain.entity.ProductPackLevel;
import com.abcm0018.sai.productos.domain.enums.PackingLevel;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * Mapper para convertir ProductBulkImportDTO a entidad ProductPackLevel.
 * <p>
 * Responsabilidades:
 * - Mapear datos del nivel de embalaje desde CSV DTO a entidad JPA
 * - Convertir string del packingLevel a enum PackingLevel
 * - Asignar relación con Product (manual, después del mapeo)
 * <p>
 * IMPORTANTE:
 * - El campo 'product' NO se mapea aquí (se asigna manualmente en el Service)
 * - No mapea campos de auditoría (id, createdAt, updatedAt se generan)
 * - El campo 'quantity' en DTO mapea a 'unitsInLevel' en entity
 */
@Mapper(componentModel = "spring")
public interface ProductBulkImportPackLevelMapper {

	ProductBulkImportPackLevelMapper INSTANCE = Mappers.getMapper(ProductBulkImportPackLevelMapper.class);

	/**
	 * Mapea ProductBulkImportDTO a ProductPackLevel (sin relación con Product)
	 * <p>
	 * Nota: El Campo 'product' no se mapea. Debe asignarse manualmente en el Service
	 * después de crear el Product.
	 * <p>
	 * MAPEOS ESPECIALES:
	 * - quantity → unitsInLevel
	 * - weight → netWeight
	 * - height → heightMM
	 * - width → widthMM
	 * - packingLevel (string) → PackingLevel (enum)
	 *
	 * @param dto DTO plano del CSV
	 * @return ProductPackLevel sin persistir, sin relación con Product
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "product", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(source = "unitsInLevel", target = "unitsInLevel")
	@Mapping(source = "netWeight", target = "netWeight")
	@Mapping(source = "heightMM", target = "heightMM")
	@Mapping(source = "widthMM", target = "widthMM")
	@Mapping(source = "packingLevel", target = "packingLevel", qualifiedByName = "stringToPackingLevel")
	ProductPackLevel toProductPackLevelEntity(ProductBulkImportDTO dto);

	/**
	 * Mapea ProductBulkImportDTO a ProductPackLevel y asigna la relación con Product
	 * <p>
	 * Realiza el mapeo Y asigna el Product.
	 * Úsalo en el Service cuando ya tengas el Product creado.
	 *
	 * @param dto DTO plano del CSV
	 * @param product Product ya creado y persistido
	 * @return ProductPackLevel con relación asignada, listo para guardar
	 */
	default ProductPackLevel toProductPackLevelEntityWithProduct(ProductBulkImportDTO dto, Product product) {
		ProductPackLevel packLevel = this.toProductPackLevelEntity(dto);
		packLevel.setProduct(product);
		return packLevel;
	}

	/**
	 * Convierte string de packingLevel a enum PackingLevel
	 * (Acepta nombre técnico o legible, ej: "UNIT" o "Unidad")
	 */
	@Named("stringToPackingLevel")
	default PackingLevel stringToPackingLevel(String packingLevel) {
		if (packingLevel == null || packingLevel.isBlank()) {
			throw new IllegalArgumentException("PackingLevel no puede estar vacío");
		}
		try {
			// Usamos el helper del Enum que es más flexible
			return PackingLevel.fromDisplayNameOrName(packingLevel);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("PackingLevel no válido procesado por el mapper: '" + packingLevel + "'");
		}
	}
}
