package com.abcm0018.sai.email.domain.events;

import lombok.Getter;
import lombok.Setter;

import org.springframework.context.ApplicationEvent;

/**
 * Evento publicado cuando el stock de un producto es CRÍTICO (urgente)
 */
@Getter
@Setter
public class CriticalStockAlertEvent extends ApplicationEvent {
	private Long productId;
	private String productName;
	private Long currentStock;
	private String supervisorEmail;

	public CriticalStockAlertEvent(Object source, Long productId, String productName, Long currentStock, String supervisorEmail) {
		super(source);
		this.productId = productId;
		this.productName = productName;
		this.currentStock = currentStock;
		this.supervisorEmail = supervisorEmail;
	}

	public CriticalStockAlertEvent() {
		super("default-source");
	}
}
