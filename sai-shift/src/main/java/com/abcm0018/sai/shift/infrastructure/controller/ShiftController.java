package com.abcm0018.sai.shift.infrastructure.controller;

import java.time.LocalTime;
import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.abcm0018.sai.shared.response.ResponseBuilder;
import com.abcm0018.sai.shared.response.StandardResponse;
import com.abcm0018.sai.shift.application.dtos.ShiftRequestDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftResponseDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftStatusChangeDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftSummaryDTO;
import com.abcm0018.sai.shift.application.service.ShiftService;
import com.abcm0018.sai.shift.domain.enums.ShiftType;

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
 * Controlador REST para la gestión de turnos (Shifts)
 * <p>
 * Gestiona la configuración de horarios de trabajo: Mañana, Tarde y Noche
 */
@RestController
@RequestMapping(value = "/api/v1/shifts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shifts", description = "Endpoints para gestión de turnos y horarios de trabajo")
public class ShiftController {

	private final ShiftService shiftService;

	/**
	 * Crear un nuevo turno
	 * Solo administradores
	 */
	@CrossOrigin
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Crear un nuevo turno",
			description = "Crea un turno con horario específico. Valida que no se solape con turnos existentes."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Turno creado exitosamente", content = @Content(schema = @Schema(implementation = ShiftResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos o solapamiento de horarios"),
			@ApiResponse(responseCode = "403", description = "No está autorizado para realizar esta operación"),
			@ApiResponse(responseCode = "409", description = "Ya existe un turno de este tipo")
	})
	public StandardResponse<ShiftResponseDTO> createShift(@Valid @RequestBody ShiftRequestDTO requestDTO) {

		log.info("Creando nuevo turno - Tipo: {}, Horario: {} - {}", requestDTO.getShiftType(), requestDTO.getStartTime(), requestDTO.getEndTime());

		ShiftResponseDTO created = shiftService.createShift(requestDTO);

		return ResponseBuilder.withCreatedElements(HttpStatus.CREATED, true, 1, "Turno creado exitosamente", created);
	}

	/**
	 * Obtener un turno por ID
	 */
	@CrossOrigin
	@GetMapping("/{id}")
	@Operation(summary = "Obtener turno por ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Turno encontrado", content = @Content(schema = @Schema(implementation = ShiftResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "Turno no encontrado")
	})
	public StandardResponse<ShiftResponseDTO> getShiftById(
			@PathVariable @Parameter(description = "ID del turno") Long id) {

		log.debug("Consultando turno con ID: {}", id);

		ShiftResponseDTO shift = shiftService.findById(id);

		return ResponseBuilder.with(HttpStatus.OK, true, "Turno encontrado", shift);
	}

	/**
	 * Obtener un turno por tipo
	 */
	@CrossOrigin
	@GetMapping("/type/{shiftType}")
	@Operation(
			summary = "Obtener turno por tipo",
			description = "Busca un turno por su tipo (MORNING, AFTERNOON, NIGHT)"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Turno encontrado",
					content = @Content(schema = @Schema(implementation = ShiftResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "Turno no encontrado")
	})
	public StandardResponse<ShiftResponseDTO> getShiftByType(
			@PathVariable @Parameter(description = "Tipo de turno") ShiftType shiftType) {

		log.debug("Consultando turno con tipo: {}", shiftType);

		ShiftResponseDTO shift = shiftService.findByShiftType(shiftType);

		return ResponseBuilder.with(HttpStatus.OK, true, "Turno encontrado", shift);
	}

	/**
	 * Listar todos los turnos
	 */
	@CrossOrigin
	@GetMapping
	@Operation(summary = "Listar todos los turnos")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de turnos")
	})
	public StandardResponse<List<ShiftSummaryDTO>> getAllShifts() {

		log.debug("Listando todos los turnos");

		List<ShiftSummaryDTO> shifts = shiftService.findAll();

		String message = String.format("Encontrados %d turnos", shifts.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, shifts);
	}

	/**
	 * Listar turnos ordenados por hora de inicio
	 */
	@CrossOrigin
	@GetMapping("/ordered")
	@Operation(
			summary = "Listar turnos ordenados por hora de inicio",
			description = "Retorna todos los turnos ordenados cronológicamente"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de turnos ordenados")
	})
	public StandardResponse<List<ShiftSummaryDTO>> getAllShiftsOrdered() {

		log.debug("Listando turnos ordenados por hora de inicio");

		List<ShiftSummaryDTO> shifts = shiftService.findAllOrderedByStartTime();

		String message = String.format("Encontrados %d turnos ordenados", shifts.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, shifts);
	}

	/**
	 * Actualizar un turno
	 * Solo administradores
	 */
	@CrossOrigin
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Actualizar un turno",
			description = "Actualiza horarios y configuración. Valida solapamientos."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Turno actualizado exitosamente", content = @Content(schema = @Schema(implementation = ShiftResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Datos inválidos o solapamiento"),
			@ApiResponse(responseCode = "404", description = "Turno no encontrado"),
			@ApiResponse(responseCode = "401", description = "No autorizado para realizar esta operación"),
	})
	public StandardResponse<ShiftResponseDTO> updateShift(@PathVariable Long id,
			@Valid @RequestBody ShiftRequestDTO requestDTO) {

		log.info("Actualizando turno {} - Tipo: {}", id, requestDTO.getShiftType());

		ShiftResponseDTO updated = shiftService.updateShift(id, requestDTO);

		return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Turno actualizado exitosamente", updated);
	}

	/**
	 * Desactivar un turno (soft delete)
	 * Solo administradores
	 */
	@CrossOrigin
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Desactivar un turno (soft delete)",
			description = "Desactiva el turno sin eliminarlo de la base de datos"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Turno desactivado exitosamente"),
			@ApiResponse(responseCode = "403", description = "No autorizado a realizar esta operación"),
			@ApiResponse(responseCode = "404", description = "Turno no encontrado"),
			@ApiResponse(responseCode = "409", description = "El turno tiene asignaciones activas")
	})
	public StandardResponse<Void> deleteShift(@PathVariable @Parameter(description = "ID del turno") Long id) {

		log.warn("Desactivando turno con ID: {}", id);

		shiftService.deleteShift(id);

		return ResponseBuilder.withDeletedElements(HttpStatus.OK, true, 1, "Turno desactivado exitosamente");
	}

	/**
	 * Eliminar permanentemente un turno
	 * Solo administradores - Usar con precaución
	 */
	@CrossOrigin
	@DeleteMapping("/{id}/permanent")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Eliminar permanentemente un turno",
			description = "PELIGRO: Elimina el turno de forma permanente. Solo si no tiene workshifts asignados."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Turno eliminado permanentemente"),
			@ApiResponse(responseCode = "404", description = "Turno no encontrado"),
			@ApiResponse(responseCode = "409", description = "El turno tiene workshifts asignados")
	})
	public StandardResponse<Void> permanentlyDeleteShift(
			@PathVariable @Parameter(description = "ID del turno") Long id) {

		log.error("ALERTA: Eliminación permanente de turno con ID: {}", id);

		shiftService.permanentlyDeleteShift(id);

		return ResponseBuilder.withDeletedElements(HttpStatus.OK, true, 1, "Turno eliminado permanentemente");
	}

	/**
	 * Activar un turno
	 * Solo administradores
	 */
	@CrossOrigin
	@PutMapping("/{id}/activate")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Activar un turno",
			description = "Reactiva un turno previamente desactivado"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Turno activado exitosamente", content = @Content(schema = @Schema(implementation = ShiftResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "Turno no encontrado")
	})
	public StandardResponse<ShiftResponseDTO> activateShift(@PathVariable @Parameter(description = "ID del turno") Long id) {

		log.info("Activando turno con ID: {}", id);

		ShiftResponseDTO activated = shiftService.activateShift(id);

		return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Turno activado exitosamente", activated);
	}

	/**
	 * Desactivar un turno con razón
	 * Solo administradores
	 */
	@CrossOrigin
	@PutMapping("/{id}/deactivate")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Desactivar un turno con razón",
			description = "Desactiva un turno proporcionando una razón administrativa"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Turno desactivado exitosamente", content = @Content(schema = @Schema(implementation = ShiftResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Debe proporcionar una razón"),
			@ApiResponse(responseCode = "404", description = "Turno no encontrado"),
			@ApiResponse(responseCode = "409", description = "El turno tiene asignaciones activas")
	})
	public StandardResponse<ShiftResponseDTO> deactivateShift(
			@PathVariable Long id,
			@Valid @RequestBody ShiftStatusChangeDTO statusChangeDTO) {

		log.warn("Desactivando turno {} - Razón: {}", id, statusChangeDTO.getReason());

		ShiftResponseDTO deactivated = shiftService.deactivateShift(id, statusChangeDTO);

		return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Turno desactivado exitosamente", deactivated);
	}

	/**
	 * Obtener todos los turnos activos
	 */
	@CrossOrigin
	@GetMapping("/active")
	@Operation(summary = "Obtener todos los turnos activos")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de turnos activos")
	})
	public StandardResponse<List<ShiftSummaryDTO>> getActiveShifts() {

		log.debug("Consultando turnos activos");

		List<ShiftSummaryDTO> shifts = shiftService.findAllActiveShifts();

		String message = String.format("Encontrados %d turnos activos", shifts.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, shifts);
	}

	/**
	 * Obtener turnos activos ordenados
	 */
	@CrossOrigin
	@GetMapping("/active/ordered")
	@Operation(
			summary = "Obtener turnos activos ordenados por hora de inicio",
			description = "Retorna solo turnos activos ordenados cronológicamente"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de turnos activos ordenados")
	})
	public StandardResponse<List<ShiftSummaryDTO>> getActiveShiftsOrdered() {

		log.debug("Consultando turnos activos ordenados");

		List<ShiftSummaryDTO> shifts = shiftService.findActiveShiftsOrdered();

		String message = String.format("Encontrados %d turnos activos ordenados", shifts.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, shifts);
	}

	/**
	 * Obtener todos los turnos inactivos
	 */
	@CrossOrigin
	@GetMapping("/inactive")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Obtener todos los turnos inactivos")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de turnos inactivos")
	})
	public StandardResponse<List<ShiftSummaryDTO>> getInactiveShifts() {

		log.debug("Consultando turnos inactivos");

		List<ShiftSummaryDTO> shifts = shiftService.findInactiveShifts();

		String message = String.format("Encontrados %d turnos inactivos", shifts.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, shifts);
	}

	/**
	 * Contar turnos activos
	 */
	@CrossOrigin
	@GetMapping("/active/count")
	@Operation(summary = "Contar turnos activos")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Número de turnos activos")
	})
	public StandardResponse<Long> countActiveShifts() {

		Long count = shiftService.countActiveShifts();

		String message = String.format("Hay %d turnos activos", count);

		return ResponseBuilder.with(HttpStatus.OK, true, message, count);
	}

	/**
	 * Buscar turno que contiene una hora específica
	 */
	@CrossOrigin
	@GetMapping("/at-time/{time}")
	@Operation(
			summary = "Buscar turno activo a una hora específica",
			description = "Retorna el turno activo que contiene la hora especificada"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Turno encontrado", content = @Content(schema = @Schema(implementation = ShiftResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "No hay turno activo para esta hora")
	})
	public StandardResponse<ShiftResponseDTO> getShiftAtTime(
			@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
			@Parameter(description = "Hora en formato HH:mm:ss") LocalTime time) {

		log.debug("Buscando turno activo a las: {}", time);

		ShiftResponseDTO shift = shiftService.findShiftAtTime(time);

		String message = String.format("Turno activo a las %s: %s", time, shift.getShiftDescription());

		return ResponseBuilder.with(HttpStatus.OK, true, message, shift);
	}

	/**
	 * Obtener el turno actual (hora del sistema)
	 */
	@CrossOrigin
	@GetMapping("/current")
	@Operation(
			summary = "Obtener el turno actual",
			description = "Retorna el turno activo en este momento según la hora del sistema"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Turno actual encontrado", content = @Content(schema = @Schema(implementation = ShiftResponseDTO.class))),
			@ApiResponse(responseCode = "404", description = "No hay turno activo en este momento")
	})
	public StandardResponse<ShiftResponseDTO> getCurrentShift() {

		log.debug("Consultando turno actual");

		ShiftResponseDTO shift = shiftService.getCurrentShift();

		String message = String.format("Turno actual: %s", shift.getShiftDescription());

		return ResponseBuilder.with(HttpStatus.OK, true, message, shift);
	}

	/**
	 * Obtener turnos que cruzan medianoche
	 */
	@CrossOrigin
	@GetMapping("/crossing-midnight")
	@Operation(
			summary = "Obtener turnos que cruzan medianoche",
			description = "Retorna turnos cuya hora de fin es anterior a la de inicio (e.g., 23:00 - 07:00)"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de turnos nocturnos")
	})
	public StandardResponse<List<ShiftSummaryDTO>> getShiftsCrossingMidnight() {

		log.debug("Consultando turnos que cruzan medianoche");

		List<ShiftSummaryDTO> shifts = shiftService.findShiftsCrossingMidnight();

		String message = String.format("Encontrados %d turnos que cruzan medianoche", shifts.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, shifts);
	}

	/**
	 * Buscar turnos por duración exacta
	 */
	@CrossOrigin
	@GetMapping("/duration/{hours}")
	@Operation(
			summary = "Buscar turnos por duración exacta",
			description = "Retorna turnos que tienen exactamente X horas de duración"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de turnos")
	})
	public StandardResponse<List<ShiftSummaryDTO>> getShiftsByDuration(
			@PathVariable @Parameter(description = "Duración en horas") long hours) {

		log.debug("Consultando turnos con duración de {} horas", hours);

		List<ShiftSummaryDTO> shifts = shiftService.findShiftsByDuration(hours);

		String message = String.format("Encontrados %d turnos de %d horas", shifts.size(), hours);

		return ResponseBuilder.with(HttpStatus.OK, true, message, shifts);
	}

	/**
	 * Buscar turnos con duración mínima
	 */
	@CrossOrigin
	@GetMapping("/min-duration/{minHours}")
	@Operation(
			summary = "Buscar turnos con duración mínima",
			description = "Retorna turnos con al menos X horas de duración"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista de turnos")
	})
	public StandardResponse<List<ShiftSummaryDTO>> getShiftsWithMinDuration(
			@PathVariable @Parameter(description = "Duración mínima en horas") long minHours) {

		log.debug("Consultando turnos con duración mínima de {} horas", minHours);

		List<ShiftSummaryDTO> shifts = shiftService.findShiftsWithMinDuration(minHours);

		String message = String.format("Encontrados %d turnos con al menos %d horas", shifts.size(), minHours);

		return ResponseBuilder.with(HttpStatus.OK, true, message, shifts);
	}

	/**
	 * Verificar si existe un turno por tipo
	 */
	@CrossOrigin
	@GetMapping("/exists/{shiftType}")
	@Operation(
			summary = "Verificar si existe un turno por tipo",
			description = "Retorna true si existe un turno del tipo especificado"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Verificación completada")
	})
	public StandardResponse<Boolean> existsByShiftType(
			@PathVariable @Parameter(description = "Tipo de turno") ShiftType shiftType) {

		log.debug("Verificando existencia de turno tipo: {}", shiftType);

		Boolean exists = shiftService.existsByShiftType(shiftType);

		String message = exists
				? String.format("Existe un turno de tipo %s", shiftType.getDisplayName())
				: String.format("No existe turno de tipo %s", shiftType.getDisplayName());

		return ResponseBuilder.with(HttpStatus.OK, true, message, exists);
	}

	/**
	 * Crear turnos por defecto del sistema
	 * Solo administradores - Para inicialización
	 */
	@CrossOrigin
	@PostMapping("/admin/create-defaults")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Crear turnos por defecto (ADMIN)",
			description = "Crea los 3 turnos estándar: Mañana (07:00-15:00), Tarde (15:00-23:00), Noche (23:00-07:00). Solo si no existen."
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Turnos por defecto creados"),
			@ApiResponse(responseCode = "409", description = "Los turnos ya existen")
	})
	public StandardResponse<Void> createDefaultShifts() {

		log.warn("Creando turnos por defecto del sistema");

		shiftService.createDefaultShifts();

		return ResponseBuilder.withCreatedElements(HttpStatus.CREATED, true, 3, "Turnos por defecto creados exitosamente");
	}
}