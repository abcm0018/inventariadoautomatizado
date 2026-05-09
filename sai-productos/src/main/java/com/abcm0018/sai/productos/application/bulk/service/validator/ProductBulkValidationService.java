package com.abcm0018.sai.productos.application.bulk.service.validator;

import com.abcm0018.sai.productos.application.bulk.dtos.BulkImportErrorDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ProductWithPackLevelsDTO;
import com.abcm0018.sai.productos.application.bulk.dtos.ValidationResultDTO;
import com.abcm0018.sai.productos.application.bulk.service.validator.rules.impl.ProductValidationRule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de validación de negocio (Refactorizado con Patrón Strategy).
 * <p>
 * Esta clase actúa como el "Contexto" del patrón.
 * <p>
 * RESPONSABILIDADES:
 * - Orquestar la ejecución de todas las reglas de validación.
 * - Inyectar todas las implementaciones de 'ProductValidationRule'.
 * - Recopilar los resultados (productos válidos y errores).
 * <p>
 * NO contiene lógica de reglas de negocio; solo las invoca.
 */
@Slf4j
@Service
@RequiredArgsConstructor // Lombok inyecta la lista de reglas
public class ProductBulkValidationService {

	/**
	 * Inyección de Estrategias (Reglas).
	 * Spring detectará automáticamente todos los Beans que implementen
	 * la interfaz 'ProductValidationRule' y los inyectará aquí
	 * en una lista.
	 * <p>
	 * Gracias a @Order en las reglas, podemos controlar su secuencia.
	 */
	private final List<ProductValidationRule> validationRules;

	/**
	 * Valida un lote de productos ejecutando todas las reglas
	 * de validación inyectadas.
	 *
	 * @param products lista de ProductWithPackLevelsDTO (normalizados)
	 * @return ValidationResultDTO (con productos válidos + errores)
	 */
//	@Override
	public ValidationResultDTO validate(List<ProductWithPackLevelsDTO> products) {
		log.info("🔍 Iniciando validación de negocio para {} productos con {} reglas.", products.size(), validationRules.size());

		ValidationResultDTO result = new ValidationResultDTO();
		int productIndex = 0;

		for (ProductWithPackLevelsDTO product : products) {
			productIndex++;
			log.debug("Validando producto idx={}: {}", productIndex, product.getProductKey());

			// Acumulador de errores para *este* producto
			List<BulkImportErrorDTO> errorsForThisProduct = new ArrayList<>();

			// 2. Ejecutamos cada regla (Strategy)
			for (ProductValidationRule rule : validationRules) {
				try {
					// El DTO de error debe saber a qué fila/producto pertenece
					final int currentRow = productIndex;
					// Validamos y recolectamos errores
					List<BulkImportErrorDTO> ruleErrors = rule.validate(product).stream()
							.peek(error -> error.setRowNumber(currentRow)) // Asignamos el nº de fila
							.toList();

					if (!ruleErrors.isEmpty()) {
						errorsForThisProduct.addAll(ruleErrors);
					}

				} catch (Exception e) {
					// Captura para errores inesperados *dentro* de una regla
					log.error("Error crítico ejecutando regla {} en producto {}: {}",
							rule.getClass().getSimpleName(), product.getProductKey(), e.getMessage(), e);
					errorsForThisProduct.add(BulkImportErrorDTO.builder()
							.rowNumber(productIndex)
							.errorMessage("Error interno en regla de validación: " + rule.getClass().getSimpleName())
							.type("VALIDATION_RULE_CRASH")
							.build());
				}
			}

			// 3. Decidimos el destino del producto
			if (errorsForThisProduct.isEmpty()) {
				log.debug("✅ Producto {} validado correctamente.", product.getProductKey());
				result.addValidProduct(product);
			} else {
				log.warn("⚠️ Producto {} tiene {} error(es).", product.getProductKey(), errorsForThisProduct.size());
				result.addErrors(errorsForThisProduct);
			}
		}

		log.info("✅ Validación completada: {} válidos, {} errores.", result.getValidCount(), result.getErrorCount());
		return result;
	}
}
