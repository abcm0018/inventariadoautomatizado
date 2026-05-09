package com.abcm0018.sai.productos.application.bulk.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.abcm0018.sai.productos.application.bulk.dtos.BulkImportErrorDTO;
import com.abcm0018.sai.productos.application.bulk.exceptions.ProductBulkImportException;
import com.abcm0018.sai.shared.constants.CustomErrorCode;

@Service
public class ErrorFileGeneratorService {

	// Cabeceras del fichero de errores
	private static final String[] HEADERS = {"Fila", "TipoError", "Mensaje"};

	public InputStream generateErrorCsv(List<BulkImportErrorDTO> errors) {
		// Usamos un stream en memoria (ByteArrayOutputStream) para generar el fichero
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				CSVPrinter csvPrinter = new CSVPrinter(
						new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)),
						CSVFormat.DEFAULT.builder().setHeader(HEADERS).build()
				)) {

				// Escribimos el BOM de UTF-8 para que Excel pueda abrir el fichero sin problemas
			out.write(0xEF);
			out.write(0xBB);
			out.write(0xBF);

			// Escribimos cada fila de error
			for (BulkImportErrorDTO error : errors) {
				csvPrinter.printRecord(
						error.getRowNumber() != null ? error.getRowNumber() : "N/A",
						error.getType() != null ? error.getType() : "GENERAL",
						error.getErrorMessage()
				);
			}

			// Volcamos el buffer del printer al stream
			csvPrinter.flush();

			// Devolver el contenido del stream como un InputStream
			return new ByteArrayInputStream(out.toByteArray());
		} catch (IOException e) {
			throw new ProductBulkImportException(CustomErrorCode.INTERNAL_SERVER_ERROR,
					"Error al guardar el fichero CSV de errores", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
