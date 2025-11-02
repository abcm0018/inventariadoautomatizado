package com.abcm0018.sai.productos.application.mappers;

import java.util.List;

import com.abcm0018.sai.productos.application.dtos.ProductPackLevelDetailResponseDTO;
import com.abcm0018.sai.productos.application.dtos.ProductPackLevelRequestDTO;
import com.abcm0018.sai.productos.application.dtos.ProductPackLevelResponseDTO;
import com.abcm0018.sai.productos.application.dtos.UpdateProductPackLevelDTO;
import com.abcm0018.sai.productos.domain.entity.ProductPackLevel;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * Mapper para ProductPackLevel usando MapStruct
 * <p>
 * Responsabilidades:
 * - Convertir ProductPackLevel ↔ DTOs
 * - Aplicar cálculos de negocio en mapeos hacia Detail
 * - Validar integridad de datos en mapeos
 * <p>
 * Buenas Prácticas:
 * - Métodos específicos para cada caso de uso
 * - @AfterMapping para lógica post-conversión
 * - Soporte para null safety
 * - No mezclar lógica de negocio compleja (delegar a Service)
 */
@Mapper(componentModel = "spring")
public interface ProductPackLevelMapper {

	ProductPackLevelMapper INSTANCE = Mappers.getMapper(ProductPackLevelMapper.class);

	/**
	 * Convierte ProductPackLevelRequestDTO (entrada del cliente) a entidad
	 * <p>
	 * Nota: El productId viene en el DTO, pero se resuelve en el Service
	 * (no aquí, para mantener la separación de capas)
	 *
	 * @param requestDTO entrada del cliente
	 * @return entidad ProductPackLevel sin persistir
	 */
	ProductPackLevel toEntity(ProductPackLevelRequestDTO requestDTO);

	/**
	 * Convierte entidad a DTO de respuesta estándar
	 * Uso: Para listados y respuestas simples
	 *
	 * @param entity entidad persistida
	 * @return DTO con información básica
	 */
	ProductPackLevelResponseDTO toResponse(ProductPackLevel entity);

	/**
	 * Convierte entidad a DTO detallado con cálculos de negocio
	 * Uso: Para GET by ID y respuestas enriquecidas
	 *
	 * @param entity entidad persistida
	 * @return DTO con cálculos incluidos
	 */
	ProductPackLevelDetailResponseDTO toDetailResponse(ProductPackLevel entity);


	/**
	 * Convierte lista de entidades a lista de DTOs de respuesta
	 *
	 * @param entities lista de entidades
	 * @return lista de DTOs
	 */
	List<ProductPackLevelResponseDTO> toResponseList(List<ProductPackLevel> entities);

	/**
	 * Convierte lista de entidades a lista de DTOs detallados
	 *
	 * @param entities lista de entidades
	 * @return lista de DTOs detallados
	 */
	List<ProductPackLevelDetailResponseDTO> toDetailResponseList(List<ProductPackLevel> entities);

	/**
	 * Actualiza una entidad existente con datos del DTO de actualización
	 * <p>
	 * Nota: Solo actualiza campos permitidos:
	 * - netWeight — heightMM — widthMM — unitsInLevel — stackingLimit — boxesPerPalet
	 * <p>
	 * NO actualizables:
	 * - GTIN (clave única)
	 * - productId (relación fija)
	 * - packingLevel (tipo definido)
	 * - createdAt (auditoría)
	 *
	 * @param updateDTO datos de actualización
	 * @param entity entidad a actualizar (será modificada)
	 */
	void updateEntityFromDTO(UpdateProductPackLevelDTO updateDTO, @MappingTarget ProductPackLevel entity);

	/**
	 * Aplica lógica de negocio después del mapeo a DTO detallado
	 * <p>
	 * Se ejecuta automáticamente al llamar toDetailResponse()
	 * <p>
	 * Realiza:
	 * - Cálculo de totalWeightPerPalet
	 * - Generación de descripción legible
	 * - Validación de configuración
	 *
	 * @param entity entidad origen
	 * @param dto DTO destino a enriquecer
	 */
	@AfterMapping
	default void enrichDetailResponse(ProductPackLevel entity, @MappingTarget ProductPackLevelDetailResponseDTO dto) {
		if (entity != null && dto != null) {
			// Cálculo 1: Peso total del palet
			dto.setTotalWeightPerPalet(entity.getTotalWeightPerPalet());

			// Cálculo 2: Descripción legible
			dto.setDescription(entity.getFullDescription());

			// Cálculo 3: Validación de configuración
			dto.setValidConfiguration(entity.isValidConfiguration());
		}
	}

}
