package com.abcm0018.sai.email.domain.enums;

import lombok.Getter;

@Getter
public enum EmailStatus {

	SENT("Enviado correctamente"),
	PENDING("Pendiente de envío"),
	FAILED("Fallo en el envío"),
	BOUNCED("Email rechazado"),
	SPAM("Marcado como spam");

	private final String description;

	EmailStatus(String description) {
		this.description = description;
	}

}
