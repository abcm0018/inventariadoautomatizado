package com.abcm0018.sai.productos.application.bulk.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO que representa una fila del archivo CSV de importación masiva de productos.
 * <p>
 * ESTRUCTURA: Plana (una fila por ProductPackLevel)
 * <p>
 * Ejemplo CSV:
 * brand,formatCode,name,description,manufacturedIn,status,packingLevel,gtin,unitsInLevel,netWeight,heightMM,widthMM
 * ACME,COLA-2L,Refresco Cola 2L,Bebida refrescante,ES,ACTIVE,UNIDAD,5901234123456,1,2.0,300,100
 * ACME,COLA-2L,Refresco Cola 2L,Bebida refrescante,ES,ACTIVE,CAJA,5901234123463,24,48.0,350,400
 * ACME,COLA-2L,Refresco Cola 2L,Bebida refrescante,ES,ACTIVE,PALET,15901234123470,40,1920.0,1200,1000
 * <p>
 * IMPORTANTE:
 * - Las primeras 6 columnas (brand... status) se repiten para cada nivel de embalaje
 * - El normalizer agrupará por brand+formatCode para crear un único Product
 * - Para cada Product se crearán 3 ProductPackLevel (UNIDAD, CAJA, PALET)
 * <p>
 * FLUJO:
 * CSV (plano) → ProductBulkImportDTO → ProductBulkNormalizer → ProductWithPackLevelsDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductBulkImportDTO {

	/**
	 * Marca del producto (ej.: ACME, COCA-COLA)
	 * Campo requerido, parte de la clave única con formatCode
	 * Validación: No nulo, no en blanco
	 */
	@NotBlank(message = "Brand es requerido")
	private String brand;

	/**
	 * Código de formato (ej: COLA-2L, AGUA-1L)
	 * Campo requerido, parte de la clave única con brand
	 * Validación: No nulo, no en blanco
	 */
	@NotBlank(message = "FormatCode es requerido")
	private String formatCode;

	/**
	 * Nombre completo del producto (ej.: Refresco Cola 2 Litros)
	 * Campo requerido
	 * Validación: No nulo, no en blanco
	 */
	@NotBlank(message = "Name es requerido")
	private String name;

	/**
	 * Descripción del producto (ej.: Bebida refrescante carbonatada)
	 * Campo opcional
	 * Validación: Puede ser nulo, pero si está presente no puede estar en blanco
	 */
	private String description;

	/**
	 * País de manufactura (código ISO 2, ej: ES, FR, IT)
	 * Campo requerido
	 * Validación: No nulo, no en blanco
	 */
	@NotBlank(message = "ManufacturedIn es requerido")
	private String manufacturedIn;

	/**
	 * Estado del producto (ACTIVE, INACTIVE, DISCONTINUED, etc.)
	 * Campo requerido, debe ser un enum válido de ProductStatus
	 * Validación: No nulo, no en blanco
	 * Nota: El parser CSV debe convertir el string al enum
	 */
	@NotBlank(message = "Status es requerido")
	private String status;

	/**
	 * Tipo de nivel de embalaje (UNIDAD, CAJA, PALET)
	 * Campo requerido, debe ser un enum válido de PackingLevel
	 * Validación: No nulo, no en blanco
	 * Nota: El parser CSV debe convertir el string al enum
	 */
	@NotBlank(message = "PackingLevel es requerido")
	private String packingLevel;

	/**
	 * Código GTIN del nivel de embalaje
	 * - UNIDAD y CAJA: GTIN-13 (13 dígitos, EAN-13)
	 * - PALET: GTIN-14 (14 dígitos, empieza típicamente con 1)
	 * <p>
	 * Campo requerido, debe ser único en toda la BD
	 * Validación:
	 *   - No nulo, no en blanco
	 *   - Solo dígitos
	 *   - Entre 8 y 14 caracteres (soporte para EAN-8 a GTIN-14)
	 */
	@NotBlank(message = "GTIN es requerido")
	@Pattern(regexp = "^\\d{13,14}$", message = "GTIN debe contener 13-14 dígitos")
	private String gtin;

	/**
	 * Cantidad de unidades del nivel anterior que contiene este nivel
	 * - UNIDAD: siempre 1 (representa el producto base)
	 * - CAJA: número de unidades que contiene (ej.: 24)
	 * - PALET: número de cajas que contiene (ej.: 40)
	 * <p>
	 * Campo requerido, debe ser mayor a 0
	 * Mapea a: ProductPackLevel.unitsInLevel
	 * Validación: No nulo, mínimo 1
	 */
	@NotNull(message = "UnitsInLevel es requerido")
	@Positive(message = "UnitsInLevel debe ser mayor a 0")
	private Integer unitsInLevel;

	/**
	 * Peso neto en kilogramos del nivel de embalaje
	 * - UNIDAD: peso de una unidad (ej.: 2.0 kg)
	 * - CAJA: peso total (unidades + embalaje) (ej.: 48.0 kg)
	 * - PALET: peso total (cajas + embalaje) (ej.: 1920.0 kg)
	 * <p>
	 * Campo requerido, debe ser mayor a 0
	 * Mapea a: ProductPackLevel.netWeight
	 * Validación: No nulo, mínimo 0.1, máximo 1,000,000
	 */
	@NotNull(message = "NetWeight es requerido")
	@DecimalMin(value = "0.1", message = "NetWeight debe ser mayor a 0.1 kg")
	private BigDecimal netWeight;

	/**
	 * Altura en milímetros del nivel de embalaje
	 * Campo requerido, debe ser mayor a 0
	 * Mapea a: ProductPackLevel.heightMM
	 * Validación: No nulo, mínimo 1, máximo 3000
	 */
	@NotNull(message = "HeightMM es requerido")
	@Positive(message = "HeightMM debe ser mayor a 0 mm")
	private BigDecimal heightMM;

	/**
	 * Ancho en milímetros del nivel de embalaje
	 * Campo requerido, debe ser mayor a 0
	 * Mapea a: ProductPackLevel.widthMM
	 * Validación: No nulo, mínimo 1, máximo 3000
	 */
	@NotNull(message = "WidthMM es requerido")
	@Positive(message = "WidthMM debe ser mayor a 0 mm")
	private BigDecimal widthMM;

	/**
	 * Límite de apilado (número máximo de niveles iguales que se pueden apilar).
	 * Sola aplicable al nivel CAJA
	 * Requerido, debe ser al menos 0 y no mayor a 20.
	 */
	@NotNull(message = "StackingLimit es requerido")
	@Min(value = 0, message = "StackingLimit debe ser al menos 0")
	@Max(value = 20, message = "StackingLimit no puede exceder 20")
	private Integer stackingLimit;

	/**
	 * Número de cajas que caben en un palet de este producto.
	 * Requerido, puede ser 0 para UNIDAD o CAJA, > 0 para PALET.
	 */
	@NotNull(message = "BoxesPerPalet es requerido")
	@Min(value = 0, message = "BoxesPerPalet no puede ser negativo")
	@Max(value = 10000, message = "BoxesPerPalet no puede exceder 10000")
	private Integer boxesPerPalet;

	/**
	 * Retorna una descripción legible del DTO para logging
	 */
	@Override
	public String toString() {
		return String.format(
				"ProductBulkImportDTO{brand='%s', formatCode='%s', packingLevel='%s', gtin='%s', unitsInLevel=%d}",
				brand, formatCode, packingLevel, gtin, unitsInLevel
		);
	}

	/**
	 * Retorna la clave única del producto (brand + formatCode)
	 * Útil para agrupación y deduplicación en el normalizer
	 */
	public String getProductKey() {
		return brand + "-" + formatCode;
	}

	/**
	 * Verifica si este DTO representa el nivel UNIDAD
	 */
	public boolean isUnitPackLevel() {
		return "UNIDAD".equalsIgnoreCase(packingLevel);
	}

	/**
	 * Verifica si este DTO representa el nivel CAJA
	 */
	public boolean isBoxPackLevel() {
		return "CAJA".equalsIgnoreCase(packingLevel);
	}

	/**
	 * Verifica si este DTO representa el nivel PALET
	 */
	public boolean isPaletPackLevel() {
		return "PALET".equalsIgnoreCase(packingLevel);
	}

	/**
	 * Valida que las cantidades sean coherentes con el tipo de nivel
	 * - UNIDAD: unitsInLevel debe ser 1
	 * - CAJA: unitsInLevel debe ser > 1
	 * - PALET: unitsInLevel debe ser > 1
	 */
	public boolean isValidQuantityForPackLevel() {
		if (unitsInLevel == null) {
			return false;
		}

		if (isUnitPackLevel()) {
			return unitsInLevel == 1;
		}

		return unitsInLevel > 1;
	}
}