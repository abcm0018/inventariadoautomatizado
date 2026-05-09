package com.abcm0018.sai.shift.domain.entity;

import com.abcm0018.sai.shift.domain.enums.ShiftType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "SHIFTS",
		uniqueConstraints = @UniqueConstraint(columnNames = {"uk_shift_type", "SHIFT_TYPE"}),
		indexes = { @Index(name = "idx_shift_type", columnList = "SHIFT_TYPE") })
public class Shift {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "SHIFT_TYPE", nullable = false, unique = true, length = 20)
	private ShiftType shiftType;

	@Column(name = "START_TIME", nullable = false)
	private LocalTime startTime;

	@Column(name = "END_TIME", nullable = false)
	private LocalTime endTime;

	@Column(name = "DESCRIPTION", length = 200)
	private String description;

	@Column(name = "ACTIVE", nullable = false)
	@Builder.Default
	private boolean active = true; // Permite deshabilitar turnos sin borrarlos

	@Column(name = "CREATE_AT", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "UPDATE_AT")
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = null;
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * Calcula la duración del turno en horas
	 */
	public long getDurationInHours() {
		Duration duration = Duration.between(startTime, endTime);

		// Si el turno cruza la medianoche (ej: 22:00 - 06:00)
		if (endTime.isBefore(startTime)) {
			duration = duration.plusHours(24);
		}

		return duration.toHours();
	}

	/**
	 * Calcula la duración del turno en minutos
	 */
	public long getDurationInMinutes() {
		Duration duration = Duration.between(startTime, endTime);

		if (endTime.isBefore(startTime)) {
			duration = duration.plusHours(24);
		}

		return duration.toMinutes();
	}

	/**
	 * Verifica si el turno cruza la medianoche
	 */
	public boolean crossesMidnight() {
		return endTime.isBefore(startTime);
	}

	/**
	 * Obtiene una representación legible del turno
	 */
	public String getShiftDescription() {
		return String.format("%s (%s - %s) - %d horas", shiftType.getDisplayName(), startTime, endTime, getDurationInHours());
	}

	/**
	 * Verifica si una hora específica está dentro del turno
	 */
	public boolean isTimeWithinShift(LocalTime time) {
		if (crossesMidnight()) {
			return time.isAfter(startTime) || time.equals(startTime) || time.isBefore(endTime) || time.equals(endTime);
		} else {
			return (time.isAfter(startTime) || time.equals(startTime)) && (time.isBefore(endTime) || time.equals(endTime));
		}
	}

	/**
	 * Verifica si el turno ha sido modificado
	 */
	public boolean hasBeenUpdated() {
		return this.updatedAt != null;
	}
}
