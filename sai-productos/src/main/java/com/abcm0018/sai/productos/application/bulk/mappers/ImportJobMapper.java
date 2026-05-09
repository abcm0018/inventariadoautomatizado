package com.abcm0018.sai.productos.application.bulk.mappers;

import com.abcm0018.sai.productos.application.bulk.dtos.BulkImportResponseDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ImportJobResponseDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ImportStatusResponseDTO;
import com.abcm0018.sai.productos.application.bulk.job.ImportJob;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Mapper(componentModel = "spring")
public abstract class ImportJobMapper {

	@Autowired
	protected ObjectMapper objectMapper;

	/**
	 * Mapea la entidad Job a la respuesta DTO de "inicio".
	 * El campo 'message' es dinámico y se debe setear en el servicio.
	 */
	@Mappings({
			@Mapping(source = "jobId", target = "jobId"),
			@Mapping(source = "status", target = "status"),
			@Mapping(target = "message", ignore = true)
	})
	public abstract ImportJobResponseDTO toImportJobResponseDTO(ImportJob job);

	/**
	 * Mapea la entidad Job a la respuesta DTO de "estado".
	 * El campo 'result' se ignora aquí porque se setea en @AfterMapping.
	 */
	@Mappings({
			@Mapping(source = "jobId", target = "jobId"),
			@Mapping(source = "status", target = "status"),
			@Mapping(target = "result", ignore = true) // <-- 3. Ignorado aquí
	})
	public abstract ImportStatusResponseDTO toImportStatusResponseDTO(ImportJob job);

	/**
	 * 4. Lógica de deserialización
	 * Se ejecuta DESPUÉS de toImportStatusResponseDTO()
	 */
	@AfterMapping
	protected void mapResultDetails(ImportJob job, @MappingTarget ImportStatusResponseDTO dto) {
		if (job.getResultDetails() != null && !job.getResultDetails().isBlank()) {
			try {
				// Usamos el ObjectMapper inyectado
				BulkImportResponseDTO resultDTO = objectMapper.readValue(job.getResultDetails(), BulkImportResponseDTO.class);
				dto.setResult(resultDTO);
			} catch (Exception e) {
				log.error("[Job {}] Error al deserializar resultado en Mapper: {}", dto.getJobId(), e.getMessage());
				dto.setResult(null);
			}
		}
	}
}