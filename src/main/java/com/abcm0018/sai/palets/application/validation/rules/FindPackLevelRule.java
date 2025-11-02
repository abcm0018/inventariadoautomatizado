package com.abcm0018.sai.palets.application.validation.rules;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.abcm0018.sai.palets.application.validation.PaletValidationContext;
import com.abcm0018.sai.palets.exceptions.PaletValidationException;
import com.abcm0018.sai.productos.domain.entity.ProductPackLevel;
import com.abcm0018.sai.productos.domain.repository.ProductPackLevelRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REGLA 3: Busca el nivel de empaque (ProductPackLevel) basado en el EAN (GTIN).
 * Esta regla debe usar la caché de Spring (@Cacheable) para alto rendimiento.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FindPackLevelRule implements PaletValidationRule {

	// Inyectamos el repositorio del módulo de Productos
	private final ProductPackLevelRepository productPackLevelRepository;

	@Override
	public void validate(PaletValidationContext context) throws PaletValidationException {

		final String ean = context.getMessage().getEan();

		log.debug("Regla [Producto]: Buscando ProductPackLevel para EAN '{}'", ean);

		Optional<ProductPackLevel> packLevelOpt = productPackLevelRepository.findByGtin(ean);

		// Si no se encuentra...
		if (packLevelOpt.isEmpty()) {
			log.warn("Regla [Producto] Fallida: No se encontró Producto (PackLevel) para EAN '{}'", ean);

			// Lanza la excepción y rechaza el mensaje
			throw new PaletValidationException("Producto (PackLevel) no encontrado para el EAN: " + ean);
		}

		// --- ¡ÉXITO! ENRIQUECEMOS EL CONTEXTO ---
		ProductPackLevel packLevel = packLevelOpt.get();

		// (Opcional: puedes añadir más validaciones aquí, ej. packLevel.isActive())
		if (!packLevel.isValidConfiguration()) {
			log.warn("Regla [Producto] Fallida: El EAN '{}' está inactivo.", ean);
			throw new PaletValidationException("El Producto (PackLevel) con EAN " + ean + " está inactivo.");
		}

		context.setPackLevel(packLevel);

		log.debug("Regla [Producto] OK: EAN encontrado (ID: {}, Producto: {})",
				packLevel.getId(), packLevel.getProduct().getDescription());

	}

	/**
	 * Define el orden de ejecución.
	 * 30 = Se ejecuta después de la regla de Workshift (20).
	 */
	@Override
	public int getOrder() {
		return 30;
	}
}
