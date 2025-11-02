package com.abcm0018.sai.email.domain.events;

import lombok.Getter;
import lombok.Setter;

import org.springframework.context.ApplicationEvent;

/**
 * Evento publicado cuando se registra un nuevo usuario
 */
@Getter
@Setter
public class UserCreatedEvent extends ApplicationEvent {

	private Long userId;
	private String name;
	private String surname;
	private String employeeNumber;
	private String temporaryPassword;
	private String userEmail;

	public UserCreatedEvent(Object source, Long userId, String name, String surname,
			String employeeNumber, String userEmail, String temporaryPassword) {
		super(source);
		this.userId = userId;
		this.name = name;
		this.surname = surname;
		this.employeeNumber = employeeNumber;
		this.userEmail = userEmail;
		this.temporaryPassword = temporaryPassword;
	}

	public UserCreatedEvent() {
		super("default-source");
	}
}