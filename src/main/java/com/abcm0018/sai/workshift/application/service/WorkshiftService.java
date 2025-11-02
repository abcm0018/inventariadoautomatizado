package com.abcm0018.sai.workshift.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
import com.abcm0018.sai.workshift.domain.entity.Workshift;

public interface WorkshiftService {

	// ========== OPERACIONES EXCEPCIONALES (USO ADMINISTRATIVO) ==========

	/**
	 * Crear un turno individual de forma manual
	 * SOLO para casos excepcionales: emergencias, ausencias, turnos extra
	 * La planificación normal se hace automáticamente con el cron
	 *
	 * @param requestDTO DTO con userId, shiftId, date y reason
	 * @return WorkshiftDetailResponseDTO creado
	 */
	WorkshiftDetailResponseDTO createExceptionalWorkshift(CreateWorkshiftRequestDTO requestDTO);

	/**
	 * Actualizar un turno existente de forma manual
	 * SOLO para correcciones administrativas
	 *
	 * @param id ID del workshift
	 * @param requestDTO DTO con campos a actualizar y reason obligatorio
	 * @return WorkshiftDetailResponseDTO actualizado
	 */
	WorkshiftDetailResponseDTO updateWorkshift(Long id, UpdateWorkshiftRequestDTO requestDTO);

	/**
	 * Buscar workshift por ID
	 *
	 * @param id ID del workshift
	 * @return WorkshiftDetailResponseDTO con información completa
	 */
	WorkshiftDetailResponseDTO findById(Long id);

	/**
	 * Listar todos los workshifts con paginación
	 *
	 * @param pageable Configuración de paginación
	 * @return Página de WorkshiftSummaryDTO
	 */
	Page<WorkshiftSummaryDTO> findAll(Pageable pageable);

	/**
	 * Eliminar un workshift
	 *
	 * @param id ID del workshift
	 */
	void deleteWorkshift(Long id);

	// ========== BÚSQUEDAS POR USUARIO ==========

	/**
	 * Buscar todos los workshifts de un usuario
	 *
	 * @param userId ID del usuario
	 * @return Lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> findByUser(Long userId);

	/**
	 * Buscar workshifts de un usuario con paginación
	 *
	 * @param userId ID del usuario
	 * @param pageable Configuración de paginación
	 * @return Página de WorkshiftSummaryDTO
	 */
	Page<WorkshiftSummaryDTO> findByUserPaginated(Long userId, Pageable pageable);

	/**
	 * Obtener el turno de hoy de un usuario
	 *
	 * @param userId ID del usuario
	 * @return WorkshiftDetailResponseDTO del turno de hoy
	 */
	WorkshiftDetailResponseDTO getTodayWorkshift(Long userId);

	/**
	 * Buscar turnos futuros de un usuario
	 *
	 * @param userId ID del usuario
	 * @return Lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> findFutureWorkshifts(Long userId);

	/**
	 * Buscar turnos pasados de un usuario
	 *
	 * @param userId ID del usuario
	 * @return Lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> findPastWorkshifts(Long userId);

	/**
	 * Obtener el próximo turno de un usuario
	 *
	 * @param userId ID del usuario
	 * @return WorkshiftDetailResponseDTO del próximo turno
	 */
	WorkshiftDetailResponseDTO getNextWorkshift(Long userId);

	/**
	 * Buscar workshifts de un usuario en un rango de fechas
	 *
	 * @param userId ID del usuario
	 * @param startDate Fecha de inicio
	 * @param endDate Fecha de fin
	 * @return Lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> findByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);

	/**
	 * Contar workshifts de un usuario
	 *
	 * @param userId ID del usuario
	 * @return Cantidad de workshifts
	 */
	Long countWorkshiftsByUser(Long userId);

	// ========== BÚSQUEDAS POR FECHA ==========

	/**
	 * Buscar todos los workshifts de una fecha
	 *
	 * @param date Fecha
	 * @return Lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> findByDate(LocalDate date);

	/**
	 * Buscar workshifts de hoy
	 *
	 * @return Lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> findTodayWorkshifts();

	/**
	 * Buscar workshifts en un rango de fechas
	 *
	 * @param startDate Fecha de inicio
	 * @param endDate Fecha de fin
	 * @return Lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> findByDateRange(LocalDate startDate, LocalDate endDate);

	/**
	 * Buscar usuarios que trabajan en una fecha
	 *
	 * @param date Fecha
	 * @return Lista de IDs de usuarios
	 */
	List<Long> findUserIdsWorkingOnDate(LocalDate date);

