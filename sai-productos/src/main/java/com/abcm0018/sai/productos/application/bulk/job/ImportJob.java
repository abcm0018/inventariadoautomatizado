package com.abcm0018.sai.productos.application.bulk.job;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.abcm0018.sai.productos.application.bulk.enums.LoaderType;

/**
 * Entidad JPA que rastrea el estado de un trabajo de importación masiva.
 * <p>
 * Esta entidad pertenece a la capa de Aplicación, ya que su único
 * propósito es rastrear el estado de un Caso de Uso (la importación),
 * no una regla de negocio del Dominio (como 'Producto').
 */
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "BULK_IMPORTS_JOBS") // Nombre de la tabla en la BD
public class ImportJob {

	/**
	 * ID único del trabajo (Clave Primaria).
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID jobId = UUID.randomUUID();

	/**
	 * Nombre original del archivo subido por el usuario.
	 * Ej.: "mis_productos_octubre.csv"
	 */
	@Column(nullable = false)
	private String originalFilename;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private LoaderType loaderType;

	/**
	 * Ruta persistente donde se almacena el archivo (ej. disco local o S3).
	 * Ej: "/tmp/imports/a8df-..." o "s3://bucket/imports/a8df-..."
	 */
	@Column
	private String storagePath;

	/**
	 * Estado actual del trabajo (PENDING, PROCESSING, etc.).
	 * Almacenado como String en la BD para legibilidad.
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ImportJobStatus status = ImportJobStatus.PENDING;

	/**
	 * ID del usuario que inició la importación (opcional).
	 */
	private String userId;

	/**
	 * Fecha y hora de creación del trabajo.
	 */
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	/**
	 * Fecha y hora en que el worker (@Async) comenzó el procesamiento.
	 */
	private LocalDateTime startedAt;

	/**
	 * Fecha y hora en que el trabajo finalizó (COMPLETED o FAILED).
	 */
	private LocalDateTime finishedAt;

	/**
	 * Campo de texto largo (TEXT/CLOB) para almacenar el resultado.
	 * Guardará el JSON del 'BulkImportResponseDTO' (éxito)
	 * o el mensaje de error (fallo).
	 */
	@Lob
	@Column(columnDefinition = "TEXT")
	private String resultDetails;

	public static ImportJob createNewJob(String filename, LoaderType loaderType, String storagePath, String userId) {

		if (StringUtils.isBlank(filename) || StringUtils.isBlank(storagePath)
				|| StringUtils.isBlank(userId) || StringUtils.isBlank(loaderType.toString())) {

			throw new IllegalArgumentException("Error {file, storagePath, userId, loaderType} no pueden ser nulos o vacios");
		}

		ImportJob newJob = new ImportJob();

		newJob.setOriginalFilename(filename);
		newJob.setLoaderType(loaderType);
		newJob.setStoragePath(storagePath);
		newJob.setUserId(userId);

		return newJob;
	}

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();

		if (status == null) {
			status = ImportJobStatus.PENDING;
		}

		this.finishedAt = null;
		this.startedAt = null;
	}

}