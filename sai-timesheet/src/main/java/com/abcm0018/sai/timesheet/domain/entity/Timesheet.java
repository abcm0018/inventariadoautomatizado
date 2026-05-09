package com.abcm0018.sai.timesheet.domain.entity;

import com.abcm0018.sai.timesheet.domain.enums.TimesheetStatus;
import com.abcm0018.sai.workshift.domain.entity.Workshift;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Entidad que representa el registro de entrada y salida de un usuario en un turno específico.
 * Permite el control de asistencia y cálculo de horas trabajadas.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "TIMESHEET",
		indexes = {
				@Index(name = "idx_workshift", columnList = "workshift_id"),
				@Index(name = "idx_check_in", columnList = "check_in_at"),
				@Index(name = "idx_check_out", columnList = "check_out_at")
		})
public class Timesheet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	// ========== RELACIÓN CON WORKSHIFT ==========

	/**
	 * Workshift asociado (contiene usuario, turno y fecha)
	 * Esta es la ÚNICA relación necesaria
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "workshift_id", nullable = false)
	@JsonIgnore
	private Workshift workshift;

	// ========== REGISTRO DE ENTRADA/SALIDA ==========
	@Column(name = "CHECK_IN_AT", nullable = false)
	private LocalDateTime checkInAt;

	@Column(name = "CHECK_OUT_AT") // Puede ser null (aún no ha salido)
	private LocalDateTime checkOutAt;

	// ========== INFORMACIÓN ADICIONAL ==========
	@Column(name = "NOTES", length = 500)
	private String notes; // Observaciones: tardanza, salida anticipada, etc.

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", length = 20)
	@Builder.Default
	private TimesheetStatus status = TimesheetStatus.OPEN;

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

		// Si no se especifica estado, es OPEN
		if (this.status == null) {
			this.status = TimesheetStatus.OPEN;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();

		// Si se registra check-out, cambiar estado a CLOSED
		if (this.checkOutAt != null && this.status == TimesheetStatus.OPEN) {
			this.status = TimesheetStatus.CLOSED;
		}
	}

	// ========== MÉTODOS DE UTILIDAD ==========

	/**
	 * Verifica si el registro está abierto (sin check-out)
	 */
	public boolean isOpen() {
		return checkOutAt == null;
	}

	/**
	 * Verifica si el registro está cerrado (con check-out)
	 */
	public boolean isClosed() {
		return checkOutAt != null;
	}

	/**
	 * Calcula las horas trabajadas (solo si hay check-out)
	 */
	public Long getWorkedHours() {
		if (checkOutAt == null) {
			return null; // Aún no ha terminado
		}

		Duration duration = Duration.between(checkInAt, checkOutAt);
		return duration.toHours();
	}

	/**
	 * Calcula los minutos trabajados (solo si hay check-out)
	 */
	public Long getWorkedMinutes() {
		if (checkOutAt == null) {
			return null;
		}

		Duration duration = Duration.between(checkInAt, checkOutAt);
		return duration.toMinutes();
	}

	/**
	 * Obtiene la duración trabajada en formato legible (ej: "8h 30m")
	 */
	public String getWorkedDuration() {
		if (checkOutAt == null) {
			return "En curso";
		}

		Duration duration = Duration.between(checkInAt, checkOutAt);
		long hours = duration.toHours();
		long minutes = duration.toMinutes() % 60;

		return String.format("%dh %dm", hours, minutes);
	}

	/**
	 * Verifica si el usuario llegó tarde (más de X minutos después del inicio del turno)
	 */
	public boolean isLate(int toleranceMinutes) {
		if (workshift == null || workshift.getShift() == null) {
			return false;
		}

		LocalDateTime expectedCheckIn = LocalDateTime.of(
				workshift.getDate(),
				workshift.getShift().getStartTime()
		);

		Duration delay = Duration.between(expectedCheckIn, checkInAt);
		return delay.toMinutes() > toleranceMinutes;
	}

	/**
	 * Calcula los minutos de retraso
	 */
	public long getLateMinutes() {
		if (workshift == null || workshift.getShift() == null) {
			return 0;
		}

		LocalDateTime expectedCheckIn = LocalDateTime.of(
				workshift.getDate(),
				workshift.getShift().getStartTime()
		);

		Duration delay = Duration.between(expectedCheckIn, checkInAt);
		return Math.max(0, delay.toMinutes());
	}

	/**
	 * Verifica si el usuario salió antes de tiempo
	 */
	public boolean isEarlyExit(int toleranceMinutes) {
		if (checkOutAt == null || workshift == null || workshift.getShift() == null) {
			return false;
		}

		LocalDateTime expectedCheckOut = LocalDateTime.of(
				workshift.getDate(),
				workshift.getShift().getEndTime()
		);

		// Ajustar si el turno cruza medianoche
		if (workshift.getShift().crossesMidnight()) {
			expectedCheckOut = expectedCheckOut.plusDays(1);
		}

		Duration early = Duration.between(checkOutAt, expectedCheckOut);
		return early.toMinutes() > toleranceMinutes;
	}

	/**
	 * Verifica si trabajó horas extras (más de X horas extras)
	 */
	public boolean hasOvertime(long extraHoursThreshold) {
		if (checkOutAt == null || workshift == null || workshift.getShift() == null) {
			return false;
		}

		Long workedHours = getWorkedHours();
		if (workedHours == null) {
			return false;
		}

		long expectedHours = workshift.getShift().getDurationInHours();
		long overtime = workedHours - expectedHours;

		return overtime > extraHoursThreshold;
	}

	/**
	 * Calcula las horas extras trabajadas
	 */
	public long getOvertimeHours() {
		if (checkOutAt == null || workshift == null || workshift.getShift() == null) {
			return 0;
		}

		Long workedHours = getWorkedHours();
		if (workedHours == null) {
			return 0;
		}

		long expectedHours = workshift.getShift().getDurationInHours();
		return Math.max(0, workedHours - expectedHours);
	}

	/**
	 * Registra la salida del usuario
	 */
	public void checkOut() {
		if (this.checkOutAt != null) {
			throw new IllegalStateException("El check-out ya fue registrado");
		}
		this.checkOutAt = LocalDateTime.now();
		this.status = TimesheetStatus.CLOSED;
	}

	/**
	 * Registra la salida con una hora específica (para correcciones)
	 */
	public void checkOut(LocalDateTime checkOutTime) {
		if (this.checkOutAt != null) {
			throw new IllegalStateException("El check-out ya fue registrado");
		}
		if (checkOutTime.isBefore(this.checkInAt)) {
			throw new IllegalArgumentException("La hora de salida no puede ser anterior a la entrada");
		}
		this.checkOutAt = checkOutTime;
		this.status = TimesheetStatus.CLOSED;
	}

	/**
	 * Verifica si el registro ha sido modificado
	 */
	public boolean hasBeenUpdated() {
		return this.updatedAt != null;
	}
}