	/**
	 * Contar workshifts en una fecha
	 *
	 * @param date Fecha
	 * @return Cantidad de workshifts
	 */
	Long countWorkshiftsByDate(LocalDate date);

	// ========== BÚSQUEDAS POR PERÍODO ==========

	/**
	 * Buscar workshifts de la semana actual
	 *
	 * @return Lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> findCurrentWeekWorkshifts();

	/**
	 * Buscar workshifts del mes actual
	 *
	 * @return Lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> findCurrentMonthWorkshifts();

	/**
	 * Buscar workshifts de una semana específica
	 *
	 * @param anyDayOfWeek Cualquier día de la semana
	 * @return Lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> findWorkshiftsOfWeek(LocalDate anyDayOfWeek);

	/**
	 * Buscar workshifts de un mes específico
	 *
	 * @param year Año
	 * @param month Mes
	 * @return Lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> findWorkshiftsOfMonth(int year, int month);

	// ========== BÚSQUEDAS POR SHIFT ==========

	/**
	 * Buscar workshifts por ID de shift
	 *
	 * @param shiftId ID del shift
	 * @return Lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> findByShift(Long shiftId);

	/**
	 * Buscar workshifts por tipo de turno
	 *
	 * @param shiftType Tipo de turno (MORNING, AFTERNOON, NIGHT)
	 * @return Lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> findByShiftType(ShiftType shiftType);

	/**
	 * Buscar workshifts de un usuario por tipo de turno
	 *
	 * @param userId ID del usuario
	 * @param shiftType Tipo de turno
	 * @return Lista de WorkshiftResponseDTO
	 */
	List<WorkshiftResponseDTO> findByUserAndShiftType(Long userId, ShiftType shiftType);

	// ========== BÚSQUEDAS AVANZADAS CON FILTROS ==========

	/**
	 * Buscar workshifts aplicando múltiples filtros
	 *
	 * @param filterDTO DTO con filtros opcionales
	 * @param pageable Configuración de paginación
	 * @return Página de WorkshiftSummaryDTO
	 */
	Page<WorkshiftSummaryDTO> findWithFilters(WorkshiftFilterDTO filterDTO, Pageable pageable);

	// ========== VALIDACIONES Y CONFLICTOS ==========

	/**
	 * Verificar si existen conflictos para una asignación
	 *
	 * @param userId ID del usuario
	 * @param date Fecha
	 * @param excludeId ID a excluir (opcional, para actualizaciones)
	 * @return WorkshiftConflictDTO con información de conflictos
	 */
	WorkshiftConflictDTO checkConflicts(Long userId, LocalDate date, Long excludeId);

	// ========== ESTADÍSTICAS ==========

	/**
	 * Obtener estadísticas globales de workshifts
	 *
	 * @param startDate Fecha de inicio (opcional)
	 * @param endDate Fecha de fin (opcional)
	 * @return WorkshiftStatisticsDTO con métricas agregadas
	 */
	WorkshiftStatisticsDTO getStatistics(LocalDate startDate, LocalDate endDate);

	// ========== OPERACIONES DE LIMPIEZA ==========

	/**
	 * Limpiar workshifts antiguos (más de 12 meses)
	 */
	void cleanOldWorkshifts();

	// ========== PLANIFICACIÓN AUTOMÁTICA DE TURNOS ==========

	/**
	 * Genera la planificación de turnos para la próxima semana (Lun-Vie)
	 * Se ejecuta automáticamente cada viernes mediante cron job
	 * Asigna turnos rotativos a todos los operadores activos
	 *
	 * @return WeeklyScheduleResponseDTO con resultados de la generación
	 */
	WeeklyScheduleResponseDTO generateNextWeekSchedule();

	/**
	 * Encuentra un turno para un empleado y fecha, usando la caché de Redis primero. Optimizado para alto rendimiento (ej. Consumidor de RabbitMQ).
	 *
	 * @param employeeNumber El número de empleado
	 * @param date La fecha del turno
	 * @return Un Optional con el DTO del Workshift si se encuentra (en caché o BBDD)
	 */
	Optional<Workshift> findCachedWorkshiftByEmployeeAndDate(String employeeNumber, LocalDate date);
}
