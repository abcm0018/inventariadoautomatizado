package com.abcm0018.sai.productos.infrastructure.bulk.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de FileStorageService que utiliza el
 * sistema de archivos local.
 */
@Slf4j
@Service
public class LocalStorageServiceImpl implements FileStorageService {

	private final Path rootLocation;

	@Autowired
	public LocalStorageServiceImpl(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
	}

	/**
	 * Se ejecuta al iniciar el servicio.
	 * Crea el directorio raíz si no existe.
	 */
	@Override
	@PostConstruct
	public void init() {
		try {
			Files.createDirectories(rootLocation);
			log.info("Directorio de almacenamiento incializado en: {}", rootLocation.toAbsolutePath());
		} catch (IOException e) {
			log.error("Error al inicializar el directorio de almacenamiento: {}", rootLocation.toAbsolutePath(), e);
			throw new StorageServiceException("No se pudo inciar el directorio de almacenamiento", e);
		}
	}

	/**
	 * Guarda el archivo en el disco local.
	 * @param file Archivo subido.
	 * @param filename El nombre único (ej. UUID del Job) con el que se guardará para evitar colisiones.
	 */
	@Override
	public String save(MultipartFile file, String filename) throws StorageServiceException {
		if (file.isEmpty()) {
			throw new StorageServiceException("No se puede guardar un archivo vacío.");
		}

		if (filename == null || filename.isBlank()) {
			throw new StorageServiceException("El nombre de un archivo (JobId) no puede ser nulo");
		}

		// Resuelve la ruta de destino (ej. /tmp/imports/jobId.uuid)
		Path destinationFile = this.rootLocation.resolve(filename).toAbsolutePath();
		log.debug("Guardando archivo en: {}", destinationFile);

		if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
			// Verficación de seguridad (Path trasversal)
			throw new StorageServiceException("No se puede guardar el archivo fuera del directorio raíz");
		}

		// Copia el contenido del archivo al destino
		try {
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
			}

			// Devuelve el nombre del archivo
			return filename;
		} catch (IOException e) {
			log.error("Error al guardar el archivo {}: {}", filename, e.getMessage());
			throw new StorageServiceException("Error al guardar el archivo: " + filename, e);
		}
	}

	@Override
	public String save(InputStream inputStream, String filename) throws StorageServiceException {
		if (inputStream == null) {
			throw new StorageServiceException("No se puede guardar un stream de entrada nulo.");
		}

		if (filename == null || filename.isBlank()) {
			throw new StorageServiceException("El nombre de un archivo no puede ser nulo");
		}

		Path destinationFile = this.rootLocation.resolve(filename).toAbsolutePath();
		log.debug("Guardando archivo desde InputStream en: {}", destinationFile);

		if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
			// Verficación de seguridad (Path trasversal)
			throw new StorageServiceException("No se puede guardar el archivo fuera del directorio raíz");
		}

		// Copia el contenido del stream al destino
		try {
			// Se usa Files.copy() directamente con el InputStream y la ruta de destino.
			Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);

			return filename;
		} catch (IOException e) {
			log.error("Error al guardar el archivo de errores {}: {}", filename, e.getMessage());
			throw new StorageServiceException("Error al guardar el archivo: " + filename, e);
		}
	}

	/**
	 * Resuelve la ruta relativa (filename) contra la ruta raíz.
	 */
	@Override
	public Path load(String filename) throws StorageServiceException {
		if (filename == null || filename.isBlank()) {
			throw new StorageServiceException("El nombre de archivo no puede ser nulo.");
		}
		return rootLocation.resolve(filename);
	}

	/**
	 * Obtiene un InputStream del archivo guardado.
	 * Esto será usado por el ProductLoaderStrategy.
	 */
	@Override
	public InputStream loadAsInputStream(String filename) throws StorageServiceException {
		try {
			Path file = load(filename);
			if (!Files.exists(file) || !Files.isReadable(file)) {
				log.warn("Archivo no encontrado o no legible: {}", filename);
				throw new StorageServiceException("Archivo no encontrado: " + filename);
			}

			return Files.newInputStream(file);
		} catch (IOException e) {
			log.error("Error al leer el archivo {}: {}", filename, e.getMessage());
			throw new StorageServiceException("Error al leer el archivo: " + filename, e);
		}
	}

	@Override
	public void delete(String filename) throws StorageServiceException {
		try {
			Path file = load(filename);
			Files.deleteIfExists(file);
			log.debug("Archivo temporal eliminado: {}", filename);
		} catch (IOException e) {
			log.error("Error al eliminar el archivo {}: {}", filename, e.getMessage());
			// No lanzamos excepción; un fallo al limpiar no debe
			// hacer fallar el proceso principal.
		}
	}
}
