package com.abcm0018.sai.email.domain.events;

import lombok.Getter;
import lombok.Setter;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

/**
 * Evento publicado cuando hay palets que ya han expirado
 */
@Getter
@Setter
public class PaletExpiredEvent extends ApplicationEvent {
	private String productName;
	private String batchNumber;
	private LocalDate expiryDate;
	private String sscc;
	private String supervisorEmail;

	// Constructor requerido
	public PaletExpiredEvent(Object source, String productName, String batchNumber,
			LocalDate expiryDate, String sscc, String supervisorEmail) {
		super(source);
		this.productName = productName;
		this.batchNumber = batchNumber;
		this.expiryDate = expiryDate;
		this.sscc = sscc;
		this.supervisorEmail = supervisorEmail;
	}

	public PaletExpiredEvent() {
		super("default-source");
	}

}
