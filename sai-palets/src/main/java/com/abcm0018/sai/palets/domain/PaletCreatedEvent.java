package com.abcm0018.sai.palets.domain;

import lombok.Getter;

/**
 * Evento de dominio que se publica cuando un Palet
 * ha sido creado y persistido en la base de datos con éxito
 * <p>
 * Contiene solo el ID para mantener el evento "ligero"
 */
@Getter
public class PaletCreatedEvent {

	private final Long paletId;

	public PaletCreatedEvent(Long paletId) {
		this.paletId = paletId;
	}
}
