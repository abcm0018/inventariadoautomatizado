package com.abcm0018.sai.workshift.infrastructure.controller;

import java.time.LocalDate;
import java.util.List;

import com.abcm0018.sai.shared.response.ResponseBuilder;
import com.abcm0018.sai.shared.response.StandardResponse;
import com.abcm0018.sai.shift.domain.enums.ShiftType;
import com.abcm0018.sai.workshift.application.dtos.CreateWorkshiftRequestDTO;
import com.abcm0018.sai.workshift.application.dtos.UpdateWorkshiftRequestDTO;
import com.abcm0018.sai.workshift.application.dtos.WeeklyScheduleResponseDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftConflictDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftDetailResponseDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftFilterDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftResponseDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftStatisticsDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftSummaryDTO;
import com.abcm0018.sai.workshift.application.service.WorkshiftService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de asignaciones de turnos (Workshifts)
 * <p>
 * NOTA IMPORTANTE: La planificación automática de turnos se ejecuta mediante CRON
 * cada viernes a las 18:00. Los endpoints manuales son SOLO para casos excepcionales.
 */
@RestController
@RequestMapping(value = "/api/v1/workshifts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Workshifts", description = "Endpoints para gestión de asignaciones de turnos")
public class WorkshiftController {

	private final WorkshiftService workshiftService;

