package com.abcm0018.sai.productos.domain.enums;

import lombok.Getter;

@Getter
public enum PackingLevel {
	/**
	 * Unidad de venta
	 */
	UNIT("Unidad"),
	/**
	 * Unidad de caja
	 */
	CASE("Caja"),
	/**
	 * Palet (Agrupación logísitica)
	 */
	PALLET("Palet");

	private final String displayName;

	PackingLevel(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Busca un PackingLevel por su nombre técnico (ej. "UNIT") o
	 * por su nombre legible (ej. "Unidad"), ignorando mayúsculas/minúsculas.
	 *
	 * @param input El string a buscar ("UNIDAD", "Unidad", "UNIT", etc.).
	 * @return El PackingLevel correspondiente.
	 * @throws IllegalArgumentException si no se encuentra ninguna coincidencia.
	 */
	public static PackingLevel fromDisplayNameOrName(String input) {
		if (input == null) {
			throw new IllegalArgumentException("El nivel de embalaje no puede estar vacío");
		}

		String upperInput = input.toUpperCase();

		// Intenta buscar el nombre técnico (UNIT, CASE, PALLET)
		try {
			return PackingLevel.valueOf(upperInput);
		} catch (IllegalArgumentException e) {
			// Si falla, intenta buscar por nombre legible (Unidad, Caja, Palet)
			for (PackingLevel level : values()) {
				if (level.getDisplayName().equalsIgnoreCase(input)) {
					return level;
				}
			}
		}

		// Si no se encuentra de ninguna forma, lanzar excepción
		throw new IllegalArgumentException("Nivel de embalaje no válido: '" + input + "'");
	}
}
