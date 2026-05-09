package com.abcm0018.sai.users.domain.entity;

import com.abcm0018.sai.users.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Entidad que representa un usuario del sistema.
 * Implementa UserDetails para integración con Spring Security.
 */
@Getter
@Setter
@ToString
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

	@Column(name = "EMPLOYEE_NUMBER", nullable = false, unique = true, length = 20)
	private String employeeNumber;

	@Column(name = "NAME", nullable = false, length = 100)
	private String name;

	@Column(name = "SURNAME", nullable = false, length = 100)
	private String surname;

	@Column(name = "EMAIL", nullable = false, unique = true, length = 150)
	private String email;

	@JsonIgnore // NUNCA exponer en API REST
	@Column(name = "PASSWORD", nullable = false)
	private String password; // Hasheada con BCrypt

	@Enumerated(EnumType.STRING)
	@Column(name = "ROLE", nullable = false, length = 20)
	private Role role;

	@Column(name = "JOB_POSITION", nullable = false, length = 100)
	private String jobPosition;

	@Column(name = "REGISTRATION_DATE", nullable = false)
	private LocalDate registrationDate = LocalDate.now();

	@Column(name = "ACTIVE", nullable = false)
	private boolean active = true;

	@Column(name = "INACTIVE", nullable = false)
	private boolean inactive = false;

	@Column(name = "BLOCKED", nullable = false)
	private boolean blocked = false;

	@Column(name = "EXPIRED", nullable = false)
	private boolean expired = false;

	@Column(name = "CREATE_AT", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "UPDATE_AT")
	private LocalDateTime updatedAt;

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
