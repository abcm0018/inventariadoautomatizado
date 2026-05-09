package com.abcm0018.sai.productos.infrastructure.bulk.storage;

import com.abcm0018.sai.productos.exceptions.ProductServiceException;
import com.abcm0018.sai.shared.constants.CustomErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Excepción personalizada para errores relacionados con el
 * almacenamiento de archivos (ej. no se pudo guardar, no se pudo leer).
 * <p>
 * (Refactorizada para extender ProductServiceException y
 * seguir el patrón de manejo de errores del proyecto).
 */
public class StorageServiceException extends ProductServiceException {

	/**
	 * Constructor para un error de almacenamiento con un mensaje.
	 * Automáticamente, asigna el código de error 500.
	 *
	 * @param message Mensaje descriptivo del error.
	 */
	public StorageServiceException(String message) {
		// Llama al constructor de ProductServiceException
		super(CustomErrorCode.INTERNAL_SERVER_ERROR, message, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Constructor para un error de almacenamiento con un mensaje y una causa (excepción original).
	 * Automáticamente, asigna el código de error 500.
	 *
	 * @param message Mensaje descriptivo del error.
	 * @param cause   Excepción original (ej. IOException).
	 */
	public StorageServiceException(String message, Throwable cause) {
		// Llama al constructor de ProductServiceException
		super(CustomErrorCode.INTERNAL_SERVER_ERROR, message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
	}
}