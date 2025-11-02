package com.abcm0018.sai.email.domain.repository;

import com.abcm0018.sai.email.domain.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
	/**
	 * Buscar plantilla por nombre (búsqueda exacta)
	 */
	Optional<EmailTemplate> findByTemplateName(String templateName);

	/**
	 * Validar si existe una plantilla
	 */
	boolean existsByTemplateName(String templateName);

	/**
	 * Obtener todas las plantillas (para administración)
	 */
	@Query("SELECT t FROM EmailTemplate t ORDER BY t.templateName ASC")
	List<EmailTemplate> findAllOrderByName();

	/**
	 * Búsqueda parcial de plantillas (para UI de búsqueda)
	 */
	@Query("SELECT t FROM EmailTemplate t WHERE t.templateName LIKE %:search% OR t.subject LIKE %:search%")
	List<EmailTemplate> searchTemplates(String search);
}
