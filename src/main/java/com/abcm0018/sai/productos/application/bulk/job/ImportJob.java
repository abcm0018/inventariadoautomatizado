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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import com.abcm0018.sai.productos.application.bulk.enums.LoaderType;

/**
 * Entidad JPA que rastrea el estado de un trabajo de importación masiva.
 * <p>
 * Esta entidad pertenece a la capa de Aplicación, ya que su único
 * propósito es rastrear el estado de un Caso de Uso (la importación),
 * no una regla de negocio del Dominio (como 'Producto').
 */
@Data
@Builder
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
	@Builder.Default
	private ImportJobStatus status = ImportJobStatus.PENDING;

	/**
	 * ID del usuario que inició la importación (opcional).
	 */
	private String userId;

	/**
	 * Fecha y hora de creación del trabajo.
	 */
	@Column(nullable = false, updatable = false)
	@Builder.Default
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

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.finishedAt = null;
		this.startedAt = null;
	}

}