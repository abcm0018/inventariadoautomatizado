package com.abcm0018.sai.workshift.domain.entity;

import com.abcm0018.sai.palets.domain.entity.Palet;
import com.abcm0018.sai.shift.domain.entity.Shift;
import com.abcm0018.sai.shift.domain.enums.ShiftType;
import com.abcm0018.sai.timesheet.domain.entity.Timesheet;
import com.abcm0018.sai.users.domain.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Asignación de un turno específico a un usuario en una fecha concreta.
 * Representa la programación real de trabajo.
 */
@Getter
@Setter
@ToString(exclude = {"shift", "user", "timesheets", "palets"})
@NoArgsConstructor
@Entity
@Table(
		name = "WORKSHIFTS",
		uniqueConstraints = {@UniqueConstraint(name = "uk_user_date", columnNames = {"EMPLOYEE_ID", "DATE"})},
		indexes = {
				@Index(name = "idx_date", columnList = "DATE"),
				@Index(name = "idx_user_date", columnList = "EMPLOYEE_ID, DATE"),
				@Index(name = "idx_shift_date", columnList = "SHIFT_ID, DATE")
		}
)
public class Workshift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	// ========== ASIGNACIÓN DEL TURNO ==========
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "SHIFT_ID", nullable = false)
	private Shift shift; // Referencia al catálogo de turnos

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "EMPLOYEE_ID", nullable = false)
	private User user; // Usuario asignado

    @Column(name = "DATE", nullable = false)
    private LocalDate date;

	// ========== RELACIONES ==========
	@JsonIgnore
	@OneToMany(mappedBy = "workshift", fetch = FetchType.LAZY)
	private List<Timesheet> timesheets = new ArrayList<>();

	@JsonIgnore
	@OneToMany(mappedBy = "workshift", fetch = FetchType.LAZY)
	private List<Palet> palets = new ArrayList<>();

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
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	// ========== MÉTODOS DE UTILIDAD ==========

	public static Workshift create(User user, Shift shift, LocalDate date) {

		if (user == null || shift == null || date == null) {
			throw new IllegalArgumentException("User, Shift y Date son obligatorios");
		}

		Workshift workshift = new Workshift();

		workshift.setUser(user);
		workshift.setShift(shift);
		workshift.setDate(date);

		return workshift;
	}

	/**
	 * Obtiene el tipo de turno
	 */
	public ShiftType getShiftType() {
		return shift != null ? shift.getShiftType() : null;
	}

	/**
	 * Obtiene la descripción completa del turno
	 */
	public String getFullDescription() {
		if (shift == null || user == null) {
			return "Sin información";
		}
		return String.format("%s - %s (%s)", user.getFullName(), shift.getShiftType().getDisplayName(), date);
	}

	/**
	 * Cuenta el número de palets escaneados en este turno
	 */
	public int getTotalScannedPalets() {
		return palets != null ? palets.size() : 0;
	}

	/**
	 * Cuenta el número de registros de entrada/salida
	 */
	public int getTotalTimesheets() {
		return timesheets != null ? timesheets.size() : 0;
	}

	/**
	 * Verifica si el turno ha sido modificado
	 */
	public boolean hasBeenUpdated() {
		return this.updatedAt != null;
	}

	/**
	 * Verifica si el turno es de hoy
	 */
	public boolean isToday() {
		return date.equals(LocalDate.now());
	}

	/**
	 * Verifica si el turno ya pasó
	 */
	public boolean isPast() {
		return date.isBefore(LocalDate.now());
	}

	/**
	 * Verifica si el turno es futuro
	 */
	public boolean isFuture() {
		return date.isAfter(LocalDate.now());
	}
}
