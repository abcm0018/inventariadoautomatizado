package com.abcm0018.sai.users.domain.entity;

import com.abcm0018.sai.palets.domain.entity.Palet;
import com.abcm0018.sai.users.domain.enums.Role;
import com.abcm0018.sai.workshift.domain.entity.Workshift;
import com.abcm0018.sai.workshift.domain.entity.WorkshiftSwapRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Entidad que representa un usuario del sistema.
 * Implementa UserDetails para integración con Spring Security.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
		name = "USERS",
		indexes = {
				@Index(name = "idx_employee_number", columnList = "EMPLOYEE_NUMBER"),
				@Index(name = "idx_email", columnList = "EMAIL"),
				@Index(name = "idx_role", columnList = "ROLE"),
				@Index(name = "idx_active", columnList = "ACTIVE")
		}
)
public class User implements UserDetails, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	// ========== IDENTIFICACIÓN ==========

	@Column(name = "EMPLOYEE_NUMBER", nullable = false, unique = true, length = 20)
	private String employeeNumber;

	@Column(name = "NAME", nullable = false, length = 100)
	private String name;

	@Column(name = "SURNAME", nullable = false, length = 100)
	private String surname;

	@Column(name = "EMAIL", nullable = false, unique = true, length = 150)
	private String email;

	// ========== SEGURIDAD ==========

	@JsonIgnore // NUNCA exponer en API REST
	@Column(name = "PASSWORD", nullable = false)
	private String password; // Hasheada con BCrypt

	@Enumerated(EnumType.STRING)
	@Column(name = "ROLE", nullable = false, length = 20)
	private Role role;

	// ========== INFORMACIÓN LABORAL ==========

	@Column(name = "JOB_POSITION", nullable = false, length = 100)
	private String jobPosition;

	@Column(name = "REGISTRATION_DATE", nullable = false)
	@Builder.Default
	private LocalDate registrationDate = LocalDate.now();

	// ========== ESTADO DE LA CUENTA ==========

	@Column(name = "ACTIVE", nullable = false)
	@Builder.Default
	private boolean active = true;

	@Column(name = "BLOCKED", nullable = false)
	@Builder.Default
	private boolean blocked = false;

	@Column(name = "EXPIRED", nullable = false)
	@Builder.Default
	private boolean expired = false;

	// ========== AUDITORÍA ==========

	@Column(name = "CREATE_AT", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "UPDATE_AT")
	private LocalDateTime updatedAt;

	// ========== RELACIONES ==========

	/**
	 * Turnos asignados al usuario (Workshift, NO Shift)
	 * Shift es el catálogo, Workshift es la asignación específica
	 */
	@JsonIgnore
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<Workshift> workshifts = new ArrayList<>();

	/**
	 * Palets escaneados por este usuario
	 */
	@JsonIgnore
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<Palet> scannedPalets = new ArrayList<>();

	@JsonIgnore
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<WorkshiftSwapRequest> swapRequests = new ArrayList<>();

	// ========== LIFECYCLE CALLBACKS ==========

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = null;

		if (this.registrationDate == null) {
			this.registrationDate = LocalDate.now();
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	// ========== MÉTODOS DE UTILIDAD ==========

	/**
	 * Obtiene el nombre completo del usuario
	 */
	public String getFullName() {
		return name + " " + surname;
	}

	/**
	 * Verifica si el usuario ha sido modificado
	 */
	public boolean hasBeenUpdated() {
		return this.updatedAt != null;
	}

	/**
	 * Obtiene el número total de palets escaneados por este usuario
	 */
	public int getTotalScannedPalets() {
		return scannedPalets != null ? scannedPalets.size() : 0;
	}

	/**
	 * Obtiene el número total de turnos asignados
	 */
	public int getTotalWorkshifts() {
		return workshifts != null ? workshifts.size() : 0;
	}

	/**
	 * Verifica si el usuario tiene un turno asignado hoy
	 */
	public boolean hasWorkshiftToday() {
		if (workshifts == null || workshifts.isEmpty()) {
			return false;
		}
		LocalDate today = LocalDate.now();
		return workshifts.stream()
				.anyMatch(ws -> ws.getDate().equals(today));
	}

	/**
	 * Obtiene el workshift de hoy (si existe)
	 */
	public Workshift getTodayWorkshift() {
		if (workshifts == null || workshifts.isEmpty()) {
			return null;
		}
		LocalDate today = LocalDate.now();
		return workshifts.stream()
				.filter(ws -> ws.getDate().equals(today))
				.findFirst()
				.orElse(null);
	}

	// ========== SPRING SECURITY - UserDetails ==========

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getUsername() {
		return employeeNumber; // Se autentica con número de empleado
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean isAccountNonExpired() {
		return !expired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return !blocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true; // Puedes implementar lógica de expiración de contraseña
	}

	@Override
	public boolean isEnabled() {
		return active;
	}
}
