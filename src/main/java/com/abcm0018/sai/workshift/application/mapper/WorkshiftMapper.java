package com.abcm0018.sai.workshift.application.mapper;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.abcm0018.sai.shift.domain.entity.Shift;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.workshift.application.dtos.UpdateWorkshiftRequestDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftResponseDTO;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

/**
 * Mapper para convertir entre Workshift Entity y DTOs
 * MapStruct genera la implementación automáticamente en tiempo de compilación
 */
@Mapper(
		componentModel = "spring",
		unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface WorkshiftMapper {

	/**
	 * Actualiza una entidad existente con datos del UpdateRequestDTO
	 * Solo actualiza campos no nulos
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "shift", ignore = true)
	@Mapping(target = "user", ignore = true)
	@Mapping(target = "timesheets", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateEntityFromRequest(UpdateWorkshiftRequestDTO requestDTO, @MappingTarget Workshift entity);

	/**
	 * Convierte Workshift a WorkshiftResponseDTO (básico)
	 */
	@Mapping(target = "shift", source = "shift")
	@Mapping(target = "user", source = "user")
	@Mapping(target = "version", source = "version")
	WorkshiftResponseDTO toResponse(Workshift workshift);

	/**
	 * Convierte lista de Workshifts a lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> toResponseList(List<Workshift> workshifts);

	@Mapping(target = "name", source = "shiftType.displayName")
	WorkshiftResponseDTO.ShiftInfo toShiftInfo(Shift shift);

	@Mapping(target = "fullName", expression = "java(user.getFullName())")
	WorkshiftResponseDTO.EmployeeInfo toEmployeeInfo(User user);

	/**
	 * Formatea un LocalTime a String HH:mm
	 */
	default String formatTime(LocalTime time) {
		if (time == null) {
			return null;
		}
		return time.format(DateTimeFormatter.ofPattern("HH:mm"));
	}
}