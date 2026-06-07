package com.abcm0018.sai.workshift.infrastructure.controller;

import com.abcm0018.sai.shared.response.ResponseBuilder;
import com.abcm0018.sai.shared.response.StandardResponse;
import com.abcm0018.sai.workshift.application.dtos.CreateWorkshiftRequestDTO;
import com.abcm0018.sai.workshift.application.dtos.UpdateWorkshiftRequestDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftFilterDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftResponseDTO;
import com.abcm0018.sai.workshift.application.service.WorkshiftService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

	@CrossOrigin
	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Crea turnos para las fechas indicadas",
			description = "SOLO para correcciones administrativas."
	)
	public StandardResponse<Void> createWorkshifts(@Valid @RequestBody CreateWorkshiftRequestDTO requestDTO) {
		Integer numWorkshifts = workshiftService.generateWorkshiftSchedule(requestDTO);

		if (numWorkshifts == 0) {
			return ResponseBuilder.with(HttpStatus.OK, true, "La planificación ya está actualizada. No se crearon nuevos turnos");
		}

		return  ResponseBuilder.withCreatedElements(HttpStatus.CREATED, true, numWorkshifts,
				String.format("Planificación completada. Se generaron %d turnos nuevos.", numWorkshifts));
	}

	@CrossOrigin
	@GetMapping("/search")
	@Operation(
			summary = "Buscar turnos con filtros",
			description = "Permite filtrar por operario, fecha, tipo, ect. Soporta paginación y ordenamiento"
	)
	public StandardResponse<Page<WorkshiftResponseDTO>> search(
			// Desglosa los campos DTO como query params individuales en la documentación
			@ParameterObject @Valid WorkshiftFilterDTO filterDTO,
			// Configuración de paginación por defecto
			// Si el front no manda nada, usamos página 0, tamaño 20, ordenado por fecha en orden descendente
			@ParameterObject @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC)
			Pageable pageable) {

		Page<WorkshiftResponseDTO> resultPage = workshiftService.findWithFilters(filterDTO, pageable);
		return ResponseBuilder.with(HttpStatus.OK, true, "Consulta realizada exitosamente", resultPage);

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
	public StandardResponse<WorkshiftResponseDTO> updateWorkshift(@PathVariable Long id, @Valid @RequestBody UpdateWorkshiftRequestDTO requestDTO) {

		log.warn("Actualización manual de turno {} - Motivo: {}", id, requestDTO.getReason());

		WorkshiftResponseDTO updated = workshiftService.updateWorkshift(id, requestDTO);

		return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Turno actualizado exitosamente", updated);
	}

	/**
	 * Obtener un turno por ID
	 */
	@CrossOrigin
	@GetMapping("/{id}")
	@Operation(summary = "Obtener turno por ID")
	public StandardResponse<WorkshiftResponseDTO> getWorkshiftById(@PathVariable @Parameter(description = "ID del turno") Long id) {

		log.debug("Consultando turno con ID: {}", id);

		WorkshiftResponseDTO workshift = workshiftService.findById(id);

		return ResponseBuilder.with(HttpStatus.OK, true, "Turno encontrado", workshift);
	}

	/**
	 * Eliminar un turno
	 * Solo si no tiene palets ni fichajes asociados
	 */
	@CrossOrigin
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Eliminar un turno",
			description = "Solo se puede eliminar si no tiene fichajes asociados"
	)
	public StandardResponse<Void> deleteWorkshift(@PathVariable @Parameter(description = "ID del turno") Long id) {
		log.warn("Eliminando turno con ID: {}", id);

		int deletedCount = workshiftService.deleteWorkshift(id);

		if (deletedCount == 0) {
			return ResponseBuilder.with(HttpStatus.CONFLICT, false, "No se puede eliminar el turno porque tiene fichajes asociados o no existe.");
		}

		return ResponseBuilder.withDeletedElements(HttpStatus.OK, true, deletedCount, "Turno eliminado exitosamente");
	}


	/**
	 * Obtener el turno de hoy de un usuario
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/today")
	@Operation(summary = "Obtener el turno de hoy de un usuario")
	public StandardResponse<WorkshiftResponseDTO> getTodayWorkshift(@PathVariable Long userId) {

		log.debug("Consultando turno de hoy para usuario: {}", userId);

		WorkshiftResponseDTO workshift = workshiftService.getTodayWorkshift(userId);

		return ResponseBuilder.with(HttpStatus.OK, true, "Turno de hoy encontrado", workshift);
	}

	/**
	 * Obtener el próximo turno de un usuario
	 */
	@CrossOrigin
	@GetMapping("/user/{userId}/next")
	@Operation(summary = "Obtener el próximo turno de un usuario")
	public StandardResponse<WorkshiftResponseDTO> getNextWorkshift(@PathVariable Long userId) {

		log.debug("Consultando próximo turno del usuario: {}", userId);

		WorkshiftResponseDTO workshift = workshiftService.getNextWorkshift(userId);

		return ResponseBuilder.with(HttpStatus.OK, true, "Próximo turno encontrado", workshift);
	}

	/**
	 * Genera turnos para el rango de fechas seleccionado desde la web.
	 * Acepta el mismo payload que {@code POST /api/v1/workshifts} para que
	 * {@code GenerateScheduleModal} pueda enviar {@code fromDate} y {@code toDate}.
	 */
	@CrossOrigin
	@PostMapping("/generate-schedule")
	@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
	@Operation(
			summary = "Generar planificación para un rango de fechas",
			description = "Genera turnos para todos los operarios activos en el rango indicado " +
					"respetando la rotación estándar. Evicta la caché y popula Redis inmediatamente."
	)
	public StandardResponse<Void> generateScheduleForRange(@Valid @RequestBody CreateWorkshiftRequestDTO requestDTO) {

		log.info("Generando planificación manual: {} → {}", requestDTO.getFromDate(), requestDTO.getToDate());

		Integer count = workshiftService.generateWorkshiftSchedule(requestDTO);

		if (count == 0) {
			return ResponseBuilder.with(HttpStatus.OK, true,
					"La planificación ya está actualizada. No se crearon nuevos turnos.");
		}
		return ResponseBuilder.withCreatedElements(HttpStatus.CREATED, true, count,
				String.format("Planificación completada. Se generaron %d turnos nuevos.", count));
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
