package com.abcm0018.sai.timesheet.application.mapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.abcm0018.sai.shift.domain.entity.Shift;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetDetailResponseDTO;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetRequestDTO;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetResponseDTO;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetSummaryDTO;
import com.abcm0018.sai.timesheet.domain.entity.Timesheet;
import com.abcm0018.sai.timesheet.domain.enums.TimesheetStatus;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

/**
 * Mapper para convertir entre Timesheet Entity y DTOs
 * MapStruct genera la implementación automáticamente en tiempo de compilación
 */
@Mapper(
		componentModel = "spring",
		unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TimesheetMapper {
	/**
	 * Convierte TimesheetRequestDTO a Timesheet entity
	 * El workshift se mapea por separado en el servicio
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "workshift", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "status", defaultValue = "OPEN")
	Timesheet toEntity(TimesheetRequestDTO requestDTO);

	/**
	 * Actualiza una entidad existente con datos del RequestDTO
	 * Solo actualiza campos no nulos
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "workshift", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateEntityFromRequest(TimesheetRequestDTO requestDTO, @MappingTarget Timesheet entity);

	/**
	 * Convierte Timesheet a TimesheetResponseDTO (respuesta básica)
	 */
	@Mapping(target = "workshiftId", source = "workshift.id")
	@Mapping(target = "userId", source = "workshift.user.id")
	@Mapping(target = "userFullName", expression = "java(getUserFullName(timesheet))")
	@Mapping(target = "workedHours", expression = "java(timesheet.getWorkedHours())")
	@Mapping(target = "workedMinutes", expression = "java(timesheet.getWorkedMinutes())")
	@Mapping(target = "workedDuration", expression = "java(timesheet.getWorkedDuration())")
	@Mapping(target = "isOpen", expression = "java(timesheet.isOpen())")
	@Mapping(target = "isClosed", expression = "java(timesheet.isClosed())")
	@Mapping(target = "isLate", expression = "java(timesheet.isLate(5))")
	@Mapping(target = "lateMinutes", expression = "java(timesheet.getLateMinutes())")
	TimesheetResponseDTO toResponse(Timesheet timesheet);

	/**
	 * Convierte lista de Timesheets a lista de TimesheetResponseDTO
	 */
	List<TimesheetResponseDTO> toResponseList(List<Timesheet> timesheets);

	/**
	 * Convierte Timesheet a TimesheetDetailResponseDTO (respuesta completa)
	 */
	@Mapping(target = "workshift", expression = "java(toWorkshiftInfo(timesheet))")
	@Mapping(target = "user", expression = "java(toUserInfo(timesheet))")
	@Mapping(target = "shift", expression = "java(toShiftInfo(timesheet))")
	@Mapping(target = "workedHours", expression = "java(timesheet.getWorkedHours())")
	@Mapping(target = "workedMinutes", expression = "java(timesheet.getWorkedMinutes())")
	@Mapping(target = "workedDuration", expression = "java(timesheet.getWorkedDuration())")
	@Mapping(target = "isOpen", expression = "java(timesheet.isOpen())")
	@Mapping(target = "isClosed", expression = "java(timesheet.isClosed())")
	@Mapping(target = "isLate", expression = "java(timesheet.isLate(5))")
	@Mapping(target = "lateMinutes", expression = "java(timesheet.getLateMinutes())")
	@Mapping(target = "hasOvertime", expression = "java(timesheet.hasOvertime(1))")
	@Mapping(target = "overtimeHours", expression = "java(timesheet.getOvertimeHours())")
	TimesheetDetailResponseDTO toDetailResponse(Timesheet timesheet);

	/**
	 * Convierte lista de Timesheets a lista de TimesheetDetailResponseDTO
	 */
	List<TimesheetDetailResponseDTO> toDetailResponseList(List<Timesheet> timesheets);

	/**
	 * Convierte Timesheet a TimesheetSummaryDTO (resumen)
	 */
	@Mapping(target = "userFullName", expression = "java(getUserFullName(timesheet))")
	@Mapping(target = "workedDuration", expression = "java(timesheet.getWorkedDuration())")
	@Mapping(target = "isOpen", expression = "java(timesheet.isOpen())")
	TimesheetSummaryDTO toSummary(Timesheet timesheet);

	/**
	 * Convierte lista de Timesheets a lista de TimesheetSummaryDTO
	 */
	List<TimesheetSummaryDTO> toSummaryList(List<Timesheet> timesheets);

	/**
	 * Obtiene el nombre completo del usuario desde Timesheet
	 */
	default String getUserFullName(Timesheet timesheet) {
		if (timesheet == null || timesheet.getWorkshift() == null || timesheet.getWorkshift().getUser() == null) {
			return null;
		}
		return timesheet.getWorkshift().getUser().getFullName();
	}

	/**
	 * Crea WorkshiftInfo desde Timesheet
	 */
	default TimesheetDetailResponseDTO.WorkshiftInfo toWorkshiftInfo(Timesheet timesheet) {
		if (timesheet == null || timesheet.getWorkshift() == null) {
			return null;
		}

		Workshift workshift = timesheet.getWorkshift();

		return TimesheetDetailResponseDTO.WorkshiftInfo.builder()
				.id(workshift.getId())
				.date(workshift.getDate())
				.build();
	}

	/**
	 * Crea UserInfo desde Timesheet
	 */
	default TimesheetDetailResponseDTO.UserInfo toUserInfo(Timesheet timesheet) {
		if (timesheet == null || timesheet.getWorkshift() == null || timesheet.getWorkshift().getUser() == null) {
			return null;
		}

		User user = timesheet.getWorkshift().getUser();

		return TimesheetDetailResponseDTO.UserInfo.builder()
				.id(user.getId())
				.employeeNumber(user.getEmployeeNumber())
				.fullName(user.getFullName())
				.email(user.getEmail())
				.jobPosition(user.getJobPosition())
				.build();
	}

	/**
	 * Crea ShiftInfo desde Timesheet
	 */
	default TimesheetDetailResponseDTO.ShiftInfo toShiftInfo(Timesheet timesheet) {
		if (timesheet == null || timesheet.getWorkshift() == null || timesheet.getWorkshift().getShift() == null) {
			return null;
		}

		Shift shift = timesheet.getWorkshift().getShift();

		return TimesheetDetailResponseDTO.ShiftInfo.builder()
				.id(shift.getId())
				.shiftType(shift.getShiftType() != null ? shift.getShiftType().getDisplayName() : null)
				.description(shift.getDescription())
				.startTime(shift.getStartTime() != null ? shift.getStartTime().toString() : null)
				.endTime(shift.getEndTime() != null ? shift.getEndTime().toString() : null)
				.build();
	}

	/**
	 * Calcula si el usuario llegó tarde (más de X minutos)
	 */
	default Boolean isLate(Timesheet timesheet, int toleranceMinutes) {
		if (timesheet == null) {
			return false;
		}
		return timesheet.isLate(toleranceMinutes);
	}

	/**
	 * Calcula los minutos de tardanza
	 */
	default Long calculateLateMinutes(Timesheet timesheet) {
		if (timesheet == null) {
			return 0L;
		}
		return timesheet.getLateMinutes();
	}

	/**
	 * Calcula las horas trabajadas
	 */
	default Long calculateWorkedHours(Timesheet timesheet) {
		if (timesheet == null) {
			return null;
		}
		return timesheet.getWorkedHours();
	}

	/**
	 * Calcula los minutos trabajados
	 */
	default Long calculateWorkedMinutes(Timesheet timesheet) {
		if (timesheet == null) {
			return null;
		}
		return timesheet.getWorkedMinutes();
	}

	/**
	 * Obtiene la duración trabajada formateada
	 */
	default String getFormattedWorkedDuration(Timesheet timesheet) {
		if (timesheet == null) {
			return null;
		}
		return timesheet.getWorkedDuration();
	}

	/**
	 * Verifica si tiene horas extras
	 */
	default Boolean hasOvertime(Timesheet timesheet, long threshold) {
		if (timesheet == null) {
			return false;
		}
		return timesheet.hasOvertime(threshold);
	}

	/**
	 * Calcula las horas extras trabajadas
	 */
	default Long calculateOvertimeHours(Timesheet timesheet) {
		if (timesheet == null) {
			return 0L;
		}
		return timesheet.getOvertimeHours();
	}

	/**
	 * Determina el estado del timesheet basado en anomalías
	 */
	default TimesheetStatus determineStatus(Timesheet timesheet) {
		if (timesheet == null || timesheet.getCheckInAt() == null) {
			return TimesheetStatus.OPEN;
		}

		// Si ya está cerrado, mantener el estado actual
		if (timesheet.getCheckOutAt() != null) {
			return timesheet.getStatus() != null ? timesheet.getStatus() : TimesheetStatus.CLOSED;
		}

		// Verificar si hay anomalías
		boolean isLate = timesheet.isLate(5); // 5 minutos de tolerancia
		boolean isOverdue = false;

		// Verificar si lleva más de 12 horas sin cerrar (posible anomalía)
		if (timesheet.getCheckInAt() != null) {
			Duration duration = Duration.between(timesheet.getCheckInAt(), LocalDateTime.now());
			isOverdue = duration.toHours() > 12;
		}

		if (isLate || isOverdue) {
			return TimesheetStatus.ANOMALY;
		}

		return TimesheetStatus.OPEN;
	}

	/**
	 * Crea un TimesheetResponseDTO con información completa calculada
	 */
	@Mapping(target = "workshiftId", source = "timesheet.workshift.id")
	@Mapping(target = "userId", source = "timesheet.workshift.user.id")
	@Mapping(target = "userFullName", expression = "java(getUserFullName(timesheet))")
	@Mapping(target = "checkInAt", source = "timesheet.checkInAt")
	@Mapping(target = "checkOutAt", source = "timesheet.checkOutAt")
	@Mapping(target = "notes", source = "timesheet.notes")
	@Mapping(target = "status", source = "timesheet.status")
	@Mapping(target = "workedHours", expression = "java(workedHours)")
	@Mapping(target = "workedMinutes", expression = "java(workedMinutes)")
	@Mapping(target = "workedDuration", expression = "java(workedDuration)")
	@Mapping(target = "isOpen", expression = "java(isOpen)")
	@Mapping(target = "isClosed", expression = "java(isClosed)")
	@Mapping(target = "isLate", expression = "java(isLate)")
	@Mapping(target = "lateMinutes", expression = "java(lateMinutes)")
	@Mapping(target = "createdAt", source = "timesheet.createdAt")
	@Mapping(target = "updatedAt", source = "timesheet.updatedAt")
	TimesheetResponseDTO toResponseWithCalculations(
			Timesheet timesheet,
			Long workedHours,
			Long workedMinutes,
			String workedDuration,
			Boolean isOpen,
			Boolean isClosed,
			Boolean isLate,
			Long lateMinutes);
}
