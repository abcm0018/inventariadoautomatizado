package com.abcm0018.sai.palets.domain.events;

import lombok.Getter;
import lombok.Setter;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

/**
 * Evento publicado cuando hay palets próximos a caducar
 */
@Getter
@Setter
public class PaletExpiryAlertEvent extends ApplicationEvent {
	private String productName;
	private String batchNumber;
	private LocalDate expiryDate;
	private Integer daysRemaining;
	private String recipientEmail;

	public PaletExpiryAlertEvent(Object source, String productName, String batchNumber,
			LocalDate expiryDate, Integer daysRemaining, String recipientEmail) {
		super(source);
		this.productName = productName;
		this.batchNumber = batchNumber;
		this.expiryDate = expiryDate;
		this.daysRemaining = daysRemaining;
		this.recipientEmail = recipientEmail;
	}

	public PaletExpiryAlertEvent() {
		super("default-source");
	}
}
