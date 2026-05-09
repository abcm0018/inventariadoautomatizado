package com.abcm0018.sai.shift.application.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.abcm0018.sai.shift.application.dtos.ShiftRequestDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftResponseDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftSummaryDTO;
import com.abcm0018.sai.shift.domain.entity.Shift;

/**
 * Mapper para convertir entre Shift Entity y DTOs
 * MapStruct genera la implementación automáticamente en tiempo de compilación
 */
@Mapper(
		componentModel = "spring",
		unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IShiftMapper {
	/**
	 * Convierte ShiftRequestDTO a Shift Entity
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "active", defaultValue = "true")
	Shift toShift(ShiftRequestDTO requestDTO);

	/**
	 * Actualiza una entidad existente con datos del ShiftRequestDTO.
	 * Solo actualiza campos no nulos.
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateEntityFromRequest(ShiftRequestDTO requestDTO, @MappingTarget Shift entity);

	/**
	 * Convierte Shift a ShiftResponseDTO (respuesta completa)
	 */
	@Mapping(target = "shiftTypeDisplay", expression = "java(shift.getShiftType().getDisplayName())")
	@Mapping(target = "durationHours", expression = "java(shift.getDurationInHours())")
	@Mapping(target = "durationMinutes", expression = "java(shift.getDurationInMinutes())")
	@Mapping(target = "crossesMidnight", expression = "java(shift.crossesMidnight())")
	@Mapping(target = "shiftDescription", expression = "java(shift.getShiftDescription())")
	@Mapping(target = "hasBeenUpdated", expression = "java(shift.hasBeenUpdated())")
	@Mapping(target = "totalWorkshifts", ignore = true)
	@Mapping(target = "canBeDeleted", ignore = true)
	ShiftResponseDTO toResponse(Shift shift);

	/**
	 * Convierte lista de Shifts a lista de ShiftResponseDTO
	 */
	List<ShiftResponseDTO> toResponseList(List<Shift> shifts);

	/**
	 * Convierte Shift a ShiftSummaryDTO (para listados)
	 */
	@Mapping(target = "shiftTypeDisplay", expression = "java(shift.getShiftType().getDisplayName())")
	@Mapping(target = "durationHours", expression = "java(shift.getDurationInHours())")
	@Mapping(target = "shiftDescription", expression = "java(shift.getShiftDescription())")
	ShiftSummaryDTO toSummaryDTO(Shift shift);

	/**
	 * Convierte lista de Shifts a lista de ShiftSummaryDTO
	 */
	List<ShiftSummaryDTO> toSummaryDTOList(List<Shift> shifts);

	/**
	 * Crea ShiftResponseDTO con información adicional de workshifts
	 */
	@Mapping(target = "shiftTypeDisplay", expression = "java(shift.getShiftType().getDisplayName())")
	@Mapping(target = "durationHours", expression = "java(shift.getDurationInHours())")
	@Mapping(target = "durationMinutes", expression = "java(shift.getDurationInMinutes())")
	@Mapping(target = "crossesMidnight", expression = "java(shift.crossesMidnight())")
	@Mapping(target = "shiftDescription", expression = "java(shift.getShiftDescription())")
	@Mapping(target = "hasBeenUpdated", expression = "java(shift.hasBeenUpdated())")
	@Mapping(target = "totalWorkshifts", source = "totalWorkshifts")
	@Mapping(target = "canBeDeleted", source = "canBeDeleted")
	ShiftResponseDTO toResponseWithDetails(Shift shift, Long totalWorkshifts, Boolean canBeDeleted);
}
