package com.abcm0018.sai.email.domain.entity;

import java.time.LocalDateTime;

import com.abcm0018.sai.email.domain.enums.EmailStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que audita todos los emails enviados por el sistema.
 * Permite rastrear: quién recibió qué, cuándo, estado del envío, errores.
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "EMAIL_LOG", indexes = {
		@Index(name = "idx_recipient", columnList = "recipient"),
		@Index(name = "idx_email_type", columnList = "email_type"),
		@Index(name = "idx_status", columnList = "status"),
		@Index(name = "idx_sent_at", columnList = "sent_at")
})
public class EmailLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "recipient", nullable = false)
	private String recipient;

	@Column(name = "subject", nullable = false)
	private String subject;

	@Column(name = "email_type", nullable = false)
	private String emailType;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	@Column(name = "status", nullable = false)
	private EmailStatus status = EmailStatus.PENDING;

	@Column(name = "body", columnDefinition = "LONGTEXT")
	private String body;

	@Column(name = "error_message", columnDefinition = "TEXT")
	private String errorMessage;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;

	@Column(name = "retry_count", nullable = false)
	@Builder.Default
	private Integer retryCount = 0;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = null;

		if (this.status == null) {
			this.status = EmailStatus.PENDING;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
