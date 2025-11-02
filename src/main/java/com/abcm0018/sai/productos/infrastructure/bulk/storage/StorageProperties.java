package com.abcm0018.sai.productos.infrastructure.bulk.storage;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Clase de configuración para vincular las propiedades desde
 * application.properties a (con el prefijo "storage.")
 * <p>
 * Ejemplo en application.properties:
 * storage.location=/tmp/sai-imports
 * storage.max-file-size=10MB
 * storage.allowed-file-extensions=csv,xlsx
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
	/**
	 * Ubicación de la carpeta para almacenar los archivos de importación.
	 * Por defecto es "/tmp/imports" si no se especifica.
	 */
	private String location = "/tmp/imports";

	/**
	 * Tamaño máximo del archivo permitido en bytes.
	 * Por defecto 10MB (10 * 1024 * 1024).
	 * Se usará para la validación de "Fase 0" en el controller.
	 * Ejemplo: 10485760
	 */
	private Long maxSizeInBytes = 10485760L; // 10MB por defecto

	/**
	 * Lista de extensiones de archivo permitidas (sin punto).
	 * Se usará para la validación de "Fase 0".
	 * Ejemplo: csv,xlsx
	 */
	private List<String> allowedTypes = new ArrayList<>();
}
