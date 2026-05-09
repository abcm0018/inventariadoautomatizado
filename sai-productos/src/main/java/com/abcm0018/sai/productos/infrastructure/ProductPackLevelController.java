package com.abcm0018.sai.productos.infrastructure;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.abcm0018.sai.productos.application.dtos.ProductPackLevelDetailResponseDTO;
import com.abcm0018.sai.productos.application.dtos.ProductPackLevelRequestDTO;
import com.abcm0018.sai.productos.application.dtos.ProductPackLevelResponseDTO;
import com.abcm0018.sai.productos.application.dtos.UpdateProductPackLevelDTO;
import com.abcm0018.sai.productos.application.service.ProductPackLevelService;
import com.abcm0018.sai.productos.domain.enums.PackingLevel;
import com.abcm0018.sai.shared.response.ResponseBuilder;
import com.abcm0018.sai.shared.response.StandardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para la gestión de niveles de embalaje (ProductPackLevel)
 * <p>
 * SEGURIDAD:
 * - ADMIN: Acceso completo (CRUD + configuración)
 * - SUPERVISOR: Consultas y lecturas
 * - OPERATOR: Acceso limitado o sin acceso
 * <p>
 * OPERACIONES DISPONIBLES:
 * - CRUD completo de niveles de embalaje
 * - Búsquedas por producto, tipo de embalaje, GTIN
 * - Validaciones de configuración logística
 * - Consultas de auditoría (recientemente creados/modificados)
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/pack-levels")
@RequiredArgsConstructor
@Tag(name = "Product Pack Levels", description = "Endpoints para gestión de niveles de embalaje")
public class ProductPackLevelController {

	private final ProductPackLevelService productPackLevelService;

