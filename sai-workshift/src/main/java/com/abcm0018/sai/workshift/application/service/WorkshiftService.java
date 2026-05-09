package com.abcm0018.sai.workshift.application.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.abcm0018.sai.workshift.application.dtos.CreateWorkshiftRequestDTO;
import com.abcm0018.sai.workshift.application.dtos.UpdateWorkshiftRequestDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftFilterDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftResponseDTO;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

public interface WorkshiftService {
	/**
	 * Genera el calendario de turnos de forma manual
	 *
	 * @param requestDTO con la fecha de incio del turno y fecha de fin del turno
	 * @return el total de turnos generados.
	 */
	Integer generateWorkshiftSchedule(CreateWorkshiftRequestDTO requestDTO);

	/**
	 * Actualizar un turno existente de forma manual
	 * SOLO para correcciones administrativas
	 *
	 * @param id ID del workshift
	 * @param requestDTO DTO con campos a actualizar y reason obligatorio
	 * @return WorkshiftDetailResponseDTO actualizado
	 */
	WorkshiftResponseDTO updateWorkshift(Long id, UpdateWorkshiftRequestDTO requestDTO);

	/**
	 * Buscar workshift por ID
	 *
	 * @param id ID del workshift
	 * @return WorkshiftDetailResponseDTO con información completa
	 */
	WorkshiftResponseDTO findById(Long id);

	/**
	 * Eliminar un workshift
	 *
	 * @param id ID del workshift
	 */
	int deleteWorkshift(Long id);

	/**
	 * Obtener el turno de hoy de un usuario
	 *
	 * @param userId ID del usuario
	 * @return WorkshiftDetailResponseDTO del turno de hoy
	 */
	WorkshiftResponseDTO getTodayWorkshift(Long userId);

	/**
	 * Obtener el próximo turno de un usuario
	 *
	 * @param userId ID del usuario
	 * @return WorkshiftDetailResponseDTO del próximo turno
	 */
	WorkshiftResponseDTO getNextWorkshift(Long userId);

	/**
	 * Buscar workshifts aplicando múltiples filtros
	 *
	 * @param filterDTO DTO con filtros opcionales
	 * @param pageable Configuración de paginación
	 * @return Página de WorkshiftSummaryDTO
	 */
	Page<WorkshiftResponseDTO> findWithFilters(WorkshiftFilterDTO filterDTO, Pageable pageable);

	/**
	 * Limpiar workshifts antiguos (más de 12 meses)
	 */
	void cleanOldWorkshifts();

	/**
	 * Genera la planificación de turnos para la próxima semana (Lun-Vie)
	 * Se ejecuta automáticamente cada viernes mediante cron job
	 * Asigna turnos rotativos a todos los operadores activos
	 */
	void generateNextWeekSchedule();

	/**
	 * Encuentra un turno para un empleado y fecha, usando la caché de Redis primero. Optimizado para alto rendimiento (ej. Consumidor de RabbitMQ).
	 *
	 * @param employeeNumber El número de empleado
	 * @param date La fecha del turno
	 * @return Un Optional con el DTO del Workshift si se encuentra (en caché o BBDD)
	 */
	Optional<Workshift> findCachedWorkshiftByEmployeeAndDate(String employeeNumber, LocalDate date);
}
