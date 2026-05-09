package com.abcm0018.sai.productos.application.mappers;

import java.util.List;

import com.abcm0018.sai.productos.application.dtos.ProductDetailResponseDTO;
import com.abcm0018.sai.productos.application.dtos.ProductRequestDTO;
import com.abcm0018.sai.productos.application.dtos.ProductResponseDTO;
import com.abcm0018.sai.productos.application.dtos.UpdateProductRequestDTO;
import com.abcm0018.sai.productos.domain.entity.Product;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * Mapper para Product usando MapStruct
 * <p>
 * Responsabilidades:
 * - Convertir Product ↔ DTOs
 * - Calcular packLevelCount en mapeos hacia Detail
 * - Calcular isValidForInventory en mapeos hacia Detail
 * - Validar integridad de datos en mapeos
 * <p>
 * Buenas Prácticas:
 * - Métodos específicos para cada caso de uso
 * - @AfterMapping para lógica post-conversión
 * - Soporte para null safety
 * - No mezclar lógica de negocio compleja (delegar a Service)
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

	ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

	/**
	 * Convierte ProductRequestDTO (entrada del cliente) a entidad
	 *
	 * @param requestDTO entrada del cliente
	 * @return entidad Product sin persistir
	 */
	Product toEntity(ProductRequestDTO requestDTO);

	/**
	 * Convierte entidad a DTO de respuesta estándar
	 * Uso: Para listados y respuestas simples
	 *
	 * @param entity entidad persistida
	 * @return DTO con información básica
	 */
	ProductResponseDTO toResponse(Product entity);

	/**
	 * Convierte entidad a DTO detallado con cálculos de negocio
	 * Uso: Para GET by ID y respuestas enriquecidas
	 *
	 * @param entity entidad persistida
	 * @return DTO con cálculos incluidos
	 */
	ProductDetailResponseDTO toDetailResponse(Product entity);

	/**
	 * Convierte lista de entidades a lista de DTOs de respuesta
	 *
	 * @param entities lista de entidades
	 * @return lista de DTOs
	 */
	List<ProductResponseDTO> toResponseList(List<Product> entities);

	/**
	 * Convierte lista de entidades a lista de DTOs detallados
	 *
	 * @param entities lista de entidades
	 * @return lista de DTOs detallados
	 */
	List<ProductDetailResponseDTO> toDetailResponseList(List<Product> entities);

	/**
	 * Actualiza una entidad existente con datos del DTO de actualización
	 * <p>
	 * Nota: Solo actualiza campos permitidos:
	 * - description
	 * - status
	 * <p>
	 * NO actualizables:
	 * - name (identidad)
	 * - brand (identidad)
	 * - formatCode (identidad)
	 * - manufacturedIn (origen fijo)
	 * - createdAt (auditoría)
	 *
	 * @param updateDTO datos de actualización
	 * @param entity entidad a actualizar (será modificada)
	 */
	void updateEntityFromDTO(UpdateProductRequestDTO updateDTO, @MappingTarget Product entity);

	/**
	 * Aplica lógica de negocio después del mapeo a DTO de respuesta
	 * <p>
	 * Se ejecuta automáticamente al llamar toResponse()
	 * <p>
	 * Realiza:
	 * - Cálculo de packLevelCount
	 *
	 * @param entity entidad origen
	 * @param dto DTO destino a enriquecer
	 */
	@AfterMapping
	default void enrichResponse(Product entity, @MappingTarget ProductResponseDTO dto) {
		if (entity != null && dto != null) {
			dto.setPackLevelCount(entity.countPackLevels());
		}
	}

	/**
	 * Aplica lógica de negocio después del mapeo a DTO detallado
	 * <p>
	 * Se ejecuta automáticamente al llamar toDetailResponse()
	 * <p>
	 * Realiza:
	 * - Cálculo de packLevelCount
	 * - Validación de integridad logística (isValidForInventory)
	 *
	 * @param entity entidad origen
	 * @param dto DTO destino a enriquecer
	 */
	@AfterMapping
	default void enrichDetailResponse(Product entity, @MappingTarget ProductDetailResponseDTO dto) {
		if (entity != null && dto != null) {
			dto.setPackLevelCount(entity.countPackLevels());
			dto.setValidForInventory(entity.isValidForInventory());
		}
	}

}
