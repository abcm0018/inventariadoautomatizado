package com.abcm0018.sai.productos.application.bulk.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO que representa un nivel de embalaje normalizado (después de validación estructural).
 * <p>
 * DIFERENCIA CON ProductBulkImportDTO:
 * - ProductBulkImportDTO: estructura plana del CSV, sin validaciones
 * - ProductPackLevelDTO: estructura jerárquica, validada y normalizada
 * <p>
 * USADO EN:
 * - ProductWithPackLevelsDTO (como lista de niveles)
 * - Respuestas del normalizer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPackLevelDTO {

	/**
	 * Tipo de nivel de embalaje (UNIDAD, CAJA, PALET)
	 * Ya convertido a enum en una fase anterior
	 */
	private String packingLevel;

	/**
	 * Código GTIN del nivel
	 * Validado: GTIN-13 para UNIDAD/CAJA, GTIN-14 para PALET
	 */
	private String gtin;

	/**
	 * Cantidad de unidades del nivel anterior que contiene
	 * Validado:
	 * - UNIDAD: siempre 1
	 * - CAJA: > 1
	 * - PALET: > 1
	 */
	private Integer unitsInLevel;

	/**
	 * Peso neto en kilogramos
	 */
	private BigDecimal netWeight;

	/**
	 * Altura en milímetros
	 */
	private BigDecimal heightMM;

	/**
	 * Ancho en milímetros
	 */
	private BigDecimal widthMM;

	/**
	 * Límite de apilado (número máximo de niveles iguales que se pueden apilar).
	 * Validado: >= 0 (0 para UNIDAD/PALET, > 0 para CAJA)
	 */
	private Integer stackingLimit;

	/**
	 * Número de cajas que caben en un palet de este producto.
	 * Validado: >= 0 (0 para UNIDAD/CAJA, > 0 para PALET)
	 */
	private Integer boxesPerPalet;
}
