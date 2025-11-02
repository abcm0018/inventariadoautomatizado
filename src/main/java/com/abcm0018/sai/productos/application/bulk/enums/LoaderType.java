package com.abcm0018.sai.productos.application.bulk.enums;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import com.abcm0018.sai.productos.application.bulk.exceptions.ProductBulkImportException;
import com.abcm0018.sai.shared.constants.CustomErrorCode;

/**
 * Enum para identificar de forma segura los tipos de cargadores (loaders)
 * Esto reemplaza el uso de Strings como "csv" o "xlsx".
 */
public enum LoaderType {
	CSV,
	JSON,
	XLSX;

	/**
	 * Extrae la extensión de un nombre de archivo.
	 *
	 * @param filename El nombre original del archivo (ej. "productos.csv")
	 * @return La extensión en minúsculas (ej. "csv")
	 * @throws ProductBulkImportException si no se puede obtener el nombre o la extensión.
	 */
	public static String extractFileExtension(String filename) {
		return Optional.ofNullable(filename)
				.filter(f -> f.contains("."))
				.map(f -> f.substring(filename.lastIndexOf(".") + 1).toLowerCase().trim())
				.orElseThrow(() -> new ProductBulkImportException(CustomErrorCode.BAD_REQUEST,
						"El archivo no tiene nombre o extensión válidos", HttpStatus.BAD_REQUEST));
	}

	/**
	 * Convierte una extensión de archivo (String) en un LoaderType (Enum).
	 *
	 * @param extension ej. "csv"
	 * @return LoaderType
	 * @throws ProductBulkImportException si la extensión no es soportada.
	 */
	public static LoaderType fromExtension(String extension) {
		if (StringUtils.isBlank(extension)) {
			throw new ProductBulkImportException(CustomErrorCode.BAD_REQUEST,
					"No se pudo determinar el tipo de archivo", HttpStatus.BAD_REQUEST);
		}

		try {
			return LoaderType.valueOf(extension.toUpperCase().trim());
		} catch (IllegalArgumentException e) {
			throw new ProductBulkImportException(CustomErrorCode.BAD_REQUEST,
					"Tipo de archivo no soportado: " + extension, HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Combina extracción y conversión.
	 * Usado por el Controller.
	 */
	public static LoaderType fromFilename(String filename) {
		String extension = extractFileExtension(filename);
		return fromExtension(extension);
	}
}
