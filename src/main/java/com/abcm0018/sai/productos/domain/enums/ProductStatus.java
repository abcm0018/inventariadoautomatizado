package com.abcm0018.sai.productos.domain.enums;

import java.util.Arrays;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import static java.util.stream.Collectors.toSet;

/**
 * Estados posibles de un producto en el sistema
 */
@Getter
public enum ProductStatus {
	/**
	 * Producto en fabricación activa
	 */
	ACTIVE("Activo"),
	/**
	 * Producto discontinuado, ya no se fabrica
	 */
	DISCONTINUED("Discontinuado"),

	/**
	 * Producto temporalmente fuera de producción
	 */
	TEMPORARY_OUT("Fuera temporalmente"),

	/**
	 * Producto en fase de prueba/validación
	 */
	PILOT("En fase piloto");

	private final String displayName;

	ProductStatus(String displayName) {
		this.displayName = displayName;
	}

	private static final Set<String> VALID_STATUSES = Arrays.stream(values())
			.map(ProductStatus::getDisplayName)
			.collect(toSet());


	/**
	 * Busca un ProductStatus por su nombre técnico (ej. "ACTIVE") o
	 * por su nombre legible (ej. "Activo"), ignorando mayúsculas/minúsculas.
	 *
	 * @param input El string a buscar.
	 * @return El ProductStatus correspondiente.
	 * @throws IllegalArgumentException si no se encuentra ninguna coincidencia.
	 */
	public static ProductStatus fromDisplayNameOrName(String input) {
		if (StringUtils.isBlank(input)) {
			throw new IllegalArgumentException("El estado no puede estar vacío");
		}

		String upperInput = input.toUpperCase();

		// Intenta buscar por nombre técnico (ACTIVE)
		try {
			return ProductStatus.valueOf(upperInput);
		} catch (IllegalArgumentException e) {
			// Si falla, intenta buscar por nombre legible (Activo)
			for (ProductStatus status : values()) {
				if (status.getDisplayName().equalsIgnoreCase(input)) {
					return status;
				}
			}
		}

		// Si no se encuentra de ninguna forma, lanzar excepción
		throw new IllegalArgumentException("Estado no válido: '" + input + "'");
	}

	public static boolean isValidStatus(String status) {

		if (StringUtils.isBlank(status)) {
			return false;
		}

		return VALID_STATUSES.contains(status);
	}

}
