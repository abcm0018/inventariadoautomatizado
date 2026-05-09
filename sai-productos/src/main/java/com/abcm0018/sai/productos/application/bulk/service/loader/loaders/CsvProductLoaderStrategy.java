package com.abcm0018.sai.productos.application.bulk.service.loader.loaders;

import com.abcm0018.sai.productos.application.bulk.dtos.ProductBulkImportDTO;
import com.abcm0018.sai.productos.application.bulk.enums.LoaderType;
import com.abcm0018.sai.productos.application.bulk.exceptions.CsvParseException;
import com.abcm0018.sai.productos.application.bulk.exceptions.ProductBulkImportException;
import com.abcm0018.sai.productos.application.bulk.service.loader.ProductLoaderStrategy;
import com.abcm0018.sai.shared.constants.CustomErrorCode;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Implementación del Strategy para cargar productos desde archivos CSV.
 * <p>
 * RESPONSABILIDADES:
 * - Parsear archivos CSV con Apache Commons CSV
 * - Validar estructura y headers
 * - Mapear cada fila a ProductBulkImportDTO
 * - Manejar errores de encoding y formato
 * - Registrar progress y errores
 * <p>
 * FORMATO ESPERADO:
 * brand,formatCode,name,description,manufacturedIn,status,packingLevel,gtin,unitsInLevel,netWeight,heightMM,widthMM
 * ACME,COLA-2L,Refresco Cola 2L,Bebida refrescante,ES,ACTIVE,UNIDAD,5901234123456,1,2.0,300,100
 * <p>
 * NOTAS:
 * - Primera línea es header (se ignora)
 * - Soporta UTF-8 encoding
 * - Delimiter: coma (,)
 * - Quote character: doble comilla (")
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CsvProductLoaderStrategy implements ProductLoaderStrategy {

	private static final char COLUMN_SEPARATOR = ',';

	@Override
	public List<ProductBulkImportDTO> load(InputStream inputStream) {
		log.info("Iniciando carga con CsvProductLoaderStrategy...");

		// Validación 1: Archivo no nulo
		if (inputStream == null) {
			throw new ProductBulkImportException(CustomErrorCode.BAD_REQUEST, "El archivo (InputStream) es nulo.", HttpStatus.BAD_REQUEST);
		}

		try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

			List<ProductBulkImportDTO> dtoList = new CsvToBeanBuilder<ProductBulkImportDTO>(reader)
					.withType(ProductBulkImportDTO.class)
					.withSeparator(COLUMN_SEPARATOR)
					.withIgnoreLeadingWhiteSpace(true)
					.withIgnoreEmptyLine(true)
					.build()
					.parse();

			if (dtoList.isEmpty()) {
				log.warn("El archivo CSV está vacío o no tiene contenido parseable.");
				throw new ProductBulkImportException(CustomErrorCode.BAD_REQUEST, "El archivo CSV está vacío.", HttpStatus.BAD_REQUEST);
			}

			log.info("✅ CSV parseado exitosamente: {} filas procesadas", dtoList.size());
			return dtoList;

		} catch (CsvParseException e) {
			throw e;
		} catch (Exception e) {
			log.error("❌ Error crítico al parsear CSV", e);
			throw new CsvParseException("Error al parsear CSV: " + e.getMessage(), e);
		}
	}

	@Override
	public LoaderType getLoaderType() {
		return LoaderType.CSV;
	}
}
