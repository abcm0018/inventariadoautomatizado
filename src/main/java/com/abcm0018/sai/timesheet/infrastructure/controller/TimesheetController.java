package com.abcm0018.sai.timesheet.infrastructure.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abcm0018.sai.shared.response.ResponseBuilder;
import com.abcm0018.sai.shared.response.StandardResponse;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetDetailResponseDTO;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetRequestDTO;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetResponseDTO;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetStatisticsDTO;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetSummaryDTO;
import com.abcm0018.sai.timesheet.application.service.TimesheetService;
import com.abcm0018.sai.timesheet.domain.enums.TimesheetStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para la gestión de fichajes (Timesheets)
 * <p>
 * Gestiona el registro de entrada/salida de empleados y control horario
 */

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/timesheets")
@RequiredArgsConstructor
@Tag(name = "Timesheets", description = "Endpoints para gestión de fichajes y control horario")
public class TimesheetController {

	private final TimesheetService timesheetService;

	/**
	 * Crear un timesheet manualmente (EXCEPCIONAL)
	 * Solo para casos administrativos especiales
	 */
	@CrossOrigin
	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Crear timesheet manualmente (EXCEPCIONAL)",
			description = "SOLO para correcciones administrativas. Los fichajes normales deben usar check-in/check-out."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Timesheet creado exitosamente", content = @Content(schema = @Schema(implementation = TimesheetDetailResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "409", description = "Ya existe un timesheet para este workshift")
	})
	public StandardResponse<TimesheetDetailResponseDTO> createTimesheet(@Valid @RequestBody TimesheetRequestDTO requestDTO) {

		log.warn("Creación manual de timesheet - Workshift: {}", requestDTO.getWorkshiftId());

		TimesheetDetailResponseDTO created = timesheetService.createTimesheet(requestDTO);

		return ResponseBuilder.withCreatedElements(HttpStatus.CREATED, true, 1, "Timesheet creado exitosamente", created);
	}

	/**
	 * Obtener un timesheet por ID
	 */
	@CrossOrigin
	@GetMapping("/{id}")
	@Operation(summary = "Obtener timesheet por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Timesheet encontrado", content = @Content(schema = @Schema(implementation = TimesheetDetailResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "Timesheet no encontrado"),
	})
	public StandardResponse<TimesheetDetailResponseDTO> getTimesheetById(
			@PathVariable @Parameter(description = "ID del timesheet") Long id) {

		log.debug("Consultando timesheet con ID: {}", id);

		TimesheetDetailResponseDTO timesheet = timesheetService.findById(id);

		return ResponseBuilder.with(HttpStatus.OK, true, "Timesheet encontrado", timesheet);
	}

	/**
	 * Listar todos los timesheets con paginación
	 */
	@CrossOrigin
	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(summary = "Listar todos los timesheets con paginación")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Lista de timesheets") })
	public StandardResponse<Page<TimesheetSummaryDTO>> getAllTimesheets(@PageableDefault(size = 20, sort = "checkInAt", direction = Sort.Direction.DESC) Pageable pageable) {

		log.debug("Listando timesheets - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());

		Page<TimesheetSummaryDTO> timesheets = timesheetService.findAll(pageable);

		String message = String.format("Página %d de %d (Total: %d timesheets)", timesheets.getNumber() + 1, timesheets.getTotalPages(), timesheets.getTotalElements());

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Actualizar un timesheet (EXCEPCIONAL)
	 * Solo para correcciones administrativas
	 */
	@CrossOrigin
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Actualizar timesheet (EXCEPCIONAL)",
			description = "SOLO para correcciones administrativas. Requiere justificación."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Timesheet actualizado exitosamente", content = @Content(schema = @Schema(implementation = TimesheetDetailResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "404", description = "Timesheet no encontrado")
	})
	public StandardResponse<TimesheetDetailResponseDTO> updateTimesheet(@PathVariable Long id, @Valid @RequestBody TimesheetRequestDTO requestDTO) {

		log.warn("Actualización manual de timesheet {} - Notas: {}", id, requestDTO.getNotes());

		TimesheetDetailResponseDTO updated = timesheetService.updateTimesheet(id, requestDTO);

		return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Timesheet actualizado exitosamente", updated);
	}

	/**
	 * Eliminar un timesheet
	 * Solo administradores
	 */
	@CrossOrigin
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Eliminar un timesheet",
			description = "Solo administradores. Usar con precaución."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Timesheet eliminado exitosamente"),
			@ApiResponse(responseCode = "404", description = "Timesheet no encontrado")
	})
	public StandardResponse<Void> deleteTimesheet(
			@PathVariable @Parameter(description = "ID del timesheet") Long id) {

		log.warn("Eliminando timesheet con ID: {}", id);

		timesheetService.deleteTimesheet(id);

		return ResponseBuilder.withDeletedElements(HttpStatus.OK, true, 1, "Timesheet eliminado exitosamente");
	}

	/**
	 * Registrar entrada (check-in) de un empleado
	 * Crea automáticamente un timesheet para el turno del día
	 */
	@CrossOrigin
	@PostMapping("/check-in/{userId}")
	@Operation(
			summary = "Registrar entrada (check-in)",
			description = "Crea automáticamente un timesheet para el turno asignado del día. Detecta tardanzas."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Check-in registrado exitosamente", content = @Content(schema = @Schema(implementation = TimesheetDetailResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "Usuario no tiene turno asignado para hoy"),
			@ApiResponse(responseCode = "409", description = "El usuario ya tiene un timesheet abierto")
	})
	public StandardResponse<TimesheetDetailResponseDTO> checkIn(@PathVariable @Parameter(description = "ID del usuario") Long userId) {

		log.info("Registrando check-in - Usuario: {}", userId);

		TimesheetDetailResponseDTO timesheet = timesheetService.checkIn(userId);

		return ResponseBuilder.withCreatedElements(HttpStatus.CREATED, true, 1, "Check-in registrado exitosamente", timesheet);
	}

	/**
	 * Registrar entrada con hora específica (ADMINISTRATIVO)
	 */
	@CrossOrigin
	@PostMapping("/check-in/{userId}/at/{checkInTime}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Registrar entrada con hora específica (ADMINISTRATIVO)",
			description = "Para correcciones administrativas. Permite especificar la hora exacta del check-in."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Check-in registrado exitosamente"),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado")
	})
	public StandardResponse<TimesheetDetailResponseDTO> checkInWithTime(
			@PathVariable Long userId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
			@Parameter(description = "Fecha y hora del check-in (ISO format)") LocalDateTime checkInTime) {

		log.warn("Check-in administrativo - Usuario: {}, Hora: {}", userId, checkInTime);

		TimesheetDetailResponseDTO timesheet = timesheetService.checkInWithTime(userId, checkInTime);

		return ResponseBuilder.withCreatedElements(HttpStatus.CREATED, true, 1, "Check-in registrado exitosamente", timesheet);
	}

	/**
	 * Registrar salida (check-out) de un empleado
	 * Cierra el timesheet abierto
	 */
	@CrossOrigin
	@PostMapping("/check-out/{userId}")
	@Operation(
			summary = "Registrar salida (check-out)",
			description = "Cierra el timesheet abierto. Calcula horas trabajadas y detecta anomalías."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Check-out registrado exitosamente", content = @Content(schema = @Schema(implementation = TimesheetDetailResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "No hay timesheet abierto para el usuario")
	})
	public StandardResponse<TimesheetDetailResponseDTO> checkOut(
			@PathVariable @Parameter(description = "ID del usuario") Long userId) {

		log.info("Registrando check-out - Usuario: {}", userId);

		TimesheetDetailResponseDTO timesheet = timesheetService.checkOut(userId);

		return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Check-out registrado exitosamente", timesheet);
	}

	/**
	 * Registrar salida con hora específica (ADMINISTRATIVO)
	 */
	@CrossOrigin
	@PostMapping("/check-out/{userId}/at/{checkOutTime}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Registrar salida con hora específica (ADMINISTRATIVO)",
			description = "Para correcciones administrativas. Permite especificar la hora exacta del check-out."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Check-out registrado exitosamente"),
			@ApiResponse(responseCode = "404", description = "No hay timesheet abierto para el usuario")
	})
	public StandardResponse<TimesheetDetailResponseDTO> checkOutWithTime(
			@PathVariable Long userId,
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
			@Parameter(description = "Fecha y hora del check-out (ISO format)") LocalDateTime checkOutTime) {

		log.warn("Check-out administrativo - Usuario: {}, Hora: {}", userId, checkOutTime);

		TimesheetDetailResponseDTO timesheet = timesheetService.checkOutWithTime(userId, checkOutTime);

		return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Check-out registrado exitosamente", timesheet);
	}

	/**
	 * Verificar si un usuario tiene un timesheet abierto
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/has-open")
	@Operation(
			summary = "Verificar si tiene timesheet abierto",
			description = "Retorna true si el usuario tiene un timesheet sin check-out"
	)
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Verificación completada") })
	public StandardResponse<Boolean> hasOpenTimesheet(@PathVariable @Parameter(description = "ID del usuario") Long userId) {

		log.debug("Verificando timesheet abierto - Usuario: {}", userId);

		Boolean hasOpen = timesheetService.hasOpenTimesheet(userId);

		String message = hasOpen ? "El usuario tiene un timesheet abierto" : "El usuario no tiene timesheets abiertos";

		return ResponseBuilder.with(HttpStatus.OK, true, message, hasOpen);
	}

	/**
	 * Obtener el timesheet abierto de un usuario
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/open")
	@Operation(
			summary = "Obtener timesheet abierto",
			description = "Retorna el último timesheet sin check-out del usuario"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Timesheet encontrado o null si no hay abierto", content = @Content(schema = @Schema(implementation = TimesheetDetailResponseDTO.class)))
	})
	public StandardResponse<TimesheetDetailResponseDTO> getOpenTimesheet(
			@PathVariable @Parameter(description = "ID del usuario") Long userId) {

		log.debug("Obteniendo timesheet abierto - Usuario: {}", userId);

		TimesheetDetailResponseDTO timesheet = timesheetService.getOpenTimesheet(userId);

		String message = timesheet != null ? "Timesheet abierto encontrado" : "No hay timesheet abierto";

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheet);
	}

	/**
	 * Obtener timesheet por workshift
	 */
	@CrossOrigin
	@GetMapping("/workshift/{workshiftId}")
	@Operation(summary = "Obtener timesheet de un workshift específico")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Timesheet encontrado o null", content = @Content(schema = @Schema(implementation = TimesheetDetailResponseDTO.class)))
	})
	public StandardResponse<TimesheetDetailResponseDTO> getTimesheetByWorkshift(
			@PathVariable @Parameter(description = "ID del workshift") Long workshiftId) {

		log.debug("Consultando timesheet del workshift: {}", workshiftId);

		TimesheetDetailResponseDTO timesheet = timesheetService.findByWorkshiftId(workshiftId);

		String message = timesheet != null ? "Timesheet encontrado" : "No hay timesheet para este workshift";

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheet);
	}

	/**
	 * Obtener todos los timesheets de un usuario
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}")
	@Operation(summary = "Obtener todos los timesheets de un usuario")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de timesheets")
	})
	public StandardResponse<List<TimesheetResponseDTO>> getTimesheetsByUser(
			@PathVariable @Parameter(description = "ID del usuario") Long userId) {

		log.debug("Consultando timesheets del usuario: {}", userId);

		List<TimesheetResponseDTO> timesheets = timesheetService.findByUserId(userId);

		String message = String.format("Encontrados %d timesheets para el usuario", timesheets.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Obtener timesheets de un usuario con paginación
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/paginated")
	@Operation(summary = "Obtener timesheets de un usuario con paginación")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Página de timesheets")
	})
	public StandardResponse<Page<TimesheetResponseDTO>> getTimesheetsByUserPaginated(
			@PathVariable Long userId,
			@PageableDefault(size = 20, sort = "checkInAt", direction = Sort.Direction.DESC) Pageable pageable) {

		log.debug("Consultando timesheets del usuario {} - Página: {}", userId, pageable.getPageNumber());

		Page<TimesheetResponseDTO> timesheets = timesheetService.findByUserId(userId, pageable);

		return ResponseBuilder.with(HttpStatus.OK, true, "Timesheets del usuario obtenidos exitosamente", timesheets);
	}

	/**
	 * Obtener timesheets de un usuario en un rango de fechas
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/date-range")
	@Operation(summary = "Obtener timesheets de un usuario en un rango de fechas")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de timesheets en el rango")
	})
	public StandardResponse<List<TimesheetResponseDTO>> getTimesheetsByUserAndDateRange(
			@PathVariable Long userId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.debug("Consultando timesheets del usuario {} desde {} hasta {}", userId, startDate, endDate);

		List<TimesheetResponseDTO> timesheets = timesheetService.findByUserIdAndDateRange(userId, startDate, endDate);

		String message = String.format("Encontrados %d timesheets en el rango", timesheets.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Obtener timesheet de un usuario en una fecha específica
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/date/{date}")
	@Operation(summary = "Obtener timesheet de un usuario en una fecha específica")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Timesheet encontrado o null", content = @Content(schema = @Schema(implementation = TimesheetDetailResponseDTO.class)))
	})
	public StandardResponse<TimesheetDetailResponseDTO> getTimesheetByUserAndDate(
			@PathVariable Long userId, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		log.debug("Consultando timesheet del usuario {} en fecha {}", userId, date);

		TimesheetDetailResponseDTO timesheet = timesheetService.findByUserIdAndDate(userId, date);

		String message = timesheet != null ? "Timesheet encontrado" : "No hay timesheet para esta fecha";

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheet);
	}

	/**
	 * Obtener timesheets por estado
	 */
	@CrossOrigin
	@GetMapping("/status/{status}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(summary = "Obtener timesheets por estado")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Lista de timesheets")
	})
	public StandardResponse<List<TimesheetResponseDTO>> getTimesheetsByStatus(
			@PathVariable @Parameter(description = "Estado del timesheet") TimesheetStatus status) {

		log.debug("Consultando timesheets con estado: {}", status);

		List<TimesheetResponseDTO> timesheets = timesheetService.findByStatus(status);

		String message = String.format("Encontrados %d timesheets con estado %s", timesheets.size(), status);

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Obtener timesheets por estado con paginación
	 */
	@CrossOrigin
	@GetMapping("/status/{status}/paginated")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(summary = "Obtener timesheets por estado con paginación")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Página de timesheets") })
	public StandardResponse<Page<TimesheetResponseDTO>> getTimesheetsByStatusPaginated(
			@PathVariable TimesheetStatus status,
			@PageableDefault(size = 20, sort = "checkInAt", direction = Sort.Direction.DESC) Pageable pageable) {

		log.debug("Consultando timesheets con estado {} - Página: {}", status, pageable.getPageNumber());

		Page<TimesheetResponseDTO> timesheets = timesheetService.findByStatus(status, pageable);

		return ResponseBuilder.with(HttpStatus.OK, true, "Timesheets obtenidos exitosamente", timesheets);
	}

	/**
	 * Obtener todos los timesheets abiertos
	 */
	@CrossOrigin
	@GetMapping("/open")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener todos los timesheets abiertos",
			description = "Retorna todos los timesheets que no tienen check-out registrado"
	)
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Lista de timesheets abiertos") })
	public StandardResponse<List<TimesheetResponseDTO>> getOpenTimesheets() {

		log.debug("Consultando timesheets abiertos");

		List<TimesheetResponseDTO> timesheets = timesheetService.findOpenTimesheets();

		String message = String.format("Encontrados %d timesheets abiertos", timesheets.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Obtener todos los timesheets cerrados
	 */
	@CrossOrigin
	@GetMapping("/closed")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(summary = "Obtener todos los timesheets cerrados")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Lista de timesheets cerrados") })
	public StandardResponse<List<TimesheetResponseDTO>> getClosedTimesheets() {

		log.debug("Consultando timesheets cerrados");

		List<TimesheetResponseDTO> timesheets = timesheetService.findClosedTimesheets();

		String message = String.format("Encontrados %d timesheets cerrados", timesheets.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Contar timesheets por estado
	 */
	@CrossOrigin
	@GetMapping("/status/{status}/count")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(summary = "Contar timesheets por estado")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Cantidad de timesheets") })
	public StandardResponse<Long> countTimesheetsByStatus(
			@PathVariable TimesheetStatus status) {

		Long count = timesheetService.countByStatus(status);

		String message = String.format("Hay %d timesheets con estado %s", count, status);

		return ResponseBuilder.with(HttpStatus.OK, true, message, count);
	}

	/**
	 * Obtener timesheets de una fecha específica
	 */
	@CrossOrigin
	@GetMapping("/date/{date}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(summary = "Obtener timesheets de una fecha específica")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de timesheets de la fecha")
	})
	public StandardResponse<List<TimesheetResponseDTO>> getTimesheetsByDate(
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		log.debug("Consultando timesheets de la fecha: {}", date);

		List<TimesheetResponseDTO> timesheets = timesheetService.findByDate(date);

		String message = String.format("Encontrados %d timesheets para la fecha %s", timesheets.size(), date);

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Obtener timesheets en un rango de fechas
	 */
	@CrossOrigin
	@GetMapping("/date-range")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(summary = "Obtener timesheets en un rango de fechas")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de timesheets en el rango")
	})
	public StandardResponse<List<TimesheetResponseDTO>> getTimesheetsByDateRange(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.debug("Consultando timesheets desde {} hasta {}", startDate, endDate);

		List<TimesheetResponseDTO> timesheets = timesheetService.findByDateRange(startDate, endDate);

		String message = String.format("Encontrados %d timesheets en el rango", timesheets.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Obtener timesheets de hoy
	 */
	@CrossOrigin
	@GetMapping("/today")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(summary = "Obtener timesheets de hoy")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de timesheets de hoy")
	})
	public StandardResponse<List<TimesheetResponseDTO>> getTodayTimesheets() {

		log.debug("Consultando timesheets de hoy");

		List<TimesheetResponseDTO> timesheets = timesheetService.findTodayTimesheets();

		String message = String.format("Encontrados %d timesheets para hoy", timesheets.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Obtener timesheets de esta semana
	 */
	@CrossOrigin
	@GetMapping("/this-week")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(summary = "Obtener timesheets de esta semana")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de timesheets de la semana")
	})
	public StandardResponse<List<TimesheetResponseDTO>> getThisWeekTimesheets() {

		log.debug("Consultando timesheets de esta semana");

		List<TimesheetResponseDTO> timesheets = timesheetService.findThisWeekTimesheets();

		String message = String.format("Encontrados %d timesheets esta semana", timesheets.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Obtener timesheets de este mes
	 */
	@CrossOrigin
	@GetMapping("/this-month")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(summary = "Obtener timesheets de este mes")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de timesheets del mes")
	})
	public StandardResponse<List<TimesheetResponseDTO>> getThisMonthTimesheets() {

		log.debug("Consultando timesheets de este mes");

		List<TimesheetResponseDTO> timesheets = timesheetService.findThisMonthTimesheets();

		String message = String.format("Encontrados %d timesheets este mes", timesheets.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Obtener timesheets con anomalías
	 */
	@CrossOrigin
	@GetMapping("/anomalies")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener timesheets con anomalías",
			description = "Retorna timesheets con tardanzas, horas excesivas, salidas anticipadas, etc."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de timesheets con anomalías")
	})
	public StandardResponse<List<TimesheetResponseDTO>> getAnomalies() {

		log.debug("Consultando timesheets con anomalías");

		List<TimesheetResponseDTO> timesheets = timesheetService.findAnomalies();

		String message = String.format("Encontrados %d timesheets con anomalías", timesheets.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Obtener timesheets con check-out pendiente
	 */
	@CrossOrigin
	@GetMapping("/overdue-checkouts")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener timesheets con check-out pendiente",
			description = "Timesheets abiertos que exceden el umbral de horas especificado"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de timesheets pendientes")
	})
	public StandardResponse<List<TimesheetResponseDTO>> getOverdueCheckouts(
			@RequestParam(defaultValue = "12") @Parameter(description = "Horas de umbral") int hoursThreshold) {

		log.debug("Consultando timesheets con check-out pendiente - Umbral: {} horas", hoursThreshold);

		List<TimesheetResponseDTO> timesheets = timesheetService.findOverdueCheckouts(hoursThreshold);

		String message = String.format("Encontrados %d timesheets con check-out pendiente", timesheets.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Obtener timesheets con horas excesivas
	 */
	@CrossOrigin
	@GetMapping("/excessive-hours")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener timesheets con horas excesivas",
			description = "Timesheets que exceden el máximo de horas permitidas"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de timesheets con horas excesivas")
	})
	public StandardResponse<List<TimesheetResponseDTO>> getExcessiveWorkHours(
			@RequestParam(defaultValue = "12") @Parameter(description = "Máximo de horas") long maxHours) {

		log.debug("Consultando timesheets con horas excesivas - Máximo: {} horas", maxHours);

		List<TimesheetResponseDTO> timesheets = timesheetService.findExcessiveWorkHours(maxHours);

		String message = String.format("Encontrados %d timesheets con horas excesivas", timesheets.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Obtener timesheets con tardanzas
	 */
	@CrossOrigin
	@GetMapping("/late-check-ins")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener timesheets con tardanzas",
			description = "Timesheets donde el empleado llegó tarde"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de timesheets con tardanzas")
	})
	public StandardResponse<List<TimesheetResponseDTO>> getLateCheckIns(
			@RequestParam(defaultValue = "30") @Parameter(description = "Minutos de tolerancia") int toleranceMinutes) {

		log.debug("Consultando timesheets con tardanzas - Tolerancia: {} minutos", toleranceMinutes);

		List<TimesheetResponseDTO> timesheets = timesheetService.findLateCheckIns(toleranceMinutes);

		String message = String.format("Encontrados %d timesheets con tardanzas", timesheets.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Corregir una anomalía
	 */
	@CrossOrigin
	@PutMapping("/{id}/correct-anomaly")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Corregir una anomalía",
			description = "Cambia el estado de ANOMALY a CORRECTED y registra las notas de corrección"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Anomalía corregida exitosamente",
					content = @Content(schema = @Schema(implementation = TimesheetDetailResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "El timesheet no está marcado como anomalía"),
			@ApiResponse(responseCode = "404", description = "Timesheet no encontrado")
	})
	public StandardResponse<TimesheetDetailResponseDTO> correctAnomaly(
			@PathVariable Long id,
			@RequestParam @Parameter(description = "Notas de corrección") String notes) {

		log.info("Corrigiendo anomalía en timesheet {} - Notas: {}", id, notes);

		TimesheetDetailResponseDTO corrected = timesheetService.correctAnomaly(id, notes);

		return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Anomalía corregida exitosamente", corrected
		);
	}

	// ========== ESTADÍSTICAS Y REPORTES ==========

	/**
	 * Obtener estadísticas globales
	 */
	@CrossOrigin
	@GetMapping("/statistics")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener estadísticas globales de timesheets",
			description = "Incluye métricas de asistencia, puntualidad, horas trabajadas, etc."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente",
					content = @Content(schema = @Schema(implementation = TimesheetStatisticsDTO.class)))
	})
	public StandardResponse<TimesheetStatisticsDTO> getGlobalStatistics() {

		log.debug("Obteniendo estadísticas globales de timesheets");

		TimesheetStatisticsDTO statistics = timesheetService.getGlobalStatistics();

		return ResponseBuilder.with(HttpStatus.OK, true, "Estadísticas obtenidas exitosamente", statistics);
	}

	/**
	 * Obtener total de horas trabajadas por usuario
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/total-hours")
	@Operation(summary = "Obtener total de horas trabajadas por usuario en un período")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Total de horas calculado")
	})
	public StandardResponse<Long> getTotalWorkedHoursByUser(
			@PathVariable Long userId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.debug("Calculando total de horas trabajadas - Usuario: {}, Período: {} a {}", userId, startDate, endDate);

		Long totalHours = timesheetService.getTotalWorkedHoursByUser(userId, startDate, endDate);

		String message = String.format("Total de horas trabajadas: %d", totalHours);

		return ResponseBuilder.with(HttpStatus.OK, true, message, totalHours);
	}

	/**
	 * Obtener promedio de horas trabajadas
	 */
	@CrossOrigin
	@GetMapping("/average-hours")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(summary = "Obtener promedio de horas trabajadas en un período")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Promedio calculado")
	})
	public StandardResponse<Double> getAverageWorkedHours(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.debug("Calculando promedio de horas trabajadas - Período: {} a {}", startDate, endDate);

		Double average = timesheetService.getAverageWorkedHours(startDate, endDate);

		String message = String.format("Promedio de horas trabajadas: %.2f", average);

		return ResponseBuilder.with(HttpStatus.OK, true, message, average);
	}

	/**
	 * Obtener estadísticas de asistencia por día
	 */
	@CrossOrigin
	@GetMapping("/stats/by-day")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener estadísticas de asistencia por día",
			description = "Retorna el número de fichajes por cada día en el período especificado"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Estadísticas obtenidas")
	})
	public StandardResponse<Map<String, Object>> getAttendanceStatsByDay(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.debug("Obteniendo estadísticas de asistencia por día - Período: {} a {}", startDate, endDate);

		Map<String, Object> stats = timesheetService.getAttendanceStatsByDay(startDate, endDate);

		return ResponseBuilder.with(HttpStatus.OK, true, "Estadísticas obtenidas exitosamente", stats);
	}

	/**
	 * Obtener estadísticas de asistencia por usuario
	 */
	@CrossOrigin
	@GetMapping("/stats/by-user")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener estadísticas de asistencia por usuario",
			description = "Retorna el número de fichajes y horas trabajadas por cada usuario"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Estadísticas obtenidas")
	})
	public StandardResponse<Map<String, Object>> getAttendanceStatsByUser(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.debug("Obteniendo estadísticas de asistencia por usuario - Período: {} a {}", startDate, endDate);

		Map<String, Object> stats = timesheetService.getAttendanceStatsByUser(startDate, endDate);

		return ResponseBuilder.with(HttpStatus.OK, true, "Estadísticas obtenidas exitosamente", stats);
	}

	/**
	 * Obtener estadísticas de asistencia por tipo de turno
	 */
	@CrossOrigin
	@GetMapping("/stats/by-shift-type")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener estadísticas de asistencia por tipo de turno",
			description = "Retorna el número de fichajes por cada tipo de turno (MORNING, AFTERNOON, NIGHT)"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Estadísticas obtenidas")
	})
	public StandardResponse<Map<String, Object>> getAttendanceStatsByShiftType(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.debug("Obteniendo estadísticas por tipo de turno - Período: {} a {}", startDate, endDate);

		Map<String, Object> stats = timesheetService.getAttendanceStatsByShiftType(startDate, endDate);

		return ResponseBuilder.with(HttpStatus.OK, true, "Estadísticas obtenidas exitosamente", stats);
	}

	/**
	 * Obtener usuarios más puntuales
	 */
	@CrossOrigin
	@GetMapping("/stats/most-punctual")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener usuarios más puntuales",
			description = "Retorna los usuarios con menor promedio de tardanza"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de usuarios puntuales")
	})
	public StandardResponse<List<Map<String, Object>>> getMostPunctualUsers(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(defaultValue = "10") @Parameter(description = "Número de usuarios a retornar") int limit) {

		log.debug("Obteniendo usuarios más puntuales - Período: {} a {}, Límite: {}", startDate, endDate, limit);

		List<Map<String, Object>> users = timesheetService.getMostPunctualUsers(startDate, endDate, limit);

		String message = String.format("Top %d usuarios más puntuales", users.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, users);
	}

	/**
	 * Obtener usuarios con más horas trabajadas
	 */
	@CrossOrigin
	@GetMapping("/stats/most-hours")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Obtener usuarios con más horas trabajadas",
			description = "Retorna los usuarios que más horas han trabajado en el período"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de usuarios")
	})
	public StandardResponse<List<Map<String, Object>>> getUsersWithMostHours(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(defaultValue = "10") @Parameter(description = "Número de usuarios a retornar") int limit) {

		log.debug("Obteniendo usuarios con más horas - Período: {} a {}, Límite: {}", startDate, endDate, limit);

		List<Map<String, Object>> users = timesheetService.getUsersWithMostHours(startDate, endDate, limit);

		String message = String.format("Top %d usuarios con más horas", users.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, users);
	}

	/**
	 * Generar reporte de asistencia de un usuario
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/attendance-report")
	@Operation(
			summary = "Generar reporte de asistencia completo de un usuario",
			description = "Incluye estadísticas detalladas, puntualidad, horas trabajadas, anomalías, etc."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Reporte generado exitosamente")
	})
	public StandardResponse<Map<String, Object>> generateUserAttendanceReport(
			@PathVariable Long userId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.info("Generando reporte de asistencia - Usuario: {}, Período: {} a {}", userId, startDate, endDate);

		Map<String, Object> report = timesheetService.generateUserAttendanceReport(userId, startDate, endDate);

		return ResponseBuilder.with(HttpStatus.OK, true, "Reporte generado exitosamente", report);
	}

	// ========== BÚSQUEDA AVANZADA CON FILTROS ==========

	/**
	 * Búsqueda avanzada de timesheets con filtros múltiples
	 */
	@CrossOrigin
	@GetMapping("/search")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Búsqueda avanzada con filtros múltiples",
			description = "Permite combinar múltiples criterios: usuario, workshift, estado, rango de fechas"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Resultados de búsqueda")
	})
	public StandardResponse<Page<TimesheetResponseDTO>> searchTimesheets(
			@RequestParam(required = false) Long userId,
			@RequestParam(required = false) Long workshiftId,
			@RequestParam(required = false) TimesheetStatus status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@PageableDefault(size = 20, sort = "checkInAt", direction = Sort.Direction.DESC) Pageable pageable) {

		log.debug("Búsqueda avanzada - Usuario: {}, Workshift: {}, Estado: {}, Fechas: {} a {}", userId, workshiftId, status, startDate, endDate);

		Page<TimesheetResponseDTO> timesheets = timesheetService.findWithFilters(userId, workshiftId, status, startDate, endDate, pageable);

		String message = String.format("Encontrados %d timesheets que cumplen los filtros", timesheets.getTotalElements());

		return ResponseBuilder.with(HttpStatus.OK, true, message, timesheets);
	}

	/**
	 * Cerrar automáticamente timesheets antiguos abiertos
	 * Puede convertirse en un CRON job
	 */
	@CrossOrigin
	@PostMapping("/admin/auto-close")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Cerrar automáticamente timesheets antiguos (ADMIN)",
			description = "Cierra timesheets abiertos que exceden el umbral de días especificado. Marca como ANOMALY."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Cierre automático completado")
	})
	public StandardResponse<Integer> autoCloseOldTimesheets(
			@RequestParam(defaultValue = "2") @Parameter(description = "Días de umbral") int daysThreshold) {

		log.warn("Ejecutando cierre automático de timesheets - Umbral: {} días", daysThreshold);

		int closedCount = timesheetService.autoCloseOldTimesheets(daysThreshold);

		String message = String.format("Se cerraron automáticamente %d timesheets", closedCount);

		return ResponseBuilder.with(HttpStatus.OK, true, message, closedCount);
	}

	/**
	 * Detectar anomalías en timesheets recientes
	 * Puede convertirse en un CRON job
	 */
	@CrossOrigin
	@PostMapping("/admin/detect-anomalies")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Detectar anomalías en timesheets (ADMIN)",
			description = "Analiza timesheets recientes y marca aquellos con tardanzas, horas excesivas, etc."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Detección completada")
	})
	public StandardResponse<Integer> detectAnomalies(
			@RequestParam(defaultValue = "7") @Parameter(description = "Días hacia atrás") int daysBack) {

		log.warn("Ejecutando detección de anomalías - Últimos {} días", daysBack);

		int anomaliesDetected = timesheetService.detectAnomalies(daysBack);

		String message = String.format("Se detectaron %d anomalías", anomaliesDetected);

		return ResponseBuilder.with(HttpStatus.OK, true, message, anomaliesDetected);
	}

	/**
	 * Archivar timesheets antiguos
	 * Puede convertirse en un CRON job
	 */
	@CrossOrigin
	@PostMapping("/admin/archive")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Archivar timesheets antiguos (ADMIN)",
			description = "Archiva timesheets con más del umbral de meses especificado"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Archivado completado")
	})
	public StandardResponse<Integer> archiveOldTimesheets(
			@RequestParam(defaultValue = "6") @Parameter(description = "Meses de umbral") int monthsThreshold) {

		log.warn("Ejecutando archivado de timesheets antiguos - Umbral: {} meses", monthsThreshold);

		int archivedCount = timesheetService.archiveOldTimesheets(monthsThreshold);

		String message = String.format("Se archivaron %d timesheets", archivedCount);

		return ResponseBuilder.with(HttpStatus.OK, true, message, archivedCount);
	}
}