package com.abcm0018.sai.palets.application.mappers;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.abcm0018.sai.palets.application.dtos.CreatePaletRequestDTO;
import com.abcm0018.sai.palets.application.dtos.PaletDetailResponseDTO;
import com.abcm0018.sai.palets.application.dtos.PaletNotificationDTO;
import com.abcm0018.sai.palets.application.dtos.PaletResponseDTO;
import com.abcm0018.sai.palets.application.dtos.PaletSummaryResponseDTO;
import com.abcm0018.sai.palets.application.dtos.UpdatePaletRequestDTO;
import com.abcm0018.sai.palets.domain.entity.Palet;
import com.abcm0018.sai.productos.domain.entity.Product;
import com.abcm0018.sai.productos.domain.entity.ProductPackLevel;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

/**
 * Mapper para convertir entre Palet Entity y DTOs
 * <p>
 * ARQUITECTURA:
 * - Mapea directamente desde ProductPackLevel (no desde Product)
 * - Extrae información del Producto a través de ProductPackLevel
 * - MapStruct genera la implementación automáticamente en tiempo de compilación
 * - Usa @AfterMapping para enriquecimiento post-mapeo (consistente con ProductPackLevelMapper)
 */
@Mapper(
		componentModel = "spring",
		unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PaletMapper {

	/**
	 * Días para considerar un palet como próximo a caducar (WARNING)
	 * Valor sincronizado con ExpiryHelper.EXPIRY_WARNING_DAYS
	 */
	int EXPIRY_WARNING_DAYS = 7;

	/**
	 * Días para considerar un palet en estado crítico de caducidad
	 * Valor sincronizado con ExpiryHelper.CRITICAL_EXPIRY_DAYS
	 */
	int CRITICAL_EXPIRY_DAYS = 3;

	/**
	 * Convierte CreatePaletRequestDTO a Palet entity
	 * <p>
	 * NOTA: packLevelId reemplaza productId (relación especifica a nivel de embalaje)
	 * Las relaciones (productPackLevel, user, workshift) se asignan en el Service
	 * @param requestDTO datos de creación del cliente
	 * @return entidad Palet sin persistir (relaciones null)
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "productPackLevel", ignore = true)
	@Mapping(target = "user", ignore = true)
	@Mapping(target = "workshift", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	Palet toPalet(CreatePaletRequestDTO requestDTO);

	/**
	 * Actualiza una entidad existente con datos del UpdateRequestDTO
	 * <p>
	 * RESTRICCIONES: productPackLevel NO es modificable (relación inmutable)
	 * Solo se pueden actualizar campos sin dependencias críticas
	 *
	 * @param requestDTO datos de actualización (parcial)
	 * @param entity entidad a actualizar (será modificada)
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "productPackLevel", ignore = true)
	@Mapping(target = "user", ignore = true)
	@Mapping(target = "workshift", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateEntityFromRequest(UpdatePaletRequestDTO requestDTO, @MappingTarget Palet entity);

	// ========== ENTITY → RESPONSE (Mappeos básicos) ==========

	/**
	 * Convierte Palet a PaletResponseDTO (respuesta básica)
	 * <p>
	 * Accede a información del Producto a través de ProductPackLevel
	 * Incluye información del operador y turno
	 *
	 * @param palet entidad persistida
	 * @return DTO con información esencial + caducidad
	 */
	@Mapping(target = "packLevelId", source = "productPackLevel.id")
	@Mapping(target = "gtin", source = "productPackLevel.gtin")
	@Mapping(target = "productId", source = "productPackLevel.product.id")
	@Mapping(target = "productName", source = "productPackLevel.product.name")
	@Mapping(target = "productBrand", source = "productPackLevel.product.brand")
	@Mapping(target = "userId", source = "user.id")
	@Mapping(target = "employeeNumber", source = "user.employeeNumber")
	@Mapping(target = "userFullName", expression = "java(getUserFullName(palet))")
	@Mapping(target = "workshiftId", source = "workshift.id")
	@Mapping(target = "workshiftDate", source = "workshift.date")
	@Mapping(target = "productionTime", source = "productionTime")
	@Mapping(target = "shiftType", source = "workshift.shift.shiftType")
	@Mapping(target = "shiftDisplayName", source = "workshift.shift.shiftType.displayName")
	PaletResponseDTO toResponse(Palet palet);

	/**
	 * Enriquece PaletResponseDTO con cálculos de caducidad
	 * <p>
	 * Se ejecuta automáticamente después de toResponse()
	 * Centraliza lógica de caducidad para evitar duplicación
	 */
	@AfterMapping
	default void enrichResponse(Palet entity, @MappingTarget PaletResponseDTO dto) {
		if (entity != null && dto != null) {
			ExpiryInfo expiryInfo = calculateExpiryInfo(entity.getProductUseByDate());
			applyExpiryInfoToResponse(dto, expiryInfo);
		}
	}

	/**
	 * Convierte lista de Palets a lista de PaletResponseDTO
	 *
	 * @param palets lista de entidades
	 * @return lista de DTOs de respuesta
	 */
	List<PaletResponseDTO> toResponseList(List<Palet> palets);

	/**
	 * Convierte Palet a PaletDetailResponseDTO (respuesta completa con relaciones anidadas)
	 * <p>
	 * Incluye 4 niveles de relaciones:
	 * - PackLevelInfo: Detalles del nivel de embalaje (9 campos)
	 * - ProductInfo: Detalles del producto base (6 campos)
	 * - UserInfo: Información del operador (5 campos)
	 * - WorkshiftInfo: Información del turno (4 campos)
	 *
	 * @param palet entidad persistida
	 * @return DTO con todas las relaciones enriquecidas
	 */
	@Mapping(target = "packLevel", expression = "java(toPackLevelInfo(palet.getProductPackLevel()))")
	@Mapping(target = "product", expression = "java(toProductInfo(palet.getProductPackLevel().getProduct()))")
	@Mapping(target = "scannedBy", expression = "java(toUserInfo(palet))")
	@Mapping(target = "workshift", expression = "java(toWorkshiftInfo(palet))")
	PaletDetailResponseDTO toResponseDetail(Palet palet);

	/**
	 * Enriquece PaletDetailResponseDTO con información de caducidad completa
	 * <p>
	 * Se ejecuta automáticamente después de toResponseDetail()
	 * Incluye estado crítico de caducidad
	 */
	@AfterMapping
	default void enrichDetailResponse(Palet entity, @MappingTarget PaletDetailResponseDTO dto) {
		if (entity != null && dto != null) {
			ExpiryInfo expiryInfo = calculateExpiryInfo(entity.getProductUseByDate());
			applyExpiryInfoToDetailResponse(dto, expiryInfo);
		}
	}

	/**
	 * Convierte Palet a PaletSummaryResponseDTO (respuesta resumida para listados)
	 * <p>
	 * Información mínima optimizada para paginación
	 * Sin anidamientos complejos (mejor performance)
	 *
	 * @param palet entidad persistida
	 * @return DTO con información esencial para listados
	 */
	@Mapping(target = "employeeNumber", source = "user.employeeNumber")
	@Mapping(target = "shift", source = "workshift.shift.shiftType.displayName")
	@Mapping(target = "gtin", source = "productPackLevel.gtin")
	@Mapping(target = "productName", source = "productPackLevel.product.name")
	@Mapping(target = "productBrand", source = "productPackLevel.product.brand")
	PaletSummaryResponseDTO toResponseSummary(Palet palet);

	/**
	 * Enriquece PaletSummaryResponseDTO con información de caducidad
	 * <p>
	 * Se ejecuta automáticamente después de toResponseSummary()
	 * Incluye todos los campos de estado de caducidad (crucial para UI)
	 */
	@AfterMapping
	default void enrichSummaryResponse(Palet entity, @MappingTarget PaletSummaryResponseDTO dto) {
		if (entity != null && dto != null) {
			ExpiryInfo expiryInfo = calculateExpiryInfo(entity.getProductUseByDate());
			applyExpiryInfoToSummaryResponse(dto, expiryInfo);
		}
	}

	/**
	 * Convierte lista de Palets a lista de PaletSummaryResponseDTO
	 *
	 * @param palets lista de entidades
	 * @return lista de DTOs resumidos
	 */
	List<PaletSummaryResponseDTO> toSummaryResponseList(List<Palet> palets);

	/**
	 * Convierte una entidad Palet al DTO específico para notificaciones WebSocket.
	 * <p>
	 * Extrae toda la información enriquecida necesaria para
	 * la UI del dashboard en tiempo real, coincidiendo con la lógica
	 * que estaba originalmente en PaletEventListener.
	 *
	 * @param palet La entidad Palet completa (con relaciones cargadas)
	 * @return DTO de notificación para WebSocket
	 */
	@Mapping(target = "gtin", source = "productPackLevel.gtin")
	@Mapping(target = "productSku", source = "productPackLevel.product.formatCode")
	@Mapping(target = "productName", source = "productPackLevel.product.name")
	@Mapping(target = "brand", source = "productPackLevel.product.brand")
	@Mapping(target = "packLevel", source = "productPackLevel.packingLevel")
	@Mapping(target = "unitsInLevel", source = "productPackLevel.unitsInLevel")
	@Mapping(target = "grossWeightKg", source = "productPackLevel.netWeight")
	@Mapping(target = "stackingLimit", source = "productPackLevel.stackingLimit")
	@Mapping(target = "employeeName", source = "user.fullName")
	@Mapping(target = "shiftType", source = "workshift.shift.shiftType")
	@Mapping(target = "dimensionsMm", ignore = true)
	@Mapping(target = "isExpired", ignore = true)
	PaletNotificationDTO toResponsePaletNotification(Palet palet);

	/**
	 * Value Object para encapsular información de caducidad
	 * <p>
	 * Elimina duplicación al pasar múltiples valores entre métodos Facilita testing y mantenimiento
	 */
	record ExpiryInfo(Long daysUntilExpiry, boolean isExpired, boolean isExpiringSoon, boolean isCritical, String status) {
	}

	/**
	 * Calcula TODA la información de caducidad en UN ÚNICO LUGAR
	 * <p>
	 * VENTAJA DRY: Un solo cálculo, reutilizado en todos los mapeos
	 * - Elimina duplicación de lógica
	 * - Fácil de testear
	 * - Si cambia lógica, se actualiza en UN lugar
	 *
	 * @param expiryDate fecha de caducidad
	 * @return Value Object ExpiryInfo con TODOS los cálculos
	 */
	default ExpiryInfo calculateExpiryInfo(LocalDate expiryDate) {
		if (expiryDate == null) {
			return new ExpiryInfo(null, false, false, false, "UNKNOWN");
		}

		long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
		boolean isExpiredFlag = daysUntilExpiry < 0;
		boolean isCriticalFlag = daysUntilExpiry <= CRITICAL_EXPIRY_DAYS && daysUntilExpiry > 0;
		boolean isExpiringFlag = daysUntilExpiry <= EXPIRY_WARNING_DAYS && daysUntilExpiry > 0;
		String statusFlag = determineExpiryStatus(daysUntilExpiry, isExpiredFlag, isCriticalFlag, isExpiringFlag);

		return new ExpiryInfo(daysUntilExpiry, isExpiredFlag, isExpiringFlag, isCriticalFlag, statusFlag);
	}

	/**
	 * Enriquece el PaletNotificationDTO con campos calculados (dimensiones y caducidad).
	 * MapStruct ejecutará esto automáticamente después de toResponsePaletNotification.
	 */
	@AfterMapping
	default void enrichNotificationDTO(Palet palet, @MappingTarget PaletNotificationDTO dto) {
		if (palet == null || dto == null) {
			return;
		}

		// 1. Calcular Caducidad (tomado de la lógica de Palet.java)
		dto.setExpired(palet.isExpired());

		// 2. Calcular Dimensiones (tomado de la lógica de PaletEventListener)
		ProductPackLevel packLevel = palet.getProductPackLevel(); //
		if (packLevel != null && packLevel.getWidthMM() != null && packLevel.getHeightMM() != null) {
			String dimensions = String.format("%dx%d", packLevel.getWidthMM().intValue(), packLevel.getHeightMM().intValue());
			dto.setDimensionsMm(dimensions); //
		}
	}

	/**
	 * Determina el estado textual de caducidad
	 * <p>
	 * PARÁMETROS OPTIMIZADOS: Recibe boolean precalculados
	 */
	default String determineExpiryStatus(Long daysUntilExpiry, boolean isExpired, boolean isCritical, boolean isExpiring) {
		if (daysUntilExpiry == null) {
			return "UNKNOWN";
		}
		if (isExpired) {
			return "EXPIRED";
		}
		if (isCritical) {
			return "CRITICAL";
		}
		if (isExpiring) {
			return "WARNING";
		}
		return "FRESH";
	}

	/**
	 * Aplica ExpiryInfo a PaletResponseDTO
	 * <p>
	 * Evita duplicación de setters
	 */
	default void applyExpiryInfoToResponse(PaletResponseDTO dto, ExpiryInfo info) {
		if (dto != null && info != null) {
			dto.setDaysUntilExpiry(info.daysUntilExpiry);
			dto.setIsExpiringSoon(info.isExpiringSoon);
			dto.setIsExpired(info.isExpired);
		}
	}

	/**
	 * Aplica ExpiryInfo a PaletDetailResponseDTO
	 * <p>
	 * Incluye campo adicional isCriticalExpiry
	 */
	default void applyExpiryInfoToDetailResponse(PaletDetailResponseDTO dto, ExpiryInfo info) {
		if (dto != null && info != null) {
			dto.setDaysUntilExpiry(info.daysUntilExpiry);
			dto.setIsExpiringSoon(info.isExpiringSoon);
			dto.setIsExpired(info.isExpired);
			dto.setIsCriticalExpiry(info.isCritical);
			dto.setExpiryStatus(info.status);
		}
	}

	/**
	 * Aplica ExpiryInfo a PaletSummaryResponseDTO
	 * <p>
	 * Incluye todos los campos de estado
	 */
	default void applyExpiryInfoToSummaryResponse(PaletSummaryResponseDTO dto, ExpiryInfo info) {
		if (dto != null && info != null) {
			dto.setDaysUntilExpiry(info.daysUntilExpiry);
			dto.setIsExpiringSoon(info.isExpiringSoon);
			dto.setIsExpired(info.isExpired);
			dto.setExpiryStatus(info.status);
		}
	}

	/**
	 * Extrae el nombre completo del usuario de forma defensiva
	 *
	 * @param palet entidad con relación a usuario
	 * @return nombre completo del usuario o null si no existe
	 */
	default String getUserFullName(Palet palet) {
		if (palet == null || palet.getUser() == null) {
			return null;
		}
		return palet.getUser().getFullName();
	}

	/**
	 * Crea PackLevelInfo desde ProductPackLevel (9 campos de embalaje)
	 * <p>
	 * Información completa del nivel de embalaje:
	 * - Identificadores (id, gtin)
	 * - Tipo de embalaje (packingLevel)
	 * - Medidas (netWeight, heightMM, widthMM)
	 * - Configuración (unitsInLevel, stackingLimit, boxesPerPalet)
	 *
	 * @param packLevel entidad ProductPackLevel
	 * @return PackLevelInfo enriquecida o null
	 */
	default PaletDetailResponseDTO.PackLevelInfo toPackLevelInfo(ProductPackLevel packLevel) {
		if (packLevel == null) {
			return null;
		}

		return PaletDetailResponseDTO.PackLevelInfo.builder()
				.id(packLevel.getId())
				.gtin(packLevel.getGtin())
				.packingLevel(packLevel.getPackingLevel() != null ? packLevel.getPackingLevel().getDisplayName() : null)
				.netWeight(packLevel.getNetWeight())
				.heightMM(packLevel.getHeightMM())
				.widthMM(packLevel.getWidthMM())
				.unitsInLevel(packLevel.getUnitsInLevel())
				.stackingLimit(packLevel.getStackingLimit())
				.boxesPerPalet(packLevel.getBoxesPerPalet())
				.build();
	}

	/**
	 * Crea ProductInfo desde Product (6 campos de producto)
	 * <p>
	 * Información del producto base (no del embalaje):
	 * - Identificadores (id)
	 * - Descripción (name, brand, description)
	 * - Clasificación (formatCode, manufacturedIn, status)
	 *
	 * @param product entidad Product
	 * @return ProductInfo enriquecida o null
	 */
	default PaletDetailResponseDTO.ProductInfo toProductInfo(Product product) {
		if (product == null) {
			return null;
		}

		return PaletDetailResponseDTO.ProductInfo.builder()
				.id(product.getId())
				.name(product.getName())
				.brand(product.getBrand())
				.description(product.getDescription())
				.formatCode(product.getFormatCode())
				.manufacturedIn(product.getManufacturedIn())
				.status(product.getStatus() != null ? product.getStatus().getDisplayName() : null)
				.build();
	}

	/**
	 * Crea UserInfo desde Palet (5 campos de usuario)
	 * <p>
	 * Información del operador que escaneó el palet:
	 * - Identificadores (id, employeeNumber)
	 * - Datos personales (fullName, email)
	 * - Posición (jobPosition)
	 *
	 * @param palet entidad con relación a usuario
	 * @return UserInfo enriquecida o null
	 */
	default PaletDetailResponseDTO.UserInfo toUserInfo(Palet palet) {
		if (palet == null || palet.getUser() == null) {
			return null;
		}

		User user = palet.getUser();
		return PaletDetailResponseDTO.UserInfo.builder()
				.id(user.getId())
				.employeeNumber(user.getEmployeeNumber())
				.fullName(user.getFullName())
				.email(user.getEmail())
				.jobPosition(user.getJobPosition())
				.build();
	}

	/**
	 * Crea WorkshiftInfo desde Palet (4 campos de turno)
	 * <p>
	 * Información del turno de producción:
	 * - Identificadores (id)
	 * - Fechas (date)
	 * - Descripción (shiftType, shiftDescription)
	 *
	 * @param palet entidad con relación a turno
	 * @return WorkshiftInfo enriquecida o null
	 */
	default PaletDetailResponseDTO.WorkshiftInfo toWorkshiftInfo(Palet palet) {
		if (palet == null || palet.getWorkshift() == null) {
			return null;
		}

		Workshift workshift = palet.getWorkshift();
		return PaletDetailResponseDTO.WorkshiftInfo.builder()
				.id(workshift.getId())
				.date(workshift.getDate())
				.shiftType(workshift.getShift() != null ? workshift.getShift().getShiftType().getDisplayName() : null)
				.shiftDescription(workshift.getShift() != null ? workshift.getShift().getDescription() : null)
				.build();
	}
}