package com.abcm0018.sai.email.domain.events;

import lombok.Getter;
import lombok.Setter;

import org.springframework.context.ApplicationEvent;

/**
 * Evento publicado cuando se solicita reseteo de contraseña
 */
@Getter
@Setter
public class PasswordResetEvent extends ApplicationEvent {
	private String name;
	private String surname;
	private String temporaryPassword;
	private String userEmail;

	public PasswordResetEvent(Object source, String name, String surname,
			String userEmail, String temporaryPassword) {
		super(source);
		this.name = name;
		this.surname = surname;
		this.userEmail = userEmail;
		this.temporaryPassword = temporaryPassword;
	}

	public PasswordResetEvent() {
		super("default-source");
	}
}
