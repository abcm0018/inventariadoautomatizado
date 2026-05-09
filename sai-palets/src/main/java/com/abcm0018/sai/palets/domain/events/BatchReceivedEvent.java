package com.abcm0018.sai.palets.domain.events;

import lombok.Getter;
import lombok.Setter;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Evento publicado cuando se recibe un nuevo lote
 */
@Getter
@Setter
public class BatchReceivedEvent extends ApplicationEvent {
	private String employeeName;
	private String batchNumber;
	private Long quantity;
	private LocalDateTime receivedDate;
	private String operatorEmail;

	public BatchReceivedEvent(Object source, String employeeName, String batchNumber,
			Long quantity, LocalDateTime receivedDate, String operatorEmail) {
		super(source);
		this.employeeName = employeeName;
		this.batchNumber = batchNumber;
		this.quantity = quantity;
		this.receivedDate = receivedDate;
		this.operatorEmail = operatorEmail;
	}

	public BatchReceivedEvent() {
		super("default-source");
	}
}