	/**
	 * Crear un nuevo nivel de embalaje
	 * Solo administradores pueden crear nuevos niveles
	 */
	@CrossOrigin
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Crear nuevo nivel de embalaje",
			description = "Crea un nivel de embalaje con validaciones de configuración logística. Solo administradores pueden crear niveles."
	)
	public StandardResponse<ProductPackLevelDetailResponseDTO> createProductPackLevel(@Valid @RequestBody ProductPackLevelRequestDTO requestDTO) {

		log.info("Creando nuevo ProductPackLevel - GTIN: {}, Producto: {}", requestDTO.getGtin(), requestDTO.getProductId());

		ProductPackLevelDetailResponseDTO created = productPackLevelService.createProductPackLevel(requestDTO);

		return ResponseBuilder.withCreatedElements(HttpStatus.CREATED, true, 1,
				"Nivel de embalaje creado exitosamente", created);
	}

	/**
	 * Obtener un nivel de embalaje por ID
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener nivel de embalaje por ID",
			description = "Retorna información completa del nivel incluyendo cálculos de negocio"
	)
	public StandardResponse<ProductPackLevelDetailResponseDTO> getProductPackLevelById(
			@PathVariable @Parameter(description = "ID del nivel de embalaje") Long id) {

		log.debug("Consultando ProductPackLevel con ID: {}", id);

		ProductPackLevelDetailResponseDTO packLevel = productPackLevelService.getProductPackLevelById(id);

		return ResponseBuilder.with(HttpStatus.OK, true, "Nivel de embalaje encontrado", packLevel);
	}

	/**
	 * Obtener un nivel de embalaje por GTIN
	 * Uso: Búsqueda por escaneo RFID
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/gtin/{gtin}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener nivel de embalaje por GTIN",
			description = "Búsqueda principal: utilizada para escaneo RFID y lectura de códigos logísticos"
	)
	public StandardResponse<ProductPackLevelDetailResponseDTO> getProductPackLevelByGtin(
			@PathVariable @Parameter(description = "Código GTIN-14 del nivel") String gtin) {

		log.debug("Consultando ProductPackLevel por GTIN: {}", gtin);

		ProductPackLevelDetailResponseDTO packLevel = productPackLevelService.getProductPackLevelByGtin(gtin);

		return ResponseBuilder.with(HttpStatus.OK, true, "Nivel de embalaje encontrado", packLevel);
	}

	/**
	 * Listar todos los niveles de embalaje con paginación
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Listar todos los niveles de embalaje",
			description = "Retorna página de niveles ordenados por fecha de creación descendente"
	)
	public StandardResponse<Page<ProductPackLevelResponseDTO>> getAllProductPackLevels(
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		log.debug("Listando ProductPackLevels - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());

		Page<ProductPackLevelResponseDTO> packLevels = productPackLevelService.getAllProductPackLevels(pageable);

		String message = String.format("Página %d de %d (Total: %d niveles)", packLevels.getNumber() + 1, packLevels.getTotalPages(),
				packLevels.getTotalElements());

		return ResponseBuilder.with(HttpStatus.OK, true, message, packLevels);
	}

	/**
	 * Actualizar un nivel de embalaje
	 * Solo administradores pueden actualizar niveles
	 * <p>
	 * Restricciones:
	 * - NO se puede actualizar GTIN
	 * - NO se puede actualizar productId
	 * - NO se puede actualizar packingLevel
	 */
	@CrossOrigin
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Actualizar nivel de embalaje",
			description = "Actualiza propiedades físicas (peso, dimensiones, límites). GTIN, producto y tipo de embalaje NO son actualizables."
	)
	public StandardResponse<ProductPackLevelDetailResponseDTO> updateProductPackLevel(
			@PathVariable @Parameter(description = "ID del nivel a actualizar") Long id,
			@Valid @RequestBody UpdateProductPackLevelDTO updateDTO) {

		log.info("Actualizando ProductPackLevel {}", id);

		ProductPackLevelDetailResponseDTO updated = productPackLevelService.updateProductPackLevel(id, updateDTO);

		return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1,
				"Nivel de embalaje actualizado exitosamente", updated);
	}

	/**
	 * Eliminar un nivel de embalaje
	 * Solo administradores pueden eliminar niveles
	 * <p>
	 * Restricción: No se puede eliminar si tiene palets asociados
	 */
	@CrossOrigin
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Eliminar nivel de embalaje",
			description = "Solo se puede eliminar si no tiene palets asociados en el inventario"
	)
	public StandardResponse<Void> deleteProductPackLevel(@PathVariable @Parameter(description = "ID del nivel a eliminar") Long id) {

		log.info("Eliminando ProductPackLevel {}", id);

		productPackLevelService.deleteProductPackLevel(id);

		return ResponseBuilder.withDeletedElements(HttpStatus.OK, true, 1,
				"Nivel de embalaje eliminado exitosamente");
	}


	/**
	 * Obtener todos los niveles de un producto específico
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/product/{productId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener niveles de un producto",
			description = "Retorna todos los niveles de embalaje disponibles para un producto específico"
	)
	public StandardResponse<List<ProductPackLevelResponseDTO>> getProductPackLevelsByProductId(
			@PathVariable @Parameter(description = "ID del producto") Long productId) {

		log.debug("Consultando ProductPackLevels del Producto ID: {}", productId);

		List<ProductPackLevelResponseDTO> packLevels = productPackLevelService.getProductPackLevelsByProductId(productId);

		String message = String.format("Encontrados %d niveles de embalaje para el producto", packLevels.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, packLevels);
	}

	/**
	 * Contar niveles de un producto
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/product/{productId}/count")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Contar niveles de un producto",
			description = "Retorna la cantidad de niveles de embalaje disponibles"
	)
	public StandardResponse<Long> countProductPackLevelsByProductId(@PathVariable @Parameter(description = "ID del producto") Long productId) {

		log.debug("Contando ProductPackLevels del Producto ID: {}", productId);

		Long count = productPackLevelService.countProductPackLevelsByProductId(productId);

		String message = String.format("El producto tiene %d niveles de embalaje", count);

		return ResponseBuilder.with(HttpStatus.OK, true, message, count);
	}

	/**
	 * Verificar si un producto tiene niveles de embalaje
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/product/{productId}/has-levels")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Verificar si producto tiene niveles",
			description = "Retorna true si el producto tiene al menos un nivel de embalaje configurado"
	)
	public StandardResponse<Boolean> hasProductPackLevels(@PathVariable @Parameter(description = "ID del producto") Long productId) {

		log.debug("Verificando si Producto ID: {} tiene niveles", productId);

		boolean hasLevels = productPackLevelService.hasProductPackLevels(productId);

		String message = hasLevels ? "El producto tiene niveles de embalaje configurados" : "El producto NO tiene niveles de embalaje configurados";

		return ResponseBuilder.with(HttpStatus.OK, true, message, hasLevels);
	}

	/**
	 * Obtener todos los niveles de un tipo específico
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/type/{packingLevel}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener niveles por tipo de embalaje",
			description = "Retorna todos los niveles del tipo especificado (UNIDAD, CAJA, PALET)"
	)
	public StandardResponse<List<ProductPackLevelResponseDTO>> getProductPackLevelsByPackingLevel(
			@PathVariable @Parameter(description = "Tipo de embalaje") PackingLevel packingLevel) {

		log.debug("Consultando ProductPackLevels por Packing Level: {}", packingLevel);

		List<ProductPackLevelResponseDTO> packLevels = productPackLevelService.getProductPackLevelsByPackingLevel(packingLevel);

		String message = String.format("Encontrados %d niveles de tipo %s", packLevels.size(), packingLevel.name());

		return ResponseBuilder.with(HttpStatus.OK, true, message, packLevels);
	}

	/**
	 * Obtener niveles de un tipo específico para un producto
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/product/{productId}/type/{packingLevel}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener niveles de producto por tipo",
			description = "Ej: Obtener solo los palets de un producto específico"
	)
	public StandardResponse<List<ProductPackLevelResponseDTO>> getProductPackLevelsByProductIdAndPackingLevel(
			@PathVariable @Parameter(description = "ID del producto") Long productId,
			@PathVariable @Parameter(description = "Tipo de embalaje") PackingLevel packingLevel) {

		log.debug("Consultando ProductPackLevels - Producto: {}, Packing Level: {}", productId, packingLevel);

		List<ProductPackLevelResponseDTO> packLevels = productPackLevelService.getProductPackLevelsByProductIdAndPackingLevel(productId, packingLevel);

		String message = String.format("Encontrados %d niveles de tipo %s para el producto", packLevels.size(), packingLevel.name());

		return ResponseBuilder.with(HttpStatus.OK, true, message, packLevels);
	}

	/**
	 * Obtener niveles creados recientemente
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/recently-created")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Niveles creados recientemente",
			description = "Retorna los últimos niveles de embalaje creados en el sistema"
	)
	public StandardResponse<List<ProductPackLevelResponseDTO>> getRecentlyCreatedProductPackLevels(
			@RequestParam(defaultValue = "10") @Parameter(description = "Cantidad de registros a retornar") int limit) {

		log.debug("Obteniendo últimos {} ProductPackLevels creados", limit);

		List<ProductPackLevelResponseDTO> packLevels = productPackLevelService.getRecentlyCreatedProductPackLevels(limit);

		String message = String.format("Últimos %d niveles creados obtenidos", packLevels.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, packLevels);
	}

	/**
	 * Obtener niveles modificados recientemente
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/recently-modified")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Niveles modificados recientemente",
			description = "Retorna los últimos niveles de embalaje modificados en el sistema"
	)
	public StandardResponse<List<ProductPackLevelResponseDTO>> getRecentlyModifiedProductPackLevels(
			@RequestParam(defaultValue = "10") @Parameter(description = "Cantidad de registros a retornar") int limit) {

		log.debug("Obteniendo últimos {} ProductPackLevels modificados", limit);

		List<ProductPackLevelResponseDTO> packLevels = productPackLevelService.getRecentlyModifiedProductPackLevels(limit);

		String message = String.format("Últimos %d niveles modificados obtenidos", packLevels.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, packLevels);
	}

	/**
	 * Verificar si un GTIN es único
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/exists/gtin/{gtin}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Verificar disponibilidad de GTIN",
			description = "Retorna true si el GTIN ya existe en el sistema"
	)
	public StandardResponse<Boolean> checkGtinExists(@PathVariable @Parameter(description = "Código GTIN") String gtin) {

		log.debug("Verificando disponibilidad de GTIN: {}", gtin);

		boolean exists = !productPackLevelService.isGtinUnique(gtin);

		String message = exists ? "El GTIN ya está registrado en el sistema" : "El GTIN está disponible";

		return ResponseBuilder.with(HttpStatus.OK, true, message, exists);
	}

}
