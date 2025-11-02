package com.abcm0018.sai.email.domain.events;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LowStockAlertEvent extends ApplicationEvent {
	private Long productId;
	private String productName;
	private Long currentStock;
	private Long minimumStock;
	private String recipientEmail;

	public LowStockAlertEvent(Object source, Long productId, String productName, Long currentStock, Long minimumStock,
			String recipientEmail) {
		super(source);
		this.productId = productId;
		this.productName = productName;
		this.currentStock = currentStock;
		this.minimumStock = minimumStock;
		this.recipientEmail = recipientEmail;
	}

	public LowStockAlertEvent() {
		super("default-source");
	}
}
