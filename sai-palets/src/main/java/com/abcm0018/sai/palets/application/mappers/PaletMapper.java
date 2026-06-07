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
 * Mapper MapStruct entre la entidad {@link Palet} y sus DTOs de respuesta.
 * <p>
 * El acceso al producto siempre se resuelve a través de {@code ProductPackLevel},
 * ya que un {@code Palet} está vinculado a un nivel de embalaje concreto, no al
 * producto directamente. La información del operador y el turno vive en
 * {@code PalletScan}: se accede mediante {@code latestScan}, que es el primer
 * elemento de la colección {@code scans} ordenada por {@code scannedAt DESC}.
 */
@Mapper(
		componentModel = "spring",
		unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PaletMapper {

	/** Umbral de aviso de caducidad, sincronizado con {@code ExpiryHelper.EXPIRY_WARNING_DAYS}. */
	int EXPIRY_WARNING_DAYS = 7;

	/** Umbral crítico de caducidad, sincronizado con {@code ExpiryHelper.CRITICAL_EXPIRY_DAYS}. */
	int CRITICAL_EXPIRY_DAYS = 3;

	/**
	 * Convierte un {@link CreatePaletRequestDTO} en una entidad {@link Palet} sin persistir.
	 * <p>
	 * Las relaciones {@code productPackLevel} y {@code scans} se asignan en el servicio
	 * tras la validación, por lo que se ignoran aquí.
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "productPackLevel", ignore = true)
	@Mapping(target = "scans", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	Palet toPalet(CreatePaletRequestDTO requestDTO);

	/**
	 * Actualiza en sitio una entidad {@link Palet} con los campos del {@link UpdatePaletRequestDTO}.
	 * <p>
	 * {@code productPackLevel} es inmutable tras la creación del palet y se ignora aquí.
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "productPackLevel", ignore = true)
	@Mapping(target = "scans", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateEntityFromRequest(UpdatePaletRequestDTO requestDTO, @MappingTarget Palet entity);

	// ========== ENTITY → RESPONSE (Mappeos básicos) ==========

	/**
	 * Convierte {@link Palet} a {@link PaletResponseDTO} con información esencial y caducidad.
	 * <p>
	 * El operador y el turno se resuelven desde {@code latestScan}. Los campos de caducidad
	 * se calculan en {@link #enrichResponse} mediante {@code @AfterMapping}.
	 */
	@Mapping(target = "packLevelId", source = "productPackLevel.id")
	@Mapping(target = "gtin", source = "productPackLevel.gtin")
	@Mapping(target = "productId", source = "productPackLevel.product.id")
	@Mapping(target = "productName", source = "productPackLevel.product.name")
	@Mapping(target = "productBrand", source = "productPackLevel.product.brand")
	@Mapping(target = "scannedAt",        source = "latestScan.scannedAt")
	@Mapping(target = "userId",           source = "latestScan.workshift.user.id")
	@Mapping(target = "employeeNumber",   source = "latestScan.workshift.user.employeeNumber")
	@Mapping(target = "userFullName", expression = "java(getUserFullName(palet))")
	@Mapping(target = "workshiftId",      source = "latestScan.workshift.id")
	@Mapping(target = "workshiftDate",    source = "latestScan.workshift.date")
	@Mapping(target = "shiftType",          source = "latestScan.workshift.shift.shiftType")
	@Mapping(target = "shiftDisplayName",   source = "latestScan.workshift.shift.shiftType.displayName")
	PaletResponseDTO toResponse(Palet palet);

	/**
	 * Calcula y aplica los campos de caducidad al builder ANTES de que se llame a build().
	 * Se usa el tipo builder como @MappingTarget para que MapStruct lo invoque correctamente
	 * cuando el DTO se construye con el patrón Lombok @Builder.
	 */
	@AfterMapping
	default void enrichResponse(Palet entity, @MappingTarget PaletResponseDTO.PaletResponseDTOBuilder dto) {
		if (entity == null || dto == null) return;
		ExpiryInfo info = calculateExpiryInfo(entity.getProductUseByDate());
		dto.daysUntilExpiry(info.daysUntilExpiry())
		   .isExpiringSoon(info.isExpiringSoon())
		   .isExpired(info.isExpired());
	}

	List<PaletResponseDTO> toResponseList(List<Palet> palets);

	/**
	 * Convierte {@link Palet} a {@link PaletDetailResponseDTO} con todas las relaciones anidadas:
	 * nivel de embalaje, producto, operador y turno.
	 * <p>
	 * {@code packagingDateTime} se descompone en {@code packagingDate} (solo fecha) y
	 * {@code productionTime} (solo hora) para ajustarse al contrato que consume
	 * {@code PaletDetailsModal}.
	 */
	@Mapping(target = "packagingDate",   expression = "java(palet.getPackagingDateTime() != null ? palet.getPackagingDateTime().toLocalDate() : null)")
	@Mapping(target = "productionTime",  expression = "java(palet.getPackagingDateTime() != null ? palet.getPackagingDateTime().toLocalTime() : null)")
	@Mapping(target = "packLevel",   expression = "java(toPackLevelInfo(palet.getProductPackLevel()))")
	@Mapping(target = "product",     expression = "java(toProductInfo(palet.getProductPackLevel().getProduct()))")
	@Mapping(target = "scannedBy",   expression = "java(toUserInfo(palet))")
	@Mapping(target = "workshift",   expression = "java(toWorkshiftInfo(palet))")
	PaletDetailResponseDTO toResponseDetail(Palet palet);

	/** Calcula y aplica los campos de caducidad (incluido estado crítico) al builder de {@link PaletDetailResponseDTO}. */
	@AfterMapping
	default void enrichDetailResponse(Palet entity, @MappingTarget PaletDetailResponseDTO.PaletDetailResponseDTOBuilder dto) {
		if (entity == null || dto == null) return;
		ExpiryInfo info = calculateExpiryInfo(entity.getProductUseByDate());
		dto.daysUntilExpiry(info.daysUntilExpiry())
		   .isExpiringSoon(info.isExpiringSoon())
		   .isExpired(info.isExpired())
		   .isCriticalExpiry(info.isCritical())
		   .expiryStatus(info.status());
	}

	/**
	 * Convierte {@link Palet} a {@link PaletSummaryResponseDTO}, versión plana optimizada
	 * para resultados paginados donde no se necesitan relaciones anidadas completas.
	 */
	@Mapping(target = "employeeNumber", source = "latestScan.workshift.user.employeeNumber")
	@Mapping(target = "shift", source = "latestScan.workshift.shift.shiftType.displayName")
	@Mapping(target = "gtin", source = "productPackLevel.gtin")
	@Mapping(target = "productName", source = "productPackLevel.product.name")
	@Mapping(target = "productBrand", source = "productPackLevel.product.brand")
	PaletSummaryResponseDTO toResponseSummary(Palet palet);

	/** Calcula y aplica los campos de caducidad al builder de {@link PaletSummaryResponseDTO}. */
	@AfterMapping
	default void enrichSummaryResponse(Palet entity, @MappingTarget PaletSummaryResponseDTO.PaletSummaryResponseDTOBuilder dto) {
		if (entity == null || dto == null) return;
		ExpiryInfo info = calculateExpiryInfo(entity.getProductUseByDate());
		dto.daysUntilExpiry(info.daysUntilExpiry())
		   .isExpiringSoon(info.isExpiringSoon())
		   .isExpired(info.isExpired())
		   .expiryStatus(info.status());
	}

	List<PaletSummaryResponseDTO> toSummaryResponseList(List<Palet> palets);

	/**
	 * Convierte {@link Palet} a {@link PaletNotificationDTO} para el canal WebSocket
	 * {@code /topic/palets} y el endpoint REST {@code /api/v1/palets/recent}.
	 * <p>
	 * La entidad debe tener las relaciones {@code productPackLevel}, {@code scans},
	 * {@code workshift} y {@code user} ya inicializadas para evitar
	 * {@code LazyInitializationException}. Las dimensiones y el flag de caducidad
	 * se calculan en {@link #enrichNotificationDTO}.
	 */
	@Mapping(target = "paletId",       source = "id")
	@Mapping(target = "scannedAt",     source = "latestScan.scannedAt")
	@Mapping(target = "gtin",          source = "productPackLevel.gtin")
	@Mapping(target = "productSku",    source = "productPackLevel.product.formatCode")
	@Mapping(target = "productName",   source = "productPackLevel.product.name")
	@Mapping(target = "brand",         source = "productPackLevel.product.brand")
	@Mapping(target = "packLevel",     source = "productPackLevel.packingLevel")
	@Mapping(target = "unitsInLevel",  source = "productPackLevel.unitsInLevel")
	@Mapping(target = "grossWeightKg", source = "productPackLevel.netWeight")
	@Mapping(target = "stackingLimit", source = "productPackLevel.stackingLimit")
	@Mapping(target = "employeeName",  source = "latestScan.workshift.user.fullName")
	@Mapping(target = "shiftType",     source = "latestScan.workshift.shift.shiftType")
	@Mapping(target = "dimensionsMm",  ignore = true)
	@Mapping(target = "isExpired",     ignore = true)
	PaletNotificationDTO toResponsePaletNotification(Palet palet);

	/**
	 * Agrupa los campos de caducidad calculados para evitar pasar múltiples primitivos
	 * entre los métodos de enriquecimiento {@code @AfterMapping}.
	 */
	record ExpiryInfo(Long daysUntilExpiry, boolean isExpired, boolean isExpiringSoon, boolean isCritical, String status) {
	}

	/**
	 * Punto único de cálculo de caducidad, reutilizado en todos los {@code @AfterMapping}.
	 * Devuelve {@code UNKNOWN} para fechas nulas.
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

	/** Calcula el flag de caducidad y las dimensiones en formato {@code WxH} al builder de {@link PaletNotificationDTO}. */
	@AfterMapping
	default void enrichNotificationDTO(Palet palet, @MappingTarget PaletNotificationDTO.PaletNotificationDTOBuilder dto) {
		if (palet == null || dto == null) return;
		dto.isExpired(palet.isExpired());
		ProductPackLevel packLevel = palet.getProductPackLevel();
		if (packLevel != null && packLevel.getWidthMM() != null && packLevel.getHeightMM() != null) {
			dto.dimensionsMm(String.format("%dx%d", packLevel.getWidthMM().intValue(), packLevel.getHeightMM().intValue()));
		}
	}

	/** Devuelve el estado textual de caducidad a partir de los flags ya calculados. */
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
	 * Extrae el nombre completo del operador de forma defensiva recorriendo la cadena
	 * {@code palet → latestScan → workshift → user}. Se usa via expression en {@link #toResponse}.
	 */
	default String getUserFullName(Palet palet) {
		if (palet == null || palet.getLatestScan() == null
				|| palet.getLatestScan().getWorkshift() == null
				|| palet.getLatestScan().getWorkshift().getUser() == null) {
			return null;
		}
		return palet.getLatestScan().getWorkshift().getUser().getFullName();
	}

	/** Construye el objeto anidado {@link PaletDetailResponseDTO.PackLevelInfo} desde un {@link ProductPackLevel}. */
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

	/** Construye el objeto anidado {@link PaletDetailResponseDTO.ProductInfo} desde un {@link Product}. */
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

	/** Construye el objeto anidado {@link PaletDetailResponseDTO.UserInfo} desde la relación {@code latestScan → workshift → user}. */
	default PaletDetailResponseDTO.UserInfo toUserInfo(Palet palet) {
		if (palet == null || palet.getLatestScan() == null
				|| palet.getLatestScan().getWorkshift() == null
				|| palet.getLatestScan().getWorkshift().getUser() == null) {
			return null;
		}

		User user = palet.getLatestScan().getWorkshift().getUser();
		return PaletDetailResponseDTO.UserInfo.builder()
				.id(user.getId())
				.employeeNumber(user.getEmployeeNumber())
				.fullName(user.getFullName())
				.email(user.getEmail())
				.jobPosition(user.getJobPosition())
				.build();
	}

	/** Construye el objeto anidado {@link PaletDetailResponseDTO.WorkshiftInfo} desde la relación {@code latestScan → workshift → shift}. */
	default PaletDetailResponseDTO.WorkshiftInfo toWorkshiftInfo(Palet palet) {
		if (palet == null || palet.getLatestScan() == null || palet.getLatestScan().getWorkshift() == null) {
			return null;
		}

		Workshift workshift = palet.getLatestScan().getWorkshift();
		return PaletDetailResponseDTO.WorkshiftInfo.builder()
				.id(workshift.getId())
				.date(workshift.getDate())
				.shiftType(workshift.getShift() != null ? workshift.getShift().getShiftType().getDisplayName() : null)
				.shiftDescription(workshift.getShift() != null ? workshift.getShift().getDescription() : null)
				.build();
	}
}