	/**
	 * Crear una asignación de turno de forma MANUAL (EXCEPCIONAL)
	 * Solo para emergencias, ausencias o turnos extra
	 * Requiere motivo obligatorio para auditoría
	 */
	@CrossOrigin
	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Crear turno excepcional manualmente",
			description = "SOLO para casos excepcionales. La planificación normal es automática. Requiere motivo obligatorio."
	)
	public StandardResponse<WorkshiftDetailResponseDTO> createExceptionalWorkshift(@Valid @RequestBody CreateWorkshiftRequestDTO requestDTO) {

		log.warn("Creación manual de turno - Usuario: {}, Motivo: {}", requestDTO.getUserId(), requestDTO.getReason());

		WorkshiftDetailResponseDTO created = workshiftService.createExceptionalWorkshift(requestDTO);

		return ResponseBuilder.withCreatedElements(HttpStatus.CREATED, true, 1, "Turno excepcional creado exitosamente", created);
	}

	/**
	 * Actualizar una asignación de turno (EXCEPCIONAL)
	 * Solo para correcciones administrativas
	 */
	@CrossOrigin
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Actualizar turno manualmente",
			description = "SOLO para correcciones administrativas. Requiere motivo obligatorio."
	)
	public StandardResponse<WorkshiftDetailResponseDTO> updateWorkshift(@PathVariable Long id, @Valid @RequestBody UpdateWorkshiftRequestDTO requestDTO) {

		log.warn("Actualización manual de turno {} - Motivo: {}", id, requestDTO.getReason());

		WorkshiftDetailResponseDTO updated = workshiftService.updateWorkshift(id, requestDTO);

		return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Turno actualizado exitosamente", updated);
	}

	/**
	 * Obtener un turno por ID
	 */
	@CrossOrigin
	@GetMapping("/{id}")
	@Operation(summary = "Obtener turno por ID")
	public StandardResponse<WorkshiftDetailResponseDTO> getWorkshiftById(@PathVariable @Parameter(description = "ID del turno") Long id) {

		log.debug("Consultando turno con ID: {}", id);

		WorkshiftDetailResponseDTO workshift = workshiftService.findById(id);

		return ResponseBuilder.with(HttpStatus.OK, true, "Turno encontrado", workshift);
	}

	/**
	 * Listar todos los turnos con paginación
	 */
	@CrossOrigin
	@GetMapping
	@Operation(summary = "Listar todos los turnos con paginación")
	public StandardResponse<Page<WorkshiftSummaryDTO>> getAllWorkshifts(@PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {

		log.debug("Listando turnos - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());

		Page<WorkshiftSummaryDTO> workshifts = workshiftService.findAll(pageable);

		String message = String.format("Página %d de %d (Total: %d turnos)", workshifts.getNumber() + 1, workshifts.getTotalPages(), workshifts.getTotalElements());

		return ResponseBuilder.with(HttpStatus.OK, true, message, workshifts);
	}

	/**
	 * Eliminar un turno
	 * Solo si no tiene palets ni fichajes asociados
	 */
	@CrossOrigin
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Eliminar un turno",
			description = "Solo se puede eliminar si no tiene palets ni fichajes asociados"
	)
	public StandardResponse<Void> deleteWorkshift(@PathVariable @Parameter(description = "ID del turno") Long id) {
		log.warn("Eliminando turno con ID: {}", id);

		workshiftService.deleteWorkshift(id);

		return ResponseBuilder.withDeletedElements(HttpStatus.OK, true, 1, "Turno eliminado exitosamente");
	}


	/**
	 * Obtener todos los turnos de un usuario
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}")
	@Operation(summary = "Obtener todos los turnos de un usuario")
	public StandardResponse<List<WorkshiftResponseDTO>> getWorkshiftsByUser(@PathVariable @Parameter(description = "ID del usuario") Long userId) {

		log.debug("Consultando turnos del usuario: {}", userId);

		List<WorkshiftResponseDTO> workshifts = workshiftService.findByUser(userId);

		String message = String.format("Encontrados %d turnos para el usuario", workshifts.size());
		return ResponseBuilder.with(HttpStatus.OK, true, message, workshifts);
	}

	/**
	 * Obtener turnos de un usuario con paginación
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/paginated")
	@Operation(summary = "Obtener turnos de un usuario con paginación")
	public StandardResponse<Page<WorkshiftSummaryDTO>> getWorkshiftsByUserPaginated(@PathVariable Long userId, @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {

		Page<WorkshiftSummaryDTO> workshifts = workshiftService.findByUserPaginated(userId, pageable);

		return ResponseBuilder.with(HttpStatus.OK, true, "Turnos del usuario obtenidos exitosamente", workshifts);
	}

	/**
	 * Obtener el turno de hoy de un usuario
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/today")
	@Operation(summary = "Obtener el turno de hoy de un usuario")
	public StandardResponse<WorkshiftDetailResponseDTO> getTodayWorkshift(@PathVariable Long userId) {

		log.debug("Consultando turno de hoy para usuario: {}", userId);

		WorkshiftDetailResponseDTO workshift = workshiftService.getTodayWorkshift(userId);

		return ResponseBuilder.with(HttpStatus.OK, true, "Turno de hoy encontrado", workshift);
	}

	/**
	 * Obtener turnos futuros de un usuario
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/future")
	@Operation(summary = "Obtener turnos futuros de un usuario")
	public StandardResponse<List<WorkshiftResponseDTO>> getFutureWorkshifts(@PathVariable Long userId) {

		log.debug("Consultando turnos futuros del usuario: {}", userId);

		List<WorkshiftResponseDTO> workshifts = workshiftService.findFutureWorkshifts(userId);

		String message = String.format("Encontrados %d turnos futuros", workshifts.size());
		return ResponseBuilder.with(HttpStatus.OK, true, message, workshifts);
	}

	/**
	 * Obtener turnos pasados de un usuario
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/past")
	@Operation(summary = "Obtener turnos pasados de un usuario")
	public StandardResponse<List<WorkshiftResponseDTO>> getPastWorkshifts(@PathVariable Long userId) {

		log.debug("Consultando turnos pasados del usuario: {}", userId);

		List<WorkshiftResponseDTO> workshifts = workshiftService.findPastWorkshifts(userId);

		String message = String.format("Encontrados %d turnos pasados", workshifts.size());
		return ResponseBuilder.with(HttpStatus.OK, true, message, workshifts);
	}

	/**
	 * Obtener el próximo turno de un usuario
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/next")
	@Operation(summary = "Obtener el próximo turno de un usuario")
	public StandardResponse<WorkshiftDetailResponseDTO> getNextWorkshift(@PathVariable Long userId) {

		log.debug("Consultando próximo turno del usuario: {}", userId);

		WorkshiftDetailResponseDTO workshift = workshiftService.getNextWorkshift(userId);

		return ResponseBuilder.with(HttpStatus.OK, true, "Próximo turno encontrado", workshift);
	}

	/**
	 * Obtener turnos de un usuario en un rango de fechas
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/date-range")
	@Operation(summary = "Obtener turnos de un usuario en un rango de fechas")
	public StandardResponse<List<WorkshiftResponseDTO>> getWorkshiftsByUserAndDateRange(
			@PathVariable Long userId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.debug("Consultando turnos del usuario {} desde {} hasta {}", userId, startDate, endDate);

		List<WorkshiftResponseDTO> workshifts = workshiftService.findByUserAndDateRange(userId, startDate, endDate);

		String message = String.format("Encontrados %d turnos para el usuario en el rango", workshifts.size());
		return ResponseBuilder.with(HttpStatus.OK, true, message, workshifts);
	}

	/**
	 * Contar turnos de un usuario
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/count")
	@Operation(summary = "Contar turnos de un usuario")
	public StandardResponse<Long> countWorkshiftsByUser(@PathVariable Long userId) {

		Long count = workshiftService.countWorkshiftsByUser(userId);

		String message = String.format("El usuario tiene %d turnos asignados", count);

		return ResponseBuilder.with(HttpStatus.OK, true, message, count);
	}

	/**
	 * Obtener todos los turnos de una fecha específica
	 */
	@CrossOrigin
	@GetMapping("/date/{date}")
	@Operation(summary = "Obtener turnos de una fecha específica")
	public StandardResponse<List<WorkshiftResponseDTO>> getWorkshiftsByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		log.debug("Consultando turnos de la fecha: {}", date);

		List<WorkshiftResponseDTO> workshifts = workshiftService.findByDate(date);

		String message = String.format("Encontrados %d turnos para la fecha %s", workshifts.size(), date);
		return ResponseBuilder.with(HttpStatus.OK, true, message,workshifts);
	}

	/**
	 * Obtener turnos de hoy
	 */
	@CrossOrigin
	@GetMapping("/today")
	@Operation(summary = "Obtener todos los turnos de hoy")
	public StandardResponse<List<WorkshiftResponseDTO>> getTodayWorkshifts() {

		log.debug("Consultando turnos de hoy");

		List<WorkshiftResponseDTO> workshifts = workshiftService.findTodayWorkshifts();

		String message = String.format("Encontrados %d turnos para hoy", workshifts.size());
		return ResponseBuilder.with(HttpStatus.OK, true, message,workshifts);
	}

	/**
	 * Obtener turnos en un rango de fechas
	 */
	@CrossOrigin
	@GetMapping("/date-range")
	@Operation(summary = "Obtener turnos en un rango de fechas")
	public StandardResponse<List<WorkshiftResponseDTO>> getWorkshiftsByDateRange(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.debug("Consultando turnos desde {} hasta {}", startDate, endDate);

		List<WorkshiftResponseDTO> workshifts = workshiftService.findByDateRange(startDate, endDate);

		String message = String.format("Encontrados %d turnos en el rango", workshifts.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, workshifts);
	}

	/**
	 * Obtener Identificadores de usuarios que trabajan en una fecha
	 */
	@CrossOrigin
	@GetMapping("/date/{date}/users")
	@Operation(summary = "Obtener usuarios que trabajan en una fecha")
	public StandardResponse<List<Long>> getUsersWorkingOnDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		log.debug("Consultando usuarios trabajando el: {}", date);

		List<Long> userIds = workshiftService.findUserIdsWorkingOnDate(date);

		String message = String.format("%d usuarios trabajando en la fecha", userIds.size());
		return ResponseBuilder.with(HttpStatus.OK, true, message, userIds);
	}

	/**
	 * Contar turnos en una fecha
	 */
	@CrossOrigin
	@GetMapping("/date/{date}/count")
	@Operation(summary = "Contar turnos de una fecha")
	public StandardResponse<Long> countWorkshiftsByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		Long count = workshiftService.countWorkshiftsByDate(date);

		String message = String.format("Hay %d turnos asignados para la fecha %s", count, date);

		return ResponseBuilder.with(HttpStatus.OK, true, message, count);
	}

	/**
	 * Obtener turnos de la semana actual
	 */
	@CrossOrigin
	@GetMapping("/current-week")
	@Operation(summary = "Obtener turnos de la semana actual (Lun-Dom)")
	public StandardResponse<List<WorkshiftResponseDTO>> getCurrentWeekWorkshifts() {

		log.debug("Consultando turnos de la semana actual");

		List<WorkshiftResponseDTO> workshifts = workshiftService.findCurrentWeekWorkshifts();
		String message = String.format("Encontrados %d turnos esta semana", workshifts.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, workshifts);
	}

	/**
	 * Obtener turnos del mes actual
	 */
	@CrossOrigin
	@GetMapping("/current-month")
	@Operation(summary = "Obtener turnos del mes actual")
	public StandardResponse<List<WorkshiftResponseDTO>> getCurrentMonthWorkshifts() {

		log.debug("Consultando turnos del mes actual");

		List<WorkshiftResponseDTO> workshifts = workshiftService.findCurrentMonthWorkshifts();
		String msg = String.format("Encontrados %d turnos este mes", workshifts.size());

		return ResponseBuilder.with(HttpStatus.OK, true, msg, workshifts);
	}

	/**
	 * Obtener turnos de una semana específica
	 */
	@CrossOrigin
	@GetMapping("/week")
	@Operation(summary = "Obtener turnos de una semana específica")
	public StandardResponse<List<WorkshiftResponseDTO>> getWorkshiftsOfWeek(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			@Parameter(description = "Cualquier día de la semana deseada") LocalDate date) {

		log.debug("Consultando turnos de la semana que contiene: {}", date);

		List<WorkshiftResponseDTO> workshifts = workshiftService.findWorkshiftsOfWeek(date);
		String msg = String.format("Encontrados %d turnos en la semana", workshifts.size());

		return ResponseBuilder.with(HttpStatus.OK, true, msg, workshifts);
	}

	/**
	 * Obtener turnos de un mes específico
	 */
	@CrossOrigin
	@GetMapping("/month")
	@Operation(summary = "Obtener turnos de un mes específico")
	public StandardResponse<List<WorkshiftResponseDTO>> getWorkshiftsOfMonth(
			@RequestParam @Parameter(description = "Año") int year,
			@RequestParam @Parameter(description = "Mes (1-12)") int month) {

		log.debug("Consultando turnos del mes {}/{}", month, year);

		List<WorkshiftResponseDTO> workshifts = workshiftService.findWorkshiftsOfMonth(year, month);
		String message = String.format("Encontrados %d turnos en %d/%d", workshifts.size(), month, year);

		return ResponseBuilder.with(HttpStatus.OK, true, message, workshifts);
	}

	/**
	 * Obtener turnos por ID de shift
	 */
	@CrossOrigin
	@GetMapping("/shift/{shiftId}")
	@Operation(summary = "Obtener turnos de un shift específico")
	public StandardResponse<List<WorkshiftResponseDTO>> getWorkshiftsByShift(@PathVariable Long shiftId) {

		log.debug("Consultando turnos del shift: {}", shiftId);

		List<WorkshiftResponseDTO> workshifts = workshiftService.findByShift(shiftId);
		String message = String.format("Encontrados %d turnos para el shift", workshifts.size());

		return ResponseBuilder.with(HttpStatus.OK, true, message, workshifts);
	}

	/**
	 * Obtener turnos por tipo de turno
	 */
	@CrossOrigin
	@GetMapping("/shift-type/{shiftType}")
	@Operation(summary = "Obtener turnos por tipo (MORNING, AFTERNOON, NIGHT)")
	public StandardResponse<List<WorkshiftResponseDTO>> getWorkshiftsByShiftType(@PathVariable ShiftType shiftType) {

		log.debug("Consultando turnos del tipo: {}", shiftType);

		List<WorkshiftResponseDTO> workshifts = workshiftService.findByShiftType(shiftType);
		String message = String.format("Encontrados %d turnos de tipo %s", workshifts.size(), shiftType.getDisplayName());

		return ResponseBuilder.with(HttpStatus.OK, true, message, workshifts);
	}

	/**
	 * Obtener turnos de un usuario por tipo de turno
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/shift-type/{shiftType}")
	@Operation(summary = "Obtener turnos de un usuario por tipo de turno")
	public StandardResponse<List<WorkshiftResponseDTO>> getWorkshiftsByUserAndShiftType(@PathVariable Long userId, @PathVariable ShiftType shiftType) {

		log.debug("Consultando turnos del usuario {} tipo {}", userId, shiftType);

		List<WorkshiftResponseDTO> workshifts = workshiftService.findByUserAndShiftType(userId, shiftType);

		String message = String.format("Encontrados %d turnos", workshifts.size());
		return ResponseBuilder.with(HttpStatus.OK, true, message, workshifts);
	}

	/**
	 * Buscar turnos con filtros múltiples
	 */
	@CrossOrigin
	@PostMapping("/search")
	@Operation(
			summary = "Búsqueda avanzada de turnos con filtros múltiples",
			description = "Permite combinar múltiples criterios de búsqueda"
	)
	public StandardResponse<Page<WorkshiftSummaryDTO>> searchWorkshifts(
			@RequestBody WorkshiftFilterDTO filterDTO,
			@PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {

		log.debug("Búsqueda avanzada con filtros: {}", filterDTO);

		Page<WorkshiftSummaryDTO> workshifts = workshiftService.findWithFilters(filterDTO, pageable);

		String message = String.format("Encontrados %d turnos que cumplen los filtros", workshifts.getTotalElements());
		return ResponseBuilder.with(HttpStatus.OK, true, message, workshifts);
	}

	/**
	 * Verificar conflictos para una asignación
	 */
	@CrossOrigin
	@GetMapping("/check-conflicts")
	@Operation(
			summary = "Verificar conflictos antes de crear/actualizar un turno",
			description = "Valida si hay turnos duplicados o conflictos para un usuario en una fecha"
	)
	public StandardResponse<WorkshiftConflictDTO> checkConflicts(
			@RequestParam Long userId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) Long excludeId) {

		log.debug("Verificando conflictos - Usuario: {}, Fecha: {}", userId, date);

		WorkshiftConflictDTO conflicts = workshiftService.checkConflicts(userId, date, excludeId);

		String message = conflicts.getHasConflicts() ? "Se detectaron conflictos" : "No hay conflictos";
		return ResponseBuilder.with(HttpStatus.OK, true, message, conflicts);
	}

	/**
	 * Obtener estadísticas globales de turnos
	 */
	@CrossOrigin
	@GetMapping("/statistics")
	@Operation(
			summary = "Obtener estadísticas globales de turnos",
			description = "Incluye métricas agregadas, distribuciones y auditoría"
	)
	public StandardResponse<WorkshiftStatisticsDTO> getStatistics(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		log.debug("Consultando estadísticas de turnos");

		WorkshiftStatisticsDTO statistics = workshiftService.getStatistics(startDate, endDate);

		return ResponseBuilder.with(HttpStatus.OK, true, "Estadísticas obtenidas exitosamente", statistics);
	}

	/**
	 * Generar planificación semanal manualmente (SOLO PARA TESTING)
	 * En producción se ejecuta automáticamente mediante CRON cada viernes 18:00
	 */
	@CrossOrigin
	@PostMapping("/generate-schedule")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
			summary = "Generar planificación de la próxima semana (MANUAL - SOLO TESTING)",
			description = "ADVERTENCIA: En producción esto se ejecuta automáticamente con CRON. Este endpoint es SOLO para testing o emergencias."
	)
	public StandardResponse<WeeklyScheduleResponseDTO> generateNextWeekSchedule() {

		log.warn("╔═══════════════════════════════════════════════════════════════════╗");
		log.warn("║  GENERACIÓN MANUAL DE PLANIFICACIÓN SEMANAL (NO RECOMENDADO)      ║");
		log.warn("║  Este endpoint es solo para testing/emergencias                   ║");
		log.warn("║  En producción usa el CRON automático                             ║");
		log.warn("╚═══════════════════════════════════════════════════════════════════╝");

		WeeklyScheduleResponseDTO schedule = workshiftService.generateNextWeekSchedule();

		return ResponseBuilder.withCreatedElements(HttpStatus.CREATED, true, schedule.getTotalWorkshiftsGenerated(), "Planificación semanal generada exitosamente", schedule);

	}

	/**
	 * Limpiar turnos antiguos (más de 12 meses)
	 */
	@CrossOrigin
	@DeleteMapping("/cleanup")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Limpiar turnos antiguos", description = "Elimina turnos con más de 12 meses de antigüedad")
	public StandardResponse<Void> cleanOldWorkshifts() {

		log.warn("Ejecutando limpieza de turnos antiguos");

		workshiftService.cleanOldWorkshifts();

		return ResponseBuilder.with(HttpStatus.OK, true, "Limpieza de turnos antiguos completada");
	}
}
