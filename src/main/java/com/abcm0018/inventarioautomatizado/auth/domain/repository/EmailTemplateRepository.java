package com.abcm0018.inventarioautomatizado.auth.domain.repository;

import com.abcm0018.inventarioautomatizado.auth.domain.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByTemplateName(String templateName);
}
