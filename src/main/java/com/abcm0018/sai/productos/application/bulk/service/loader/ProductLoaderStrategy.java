package com.abcm0018.sai.productos.application.bulk.service.loader;

import com.abcm0018.sai.productos.application.bulk.dtos.ProductBulkImportDTO;
import com.abcm0018.sai.productos.application.bulk.enums.LoaderType;
import com.abcm0018.sai.productos.application.bulk.exceptions.CsvParseException;
import com.abcm0018.sai.productos.application.bulk.exceptions.ProductBulkImportException;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * Interfaz Strategy para cargar productos desde diferentes formatos de archivo.
 * <p>
 * PATRÓN: Strategy Pattern
 * - Cada formato de archivo tiene su propia implementación
 * - El Context selecciona la estrategia correcta según el tipo de archivo
 * - Fácil agregar nuevos formatos sin modificar código existente
 * <p>
 * IMPLEMENTACIONES:
 * - CsvProductLoader: Parsea archivos CSV
 * - JsonProductLoader: (Futuro) Parsea archivos JSON
 * - ExcelProductLoader: (Futuro) Parsea archivos Excel
 */
public interface ProductLoaderStrategy {

	/**
	 * Carga productos desde un InputStream y retorna DTOs parseados.
	 *
	 * @param inputStream El stream de datos del archivo
	 * @return Lista de ProductBulkImportDTO parseados
	 * @throws ProductBulkImportException si hay error al parsear
	 */
	List<ProductBulkImportDTO> load(InputStream inputStream) throws ProductBulkImportException;

	/**
	 * Devuelve el tipo de loader que esta estrategia implementa.
	 *
	 * @return LoaderType (ej. LoaderType.CSV)
	 */
	LoaderType getLoaderType();
}