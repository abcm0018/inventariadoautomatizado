package com.abcm0018.sai.productos.application.bulk.mappers;

import com.abcm0018.sai.productos.application.bulk.dtos.BulkImportErrorDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.BulkImportResponseDTO;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Mapper para construir respuestas de importación masiva.
 * <p>
 * Responsabilidades:
 * - Construir BulkImportResponse desde el resultado del proceso
 * - Convertir excepciones a BulkImportError
 * - Generar mensajes legibles
 * <p>
 * NOTA: Este mapper es más para referencia. Los métodos de construcción
 * pueden ser métodos helper en lugar de usar MapStruct puro.
 */
@Mapper(componentModel = "spring")
public interface BulkImportResponseMapper {

	BulkImportResponseMapper INSTANCE = Mappers.getMapper(BulkImportResponseMapper.class);

	/**
	 * Construye una respuesta exitosa
	 */
	default BulkImportResponseDTO buildSuccessResponse(Integer productsCreated, Integer packLevelsCreated) {
		return BulkImportResponseDTO.builder()
				.status("SUCCESS")
				.message(String.format("Importación completada exitosamente: %d productos, %d niveles de embalaje", productsCreated, packLevelsCreated))
				.productsCreated(productsCreated)
				.packLevelsCreated(packLevelsCreated)
				.errors(null)
				.build();
	}

	/**
	 * Construye una respuesta con éxito parcial (algunos errores)
	 */
	default BulkImportResponseDTO buildPartialSuccessResponse(Integer productsCreated, Integer packLevelsCreated,
			List<BulkImportErrorDTO> errors) {
		return BulkImportResponseDTO.builder()
				.status("PARTIAL_SUCCESS")
				.message(String.format("Importación parcial: %d productos, %d niveles. Errores: %d",
						productsCreated, packLevelsCreated, errors.size()))
				.productsCreated(productsCreated)
				.packLevelsCreated(packLevelsCreated)
				.errors(errors)
				.build();
	}

	/**
	 * Construye una respuesta de error (sin datos importados)
	 */
	default BulkImportResponseDTO buildErrorResponse(String errorMessage, List<BulkImportErrorDTO> errors) {
		return BulkImportResponseDTO.builder()
				.status("ERROR")
				.message(errorMessage)
				.productsCreated(0)
				.packLevelsCreated(0)
				.errors(errors)
				.build();
	}
}
