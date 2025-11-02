package com.abcm0018.sai.productos.application.bulk.events;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

/**
 * Evento que se publica cuando un archivo de importación
 * ha sido validado, guardado y está listo para ser procesado por un worker asíncrono.
 */
@Getter
public class BulkImportStartedEvent extends ApplicationEvent {

	private final UUID jobId;

	public BulkImportStartedEvent(Object source, UUID jobId) {
		super(source);
		this.jobId = jobId;
	}
}
