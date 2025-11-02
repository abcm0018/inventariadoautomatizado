package com.abcm0018.sai.users.application.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.abcm0018.sai.users.application.dtos.*;
import com.abcm0018.sai.users.domain.entity.User;

/**
 * Mapper para convertir entre User Entity y DTOs
 * MapStruct genera la implementación automáticamente en tiempo de compilación
 */
@Mapper(
		componentModel = "spring",
		unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

	/**
	 * Convierte CreateUserRequestDTO a User entity
	 * La contraseña debe ser encriptada por el servicio antes de mapear
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "registrationDate", ignore = true)
	@Mapping(target = "password", ignore = true) // Se encripta en el servicio
	@Mapping(target = "employeeNumber", ignore = true) // Se genera en el servicio
	@Mapping(target = "workshifts", ignore = true)
	@Mapping(target = "scannedPalets", ignore = true)
	User toUser(CreateUserRequestDTO requestDTO);

	/**
	 * Actualiza una entidad existente con datos del UpdateUserRequestDTO
	 * Solo actualiza campos no nulos
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "registrationDate", ignore = true)
	@Mapping(target = "active", ignore = true)
	@Mapping(target = "blocked", ignore = true)
	@Mapping(target = "expired", ignore = true)
	@Mapping(target = "workshifts", ignore = true)
	@Mapping(target = "scannedPalets", ignore = true)
	void updateEntityFromRequest(UpdateUserRequestDTO requestDTO, @MappingTarget User entity);

	/**
	 * Convierte User a UserResponseDTO (respuesta completa)
	 */
	@Mapping(target = "fullName", expression = "java(user.getFullName())")
	@Mapping(target = "roleDisplayName", expression = "java(user.getRole().getDisplayName())")
	@Mapping(target = "canWork", expression = "java(canUserWork(user))")
	@Mapping(target = "accountStatus", expression = "java(getAccountStatus(user))")
	@Mapping(target = "daysSinceRegistration", expression = "java(calculateDaysSinceRegistration(user.getRegistrationDate()))")
	UserResponseDTO toResponse(User user);

	/**
	 * Convierte User a UserSummaryDTO (para listados)
	 */
	@Mapping(target = "fullName", expression = "java(user.getFullName())")
	@Mapping(target = "roleDisplayName", expression = "java(user.getRole().getDisplayName())")
	@Mapping(target = "accountStatus", expression = "java(getAccountStatus(user))")
	UserSummaryDTO toSummary(User user);

	/**
	 * Convierte lista de Users a lista de UserSummaryDTO
	 */
	List<UserSummaryDTO> toSummaryList(List<User> users);

	/**
	 * Convierte User a UserDetailResponseDTO
	 * Las estadísticas deben ser seteadas manualmente después
	 */
	@Mapping(target = "userInfo", source = "user")
	@Mapping(target = "totalWorkshifts", ignore = true)
	@Mapping(target = "totalScannedPalets", ignore = true)
	@Mapping(target = "hasWorkshifts", ignore = true)
	@Mapping(target = "hasScannedPalets", ignore = true)
	@Mapping(target = "hasWorkshiftToday", ignore = true)
	@Mapping(target = "currentWorkshiftInfo", ignore = true)
	UserDetailResponseDTO toDetailResponse(User user);

	/**
	 * Crea UserDetailResponseDTO con información completa
	 */
	@Mapping(target = "userInfo", source = "user")
	@Mapping(target = "totalWorkshifts", source = "totalWorkshifts")
	@Mapping(target = "totalScannedPalets", source = "totalScannedPalets")
	@Mapping(target = "hasWorkshifts", expression = "java(totalWorkshifts > 0)")
	@Mapping(target = "hasScannedPalets", expression = "java(totalScannedPalets > 0)")
	@Mapping(target = "hasWorkshiftToday", source = "hasWorkshiftToday")
	@Mapping(target = "currentWorkshiftInfo", source = "currentWorkshiftInfo")
	UserDetailResponseDTO toDetailResponseWithStats(
			User user,
			Long totalWorkshifts,
			Long totalScannedPalets,
			Boolean hasWorkshiftToday,
			UserDetailResponseDTO.CurrentWorkshiftInfo currentWorkshiftInfo
	);

	/**
	 * Verifica si un usuario puede trabajar
	 * Debe estar activo, no bloqueado, no expirado y ser OPERATOR
	 */
	default Boolean canUserWork(User user) {
		if (user == null) {
			return false;
		}
		return user.isActive() && !user.isBlocked() && !user.isExpired() && user.getRole() != null && user.getRole().name().equals("OPERATOR");
	}

	/**
	 * Determina el estado de la cuenta del usuario
	 */
	default String getAccountStatus(User user) {
		if (user == null) {
			return "UNKNOWN";
		}

		if (user.isBlocked()) {
			return "BLOCKED";
		}

		if (user.isExpired()) {
			return "EXPIRED";
		}

		if (!user.isActive()) {
			return "INACTIVE";
		}

		return "ACTIVE";
	}

	/**
	 * Calcula días desde el registro
	 */
	default Long calculateDaysSinceRegistration(LocalDate registrationDate) {
		if (registrationDate == null) {
			return null;
		}
		return ChronoUnit.DAYS.between(registrationDate, LocalDate.now());
	}

	/**
	 * Formatea el nombre completo del usuario
	 */
	default String formatFullName(String name, String surname) {
		if (name == null && surname == null) {
			return "";
		}
		if (name == null) {
			return surname;
		}
		if (surname == null) {
			return name;
		}
		return name + " " + surname;
	}
}