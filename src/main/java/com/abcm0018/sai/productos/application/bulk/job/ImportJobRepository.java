package com.abcm0018.sai.productos.application.bulk.job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de Spring Data JPA para la entidad ImportJob.
 * Proporciona métodos CRUD básicos (save, findById, etc.)
 * para gestionar los trabajos de importación.
 */
@Repository
public interface ImportJobRepository extends JpaRepository<ImportJob, UUID> {
	// Spring Data JPA generará automáticamente las implementaciones
	// Se pueden añadir métodos de consulta personalizados aquí si es necesario
	List<ImportJob> findByUserIdAndStatus(String userId, ImportJobStatus status);
}

