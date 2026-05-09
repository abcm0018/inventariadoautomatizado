package com.abcm0018.sai.productos.infrastructure.bulk.storage;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Interfaz (Adaptador Secundario) que define el contrato
 * para el almacenamiento de archivos.
 * <p>
 * La lógica de aplicación (ProductBulkImportService) dependerá
 * de esta interfaz, no de su implementación.
 */
public interface FileStorageService {

	/**
	 * Inicializa el servicio de almacenamiento (ej. crear directorio raíz).
	 */
	void init();

	/**
	 * Guarda un MultipartFile en el almacenamiento.
	 *
	 * @param file Archivo subido.
	 * @param filename El nombre único (ej. UUID del Job) con el que se guardará.
	 * @return La ruta relativa o identificador del archivo guardado.
	 * @throws StorageServiceException si hay un error al guardar.
	 */
	String save(MultipartFile file, String filename) throws StorageServiceException;

	/**
	 * Guarda el archivo en el disco local desde un InputStream.
	 * Se utiliza para guardar archivos generados internamente (ej. CSV de errores).
	 * * @param inputStream Stream de datos a guardar.
	 * @param filename El nombre único con el que se guardará (ej. errores_UUID.csv).
	 */
	String save(InputStream inputStream, String filename) throws StorageServiceException;

	/**
	 * Carga un archivo como un Path.
	 *
	 * @param filename El nombre/ID del archivo a cargar (ej. UUID del Job).
	 * @return Path al archivo físico.
	 * @throws StorageServiceException si el archivo no se encuentra.
	 */
	Path load(String filename) throws StorageServiceException;

	/**
	 * Carga un archivo como un InputStream.
	 *
	 * @param filename El nombre/ID del archivo a cargar.
	 * @return InputStream del archivo.
	 * @throws StorageServiceException si el archivo no se encuentra o no se puede leer.
	 */
	InputStream loadAsInputStream(String filename) throws StorageServiceException;

	/**
	 * Elimina un archivo del almacenamiento.
	 *
	 * @param filename El nombre/ID del archivo a eliminar.
	 * @throws StorageServiceException si hay un error al eliminar.
	 */
	void delete(String filename) throws StorageServiceException;
}
