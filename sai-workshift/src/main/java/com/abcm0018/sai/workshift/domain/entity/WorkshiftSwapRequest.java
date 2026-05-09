package com.abcm0018.sai.workshift.domain.entity;

import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.workshift.domain.enums.SwapRequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
		name = "WORKSHIFT_SWAP_REQUEST",
		indexes = {
				@Index(name = "idx_user", columnList = "USER_ID"),
				@Index(name = "idx_status", columnList = "status"),
				@Index(name = "idx_create_at", columnList = "create_at"),
				@Index(name = "idx_current_workshift", columnList = "CURRENT_WORKSHIFT_ID"),
				@Index(name = "idx_requested_workshift", columnList = "REQUESTED_WORKSHIFT_ID")
		}
)
public class WorkshiftSwapRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	// ========== USUARIO SOLICITANTE ==========

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "USER_ID", nullable = false)
	@JsonIgnore
	private User user; // Usuario que solicita el cambio

	// ========== TURNOS INVOLUCRADOS ==========

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "CURRENT_WORKSHIFT_ID", nullable = false)
	@JsonIgnore
	private Workshift currentWorkshift; // Turno que tiene actualmente

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "REQUESTED_WORKSHIFT_ID", nullable = false)
	@JsonIgnore
	private Workshift requestedWorkshift; // Turno al que quiere cambiar

	// ========== JUSTIFICACIÓN ==========

	@Column(name = "REASON", nullable = false, length = 500)
	private String reason; // Motivo del cambio

	// ========== ESTADO DE LA SOLICITUD ==========

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false, length = 20)
	@Builder.Default
	private SwapRequestStatus status = SwapRequestStatus.PENDING;

	// ========== REVISIÓN ==========

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "REVIEWED_BY_USER_ID")
	@JsonIgnore
	private User reviewedBy; // Usuario que aprobó/rechazó (supervisor/admin)

	@Column(name = "REVIEWED_AT")
	private LocalDateTime reviewedAt; // Fecha de aprobación/rechazo

	@Column(name = "REVIEW_NOTES", length = 500)
	private String reviewNotes; // Notas del revisor

	// ========== AUDITORÍA ==========

	@Column(name = "CREATE_AT", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "UPDATE_AT")
	private LocalDateTime updatedAt;

	// ========== LIFECYCLE CALLBACKS ==========

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = null;

		if (this.status == null) {
			this.status = SwapRequestStatus.PENDING;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	// ========== MÉTODOS DE UTILIDAD ==========

	/**
	 * Verifica si la solicitud está pendiente
	 */
	public boolean isPending() {
		return this.status == SwapRequestStatus.PENDING;
	}

	/**
	 * Verifica si la solicitud fue aprobada
	 */
	public boolean isApproved() {
		return this.status == SwapRequestStatus.APPROVED;
	}

	/**
	 * Verifica si la solicitud fue rechazada
	 */
	public boolean isRejected() {
		return this.status == SwapRequestStatus.REJECTED;
	}

	/**
	 * Verifica si la solicitud fue cancelada
	 */
	public boolean isCancelled() {
		return this.status == SwapRequestStatus.CANCELLED;
	}

	/**
	 * Verifica si la solicitud ya fue procesada (no está pendiente)
	 */
	public boolean isProcessed() {
		return this.status != SwapRequestStatus.PENDING;
	}

	/**
	 * Aprueba la solicitud de cambio
	 */
	public void approve(User reviewer, String notes) {
		if (this.status != SwapRequestStatus.PENDING) {
			throw new IllegalStateException("Solo se pueden aprobar solicitudes pendientes");
		}

		this.status = SwapRequestStatus.APPROVED;
		this.reviewedBy = reviewer;
		this.reviewedAt = LocalDateTime.now();
		this.reviewNotes = notes;
	}

	/**
	 * Rechaza la solicitud de cambio
	 */
	public void reject(User reviewer, String notes) {
		if (this.status != SwapRequestStatus.PENDING) {
			throw new IllegalStateException("Solo se pueden rechazar solicitudes pendientes");
		}

		if (notes == null || notes.trim().isEmpty()) {
			throw new IllegalArgumentException("Debe proporcionar una razón para el rechazo");
		}

		this.status = SwapRequestStatus.REJECTED;
		this.reviewedBy = reviewer;
		this.reviewedAt = LocalDateTime.now();
		this.reviewNotes = notes;
	}

	/**
	 * Cancela la solicitud (solo el usuario que la creó puede cancelarla)
	 */
	public void cancel(User requester) {
		if (!this.user.getId().equals(requester.getId())) {
			throw new IllegalStateException("Solo el solicitante puede cancelar la solicitud");
		}

		if (this.status != SwapRequestStatus.PENDING) {
			throw new IllegalStateException("Solo se pueden cancelar solicitudes pendientes");
		}

		this.status = SwapRequestStatus.CANCELLED;
		this.reviewedAt = LocalDateTime.now();
	}

	/**
	 * Obtiene un resumen legible de la solicitud
	 */
	public String getSummary() {
		return String.format("%s solicita cambiar de %s a %s",
				user != null ? user.getFullName() : "Usuario desconocido",
				currentWorkshift != null ? currentWorkshift.getFullDescription() : "Turno desconocido",
				requestedWorkshift != null ? requestedWorkshift.getFullDescription() : "Turno desconocido"
		);
	}

	/**
	 * Verifica si la solicitud ha sido modificada
	 */
	public boolean hasBeenUpdated() {
		return this.updatedAt != null;
	}

	/**
	 * Obtiene el tiempo transcurrido desde la creación (en horas)
	 */
	public long getHoursSinceCreation() {
		if (this.createdAt == null) {
			return 0;
		}
		return Duration.between(this.createdAt, LocalDateTime.now()).toHours();
	}

	/**
	 * Verifica si la solicitud está vencida (más de X días sin respuesta)
	 */
	public boolean isOverdue(int daysThreshold) {
		if (!isPending()) {
			return false; // Solo las pendientes pueden estar vencidas
		}
		return getHoursSinceCreation() > (daysThreshold * 24L);
	}
}


