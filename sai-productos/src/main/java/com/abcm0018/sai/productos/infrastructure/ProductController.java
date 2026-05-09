package com.abcm0018.sai.productos.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.abcm0018.sai.productos.application.dtos.*;
import com.abcm0018.sai.productos.domain.enums.ProductCountry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.abcm0018.sai.productos.application.service.ProductService;
import com.abcm0018.sai.productos.domain.enums.ProductStatus;
import com.abcm0018.sai.shared.response.ResponseBuilder;
import com.abcm0018.sai.shared.response.StandardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para la gestión de productos
 * <p>
 * SEGURIDAD:
 * - ADMIN: Acceso completo (CRUD + configuración)
 * - SUPERVISOR: Consultas y lecturas
 * - OPERATOR: Acceso limitado o sin acceso
 * <p>
 * OPERACIONES DISPONIBLES:
 * - CRUD completo de productos
 * - Búsquedas por identificadores, estado, manufactura
 * - Búsquedas de niveles de embalaje
 * - Validaciones de producto
 * - Consultas de auditoría (productos creados)
 * - Búsqueda avanzada con filtros
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Endpoints para gestión de productos")
public class ProductController {

	private final ProductService productService;

	/**
	 * Crear un nuevo producto
	 * Solo administradores pueden crear productos
	 */
	@CrossOrigin
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Crear nuevo producto",
			description = "Crea un producto con validación de unicidad en brand+formatCode. Solo administradores pueden crear productos."
	)
	public StandardResponse<ProductDetailResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO requestDTO) {

		log.info("Creando nuevo producto - Brand: {}, FormatCode: {}", requestDTO.getBrand(), requestDTO.getFormatCode());

		ProductDetailResponseDTO created = productService.createProduct(requestDTO);

		return ResponseBuilder.withCreatedElements(HttpStatus.CREATED, true, 1, "Producto creado exitosamente", created);
	}

	/**
	 * Obtener un producto por ID
	 * Supervisores y administradores pueden ver cualquier producto
	 */
	@CrossOrigin
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener producto por ID",
			description = "Retorna información completa del producto con detalles de niveles de embalaje"
	)
	public StandardResponse<ProductDetailResponseDTO> getProductById(@PathVariable @Parameter(description = "ID del producto") Long id) {

		log.debug("Consultando producto con ID: {}", id);

		ProductDetailResponseDTO product = productService.getProductById(id);

		return ResponseBuilder.with(HttpStatus.OK, true, "Producto encontrado", product);
	}

	/**
	 * Listar todos los productos con paginación
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Listar todos los productos",
			description = "Retorna página de productos ordenados por fecha de creación descendente"
	)
	public StandardResponse<Page<ProductResponseDTO>> getAllProducts(
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		log.debug("Listando productos - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());

		Page<ProductResponseDTO> products = productService.getAllProducts(pageable);

		String message = String.format("Página %d de %d (Total: %d productos)", products.getNumber() + 1, products.getTotalPages(), products.getTotalElements());

		return ResponseBuilder.with(HttpStatus.OK, true, message, products);
	}

	/**
	 * Actualizar un producto existente
	 * Solo administradores pueden actualizar productos
	 * <p>
	 * Restricciones:
	 * - NO se pueden actualizar: name, brand, formatCode, manufacturedIn
	 * - Se pueden actualizar: description, status
	 */
	@CrossOrigin
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Actualizar producto",
			description = "Actualiza solo description y status. Name, brand, formatCode y manufacturedIn son inmutables."
	)
	public StandardResponse<ProductDetailResponseDTO> updateProduct(
			@PathVariable @Parameter(description = "ID del producto") Long id,
			@Valid @RequestBody UpdateProductRequestDTO updateDTO) {

		log.info("Actualizando producto {}", id);

		ProductDetailResponseDTO updated = productService.updateProduct(id, updateDTO);

		return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Producto actualizado exitosamente", updated);
	}

	/**
	 * Eliminar un producto
	 * Solo administradores pueden eliminar productos
	 * <p>
	 * Restricción: No se puede eliminar si tiene niveles de embalaje asociados
	 */
	@CrossOrigin
	@DeleteMapping("/{id}/permanent")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Eliminar producto",
			description = "Solo se puede eliminar si no tiene niveles de embalaje configurados"
	)
	public StandardResponse<Void> deleteProduct(
			@PathVariable @Parameter(description = "ID del producto") Long id) {

		log.info("Eliminando producto {}", id);

		productService.deleteProduct(id);

		return ResponseBuilder.withDeletedElements(HttpStatus.OK, true, 1, "Producto eliminado exitosamente");
	}

	/**
	 * Desactivar un producto (soft delete)
	 * Marca al producto como discontinuo pero mantiene sus datos
	 */
	@CrossOrigin
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Desactivar producto",
			description = "Soft delete: marca al producto como discontinuo. "
	)
	public StandardResponse<Void> deleteSoftProduct(
			@PathVariable @Parameter(description = "ID del producto") Long id) {

		log.info("Desactivando producto {}", id);

		productService.deleteSoftProduct(id);

		return ResponseBuilder.withDeletedElements(
				HttpStatus.OK,
				true,
				1,
				"Producto desactivado exitosamente"
		);
	}

	/**
	 * Obtener producto por brand y formatCode
	 * Búsqueda principal (constraint único)
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/search/brand-format/{brand}/{formatCode}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Buscar producto por brand y formatCode",
			description = "Búsqueda principal utilizando el constraint único"
	)
	public StandardResponse<ProductDetailResponseDTO> getProductByBrandAndFormatCode(
			@PathVariable @Parameter(description = "Marca del producto") String brand, @PathVariable @Parameter(description = "Código de formato") String formatCode) {

		log.debug("Buscando producto - Brand: {}, FormatCode: {}", brand, formatCode);

		ProductDetailResponseDTO product = productService.getProductByBrandAndFormatCode(brand, formatCode);

		return ResponseBuilder.with(HttpStatus.OK, true, "Producto encontrado", product);
	}

	/**
	 * Buscar productos por nombre
	 * Búsqueda parcial, case-insensitive
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/search/name")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Buscar productos por nombre",
			description = "Búsqueda parcial (case-insensitive) en el nombre del producto"
	)
	public StandardResponse<List<ProductResponseDTO>> getProductsByName(@RequestParam @Parameter(description = "Término de búsqueda") String name) {

		log.debug("Buscando productos por nombre: {}", name);

		List<ProductResponseDTO> products = productService.getProductsByName(name);

		String message = String.format("Encontrados %d productos con nombre '%s'", products.size(), name);

		return ResponseBuilder.with(HttpStatus.OK, true, message, products);
	}

	/**
	 * Buscar productos por marca
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/search/brand/{brand}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Buscar productos por marca",
			description = "Retorna todos los productos de una marca específica"
	)
	public StandardResponse<List<ProductResponseDTO>> getProductsByBrand(@PathVariable @Parameter(description = "Marca a buscar") String brand) {

		log.debug("Buscando productos por marca: {}", brand);

		List<ProductResponseDTO> products = productService.getProductsByBrand(brand);

		String message = String.format("Encontrados %d productos de la marca '%s'", products.size(), brand);

		return ResponseBuilder.with(HttpStatus.OK, true, message, products);
	}

	/**
	 * Buscar productos por código de formato
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/search/format-code/{formatCode}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Buscar productos por código de formato",
			description = "Retorna todos los productos con ese código de formato"
	)
	public StandardResponse<List<ProductResponseDTO>> getProductsByFormatCode(
			@PathVariable @Parameter(description = "Código de formato") String formatCode) {

		log.debug("Buscando productos por formatCode: {}", formatCode);

		List<ProductResponseDTO> products = productService.getProductsByFormatCode(formatCode);

		String message = String.format("Encontrados %d productos con formato '%s'", products.size(), formatCode);

		return ResponseBuilder.with(HttpStatus.OK, true, message, products);
	}

	/**
	 * Obtener productos por estado
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/status/{status}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener productos por estado",
			description = "Retorna productos con estado específico (ACTIVE, DISCONTINUED, TEMPORARY_OUT, PILOT)"
	)
	public StandardResponse<List<ProductResponseDTO>> getProductsByStatus(
			@PathVariable @Parameter(description = "Estado del producto") ProductStatus status) {

		log.debug("Buscando productos por estado: {}", status);

		List<ProductResponseDTO> products = productService.getProductsByStatus(status);

		String message = String.format("Encontrados %d productos en estado %s", products.size(), status.name());

		return ResponseBuilder.with(HttpStatus.OK, true, message, products);
	}

	/**
	 * Contar productos por estado
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/status/{status}/count")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Contar productos por estado",
			description = "Retorna la cantidad de productos en un estado específico"
	)
	public StandardResponse<Long> countByStatus(
			@PathVariable @Parameter(description = "Estado a contar") ProductStatus status) {

		log.debug("Contando productos por estado: {}", status);

		Long count = productService.countByStatus(status);

		String message = String.format("Hay %d productos en estado %s", count, status.name());

		return ResponseBuilder.with(HttpStatus.OK, true, message, count);
	}

	/**
	 * Obtener productos por país de manufactura
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/manufactured-in/{country}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener productos por país de manufactura",
			description = "Retorna todos los productos fabricados en un país específico"
	)
	public StandardResponse<List<ProductResponseDTO>> getProductsByManufacturedIn(
			@PathVariable @Parameter(description = "País de manufactura") String country) {

		log.debug("Buscando productos fabricados en: {}", country);

		List<ProductResponseDTO> products = productService.getProductsByManufacturedIn(country);

		String message = String.format("Encontrados %d productos fabricados en %s", products.size(), country);

		return ResponseBuilder.with(HttpStatus.OK, true, message, products);
	}

	/**
	 * Obtener productos con niveles de embalaje configurados
	 * Productos completos, listos para producción
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/with-pack-levels")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener productos con niveles de embalaje",
			description = "Retorna productos que tienen al menos un nivel de embalaje configurado"
	)
	public StandardResponse<List<ProductResponseDTO>> getProductsWithPackLevels() {

		log.debug("Obteniendo productos con niveles de embalaje");

		List<ProductResponseDTO> products = productService.getProductsWithPackLevels();

		String message = String.format("Encontrados %d productos con niveles de embalaje configurados", products.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, products);
	}

	/**
	 * Obtener productos SIN niveles de embalaje configurados
	 * Productos incompletos, requieren configuración
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/without-pack-levels")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener productos sin niveles de embalaje",
			description = "Retorna productos que NO tienen niveles de embalaje configurados (incompletos)"
	)
	public StandardResponse<List<ProductResponseDTO>> getProductsWithoutPackLevels() {

		log.debug("Obteniendo productos sin niveles de embalaje");

		List<ProductResponseDTO> products = productService.getProductsWithoutPackLevels();

		String message = String.format("Encontrados %d productos sin niveles de embalaje configurados", products.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, products);
	}

	/**
	 * Obtener productos creados en un rango de fechas
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/created-between")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener productos creados entre fechas",
			description = "Retorna productos creados en un rango de fechas específico (para auditoría)"
	)
	public StandardResponse<List<ProductResponseDTO>> getProductsCreatedBetween(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			@Parameter(description = "Fecha de inicio (inclusive)") LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			@Parameter(description = "Fecha de fin (inclusive)") LocalDate endDate) {

		log.debug("Buscando productos creados entre {} y {}", startDate, endDate);

		List<ProductResponseDTO> products = productService.getProductsCreatedBetween(startDate, endDate);

		String message = String.format("Encontrados %d productos creados entre %s y %s", products.size(), startDate, endDate);

		return ResponseBuilder.with(HttpStatus.OK, true, message, products);
	}

	/**
	 * Búsqueda avanzada con filtros múltiples
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/search/advanced")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Búsqueda avanzada con filtros",
			description = "Permite combinar múltiples criterios: estado, marca, nombre, país (todos opcionales)"
	)
	public StandardResponse<Page<ProductResponseDTO>> findWithFilters(
			@RequestParam(required = false)
			@Parameter(description = "Estado del producto (opcional)") ProductStatus status,
			@RequestParam(required = false)
			@Parameter(description = "Marca (búsqueda parcial, opcional)") String brand,
			@RequestParam(required = false)
			@Parameter(description = "Nombre (búsqueda parcial, opcional)") String name,
			@RequestParam(required = false)
			@Parameter(description = "País de manufactura (opcional)") String country,
			@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
			Pageable pageable) {

		log.debug("Búsqueda avanzada - Status: {}, Brand: {}, Name: {}, Country: {}", status, brand, name, country);

		Page<ProductResponseDTO> products = productService.findWithFilters(status, brand, name, country, pageable);

		String message = String.format("Encontrados %d productos que cumplen los filtros", products.getTotalElements());

		return ResponseBuilder.with(HttpStatus.OK, true, message, products);
	}

	/**
	 * Verificar si existe un producto con brand y formatCode específico
	 * Acceso: Supervisores y administradores
	 */
	@CrossOrigin
	@GetMapping("/exists/brand-format/{brand}/{formatCode}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Verificar existencia de producto",
			description = "Retorna true si existe un producto con esa combinación de brand+formatCode"
	)
	public StandardResponse<Boolean> checkProductExists(
			@PathVariable @Parameter(description = "Marca del producto") String brand,
			@PathVariable @Parameter(description = "Código de formato") String formatCode) {

		log.debug("Verificando existencia - Brand: {}, FormatCode: {}", brand, formatCode);

		boolean exists = productService.existsByBrandAndFormatCode(brand, formatCode);

		String message = exists ? "El producto ya existe en el sistema" : "El producto no existe";

		return ResponseBuilder.with(HttpStatus.OK, true, message, exists);
	}

	@CrossOrigin
	@GetMapping("/status")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Listar estados disponibles",
			description = "Obtiene los estados de cuenta con su valor técnico y nombre amigable"
	)
	public StandardResponse<ProductStatusResponse> getAvailableStatus() {
		Set<String> statusStr = ProductStatus.getProductStatus();
		return ResponseBuilder.with(HttpStatus.OK, true, "Estados recuperados correctamente", new ProductStatusResponse(statusStr));
	}

	@CrossOrigin
	@GetMapping("/countries") // Endpoint: /api/v1/users/status
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Listar paises disponibles",
			description = "Obtiene los estados de cuenta con su valor técnico y nombre amigable"
	)
	public StandardResponse<ProductCountryResponse> getAvailableCountries() {
		log.info("Petición REST para obtener todos los estados disponibles");

		List<String> countriesStr = ProductCountry.getProductCountries().stream().map(status -> status.name()  + "|" + status.getDisplayCountry()).toList();

		return ResponseBuilder.with(HttpStatus.OK, true, "Estados recuperados correctamente", new ProductCountryResponse(countriesStr));
	}

	@CrossOrigin
	@GetMapping("/brand") // Endpoint: /api/v1/users/status
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Listar las marcas disponibles",
			description = "Obtiene las marcas de cuenta con su valor técnico y nombre amigable"
	)
	public StandardResponse<ProductBrandResponse> getAvailableBrand() {
		log.info("Petición REST para obtener todos las marcas de productos disponibles");

		ProductBrandResponse brands = productService.getAllBrandsProducts();

		return ResponseBuilder.with(HttpStatus.OK, true, "Marcas recuperadas correctamente", brands);
	}



}