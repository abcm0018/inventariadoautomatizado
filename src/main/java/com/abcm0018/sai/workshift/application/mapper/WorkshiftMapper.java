package com.abcm0018.sai.workshift.application.mapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.abcm0018.sai.shift.domain.entity.Shift;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.workshift.application.dtos.CreateWorkshiftRequestDTO;
import com.abcm0018.sai.workshift.application.dtos.UpdateWorkshiftRequestDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftDetailResponseDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftResponseDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftSummaryDTO;
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
	 * Convierte CreateWorkshiftRequestDTO a Workshift entity
	 * Los identificadores de las relaciones se mapean por separado
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "shift", ignore = true)
	@Mapping(target = "user", ignore = true)
	@Mapping(target = "timesheets", ignore = true)
	@Mapping(target = "palets", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	Workshift toWorkshift(CreateWorkshiftRequestDTO requestDTO);

	/**
	 * Actualiza una entidad existente con datos del UpdateRequestDTO
	 * Solo actualiza campos no nulos
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "shift", ignore = true)
	@Mapping(target = "user", ignore = true)
	@Mapping(target = "timesheets", ignore = true)
	@Mapping(target = "palets", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateEntityFromRequest(UpdateWorkshiftRequestDTO requestDTO, @MappingTarget Workshift entity);

	/**
	 * Convierte Workshift a WorkshiftResponseDTO (básico)
	 */
	@Mapping(target = "shiftId", source = "shift.id")
	@Mapping(target = "shiftType", expression = "java(getShiftTypeName(workshift))")
	@Mapping(target = "shiftDescription", source = "shift.description")
	@Mapping(target = "startTime", expression = "java(formatTime(workshift.getShift().getStartTime()))")
	@Mapping(target = "endTime", expression = "java(formatTime(workshift.getShift().getEndTime()))")
	@Mapping(target = "userId", source = "user.id")
	@Mapping(target = "employeeNumber", source = "user.employeeNumber")
	@Mapping(target = "userFullName", expression = "java(workshift.getUser().getFullName())")
	@Mapping(target = "jobPosition", source = "user.jobPosition")
	@Mapping(target = "totalScannedPalets", expression = "java(workshift.getTotalScannedPalets())")
	@Mapping(target = "totalTimesheets", expression = "java(workshift.getTotalTimesheets())")
	@Mapping(target = "isToday", expression = "java(workshift.isToday())")
	@Mapping(target = "isPast", expression = "java(workshift.isPast())")
	@Mapping(target = "isFuture", expression = "java(workshift.isFuture())")
	@Mapping(target = "hasBeenUpdated", expression = "java(workshift.hasBeenUpdated())")
	WorkshiftResponseDTO toResponse(Workshift workshift);

	/**
	 * Convierte lista de Workshifts a lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> toResponseList(List<Workshift> workshifts);

	/**
	 * Convierte Workshift a WorkshiftSummaryDTO (resumido)
	 */
	@Mapping(target = "shiftType", expression = "java(getShiftTypeName(workshift))")
	@Mapping(target = "userFullName", expression = "java(workshift.getUser().getFullName())")
	@Mapping(target = "employeeNumber", source = "user.employeeNumber")
	@Mapping(target = "isToday", expression = "java(workshift.isToday())")
	@Mapping(target = "isPast", expression = "java(workshift.isPast())")
	@Mapping(target = "totalScannedPalets", expression = "java(workshift.getTotalScannedPalets())")
	WorkshiftSummaryDTO toSummary(Workshift workshift);

	/**
	 * Convierte lista de Workshifts a lista de WorkshiftSummaryDTO
	 */
	List<WorkshiftSummaryDTO> toSummaryList(List<Workshift> workshifts);

	/**
	 * Convierte Workshift a WorkshiftDetailResponseDTO (completo con información anidada)
	 */
	@Mapping(target = "shift", expression = "java(toShiftInfo(workshift))")
	@Mapping(target = "user", expression = "java(toUserInfo(workshift))")
	@Mapping(target = "totalScannedPalets", expression = "java(workshift.getTotalScannedPalets())")
	@Mapping(target = "totalTimesheets", expression = "java(workshift.getTotalTimesheets())")
	@Mapping(target = "fullDescription", expression = "java(workshift.getFullDescription())")
	@Mapping(target = "isToday", expression = "java(workshift.isToday())")
	@Mapping(target = "isPast", expression = "java(workshift.isPast())")
	@Mapping(target = "isFuture", expression = "java(workshift.isFuture())")
	@Mapping(target = "hasBeenUpdated", expression = "java(workshift.hasBeenUpdated())")
	WorkshiftDetailResponseDTO toDetailResponse(Workshift workshift);

	/**
	 * Convierte lista de Workshifts a lista de WorkshiftDetailResponseDTO
	 */
	List<WorkshiftDetailResponseDTO> toDetailResponseList(List<Workshift> workshifts);

	/**
	 * Obtiene el nombre del tipo de turno
	 */
	default String getShiftTypeName(Workshift workshift) {
		if (workshift.getShift() == null || workshift.getShift().getShiftType() == null) {
			return null;
		}
		return workshift.getShift().getShiftType().getDisplayName();
	}

	/**
	 * Formatea un LocalTime a String HH:mm
	 */
	default String formatTime(java.time.LocalTime time) {
		if (time == null) {
			return null;
		}
		return time.format(DateTimeFormatter.ofPattern("HH:mm"));
	}

	/**
	 * Crea ShiftInfo desde Workshift
	 */
	default WorkshiftDetailResponseDTO.ShiftInfo toShiftInfo(Workshift workshift) {
		if (workshift.getShift() == null) {
			return null;
		}

		Shift shift = workshift.getShift();

		return WorkshiftDetailResponseDTO.ShiftInfo.builder()
				.id(shift.getId())
				.shiftType(shift.getShiftType() != null ? shift.getShiftType().getDisplayName() : null)
				.startTime(shift.getStartTime())
				.endTime(shift.getEndTime())
				.description(shift.getDescription())
				.isActive(shift.isActive())
				.durationHours(shift.getDurationInHours())
				.crossesMidnight(shift.crossesMidnight())
				.build();
	}

	/**
	 * Crea UserInfo desde Workshift
	 */
	default WorkshiftDetailResponseDTO.UserInfo toUserInfo(Workshift workshift) {
		if (workshift.getUser() == null) {
			return null;
		}

		User user = workshift.getUser();

		return WorkshiftDetailResponseDTO.UserInfo.builder()
				.id(user.getId())
				.employeeNumber(user.getEmployeeNumber())
				.fullName(user.getFullName())
				.email(user.getEmail())
				.jobPosition(user.getJobPosition())
				.role(user.getRole() != null ? user.getRole().name() : null)
				.isActive(user.isActive())
				.build();
	}

	/**
	 * Verifica si un workshift es de hoy
	 */
	default Boolean isToday(LocalDate date) {
		if (date == null) {
			return false;
		}
		return date.equals(LocalDate.now());
	}

	/**
	 * Verifica si un workshift es pasado
	 */
	default Boolean isPast(LocalDate date) {
		if (date == null) {
			return false;
		}
		return date.isBefore(LocalDate.now());
	}

	/**
	 * Verifica si un workshift es futuro
	 */
	default Boolean isFuture(LocalDate date) {
		if (date == null) {
			return false;
		}
		return date.isAfter(LocalDate.now());
	}
}