package com.abcm0018.sai.timesheet.application.service;


import com.abcm0018.sai.timesheet.application.dtos.TimesheetDetailResponseDTO;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetRequestDTO;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetResponseDTO;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetStatisticsDTO;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetSummaryDTO;
import com.abcm0018.sai.timesheet.domain.enums.TimesheetStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TimesheetService {

	/**
	 * Crea un nuevo registro de timesheet
	 * @param requestDTO Datos del timesheet a crear
	 * @return TimesheetDetailResponseDTO con el timesheet creado
	 */
	TimesheetDetailResponseDTO createTimesheet(TimesheetRequestDTO requestDTO);

	/**
	 * Busca un timesheet por ID
	 * @param id ID del timesheet
	 * @return TimesheetDetailResponseDTO con los detalles del timesheet
	 */
	TimesheetDetailResponseDTO findById(Long id);

	/**
	 * Lista todos los timesheets con paginación
	 * @param pageable Configuración de paginación
	 * @return Página de TimesheetSummaryDTO
	 */
	Page<TimesheetSummaryDTO> findAll(Pageable pageable);

	/**
	 * Actualiza un timesheet existente
	 * @param id ID del timesheet a actualizar
	 * @param requestDTO Nuevos datos del timesheet
	 * @return TimesheetDetailResponseDTO con el timesheet actualizado
	 */
	TimesheetDetailResponseDTO updateTimesheet(Long id, TimesheetRequestDTO requestDTO);

	/**
	 * Elimina un timesheet
	 * @param id ID del timesheet a eliminar
	 */
	void deleteTimesheet(Long id);

	// ========== OPERACIONES DE CHECK-IN / CHECK-OUT ==========

	/**
	 * Registra la entrada (check-in) de un usuario en su turno
	 * Crea automáticamente un timesheet para el workshift del día
	 * @param userId ID del usuario
	 * @return TimesheetDetailResponseDTO con el check-in registrado
	 */
	TimesheetDetailResponseDTO checkIn(Long userId);

	/**
	 * Registra la entrada (check-in) con una hora específica (para correcciones)
	 * @param userId ID del usuario
	 * @param checkInTime Hora de entrada
	 * @return TimesheetDetailResponseDTO con el check-in registrado
	 */
	TimesheetDetailResponseDTO checkInWithTime(Long userId, LocalDateTime checkInTime);

	/**
	 * Registra la salida (check-out) del usuario
	 * Cierra el timesheet abierto más reciente
	 * @param userId ID del usuario
	 * @return TimesheetDetailResponseDTO con el check-out registrado
	 */
	TimesheetDetailResponseDTO checkOut(Long userId);

	/**
	 * Registra la salida (check-out) con una hora específica (para correcciones)
	 * @param userId ID del usuario
	 * @param checkOutTime Hora de salida
	 * @return TimesheetDetailResponseDTO con el check-out registrado
	 */
	TimesheetDetailResponseDTO checkOutWithTime(Long userId, LocalDateTime checkOutTime);

	/**
	 * Verifica si un usuario tiene un timesheet abierto (sin check-out)
	 * @param userId ID del usuario
	 * @return true si tiene un timesheet abierto, false en caso contrario
	 */
	boolean hasOpenTimesheet(Long userId);

	/**
	 * Obtiene el último timesheet abierto de un usuario (si existe)
	 * @param userId ID del usuario
	 * @return TimesheetDetailResponseDTO del timesheet abierto, o null si no tiene
	 */
	TimesheetDetailResponseDTO getOpenTimesheet(Long userId);

	/**
	 * Busca el timesheet de un workshift específico
	 * @param workshiftId ID del workshift
	 * @return TimesheetDetailResponseDTO si existe, null en caso contrario
	 */
	TimesheetDetailResponseDTO findByWorkshiftId(Long workshiftId);

	/**
	 * Lista todos los timesheets de un workshift
	 * @param workshiftId ID del workshift
	 * @return Lista de TimesheetResponseDTO
	 */
	List<TimesheetResponseDTO> findByWorkshift(Long workshiftId);

	/**
	 * Busca todos los timesheets de un usuario
	 * @param userId ID del usuario
	 * @return Lista de TimesheetResponseDTO
	 */
	List<TimesheetResponseDTO> findByUserId(Long userId);

	/**
	 * Busca timesheets de un usuario con paginación
	 * @param userId ID del usuario
	 * @param pageable Configuración de paginación
	 * @return Página de TimesheetResponseDTO
	 */
	Page<TimesheetResponseDTO> findByUserId(Long userId, Pageable pageable);

	/**
	 * Busca timesheets de un usuario en un rango de fechas
	 * @param userId ID del usuario
	 * @param startDate Fecha de inicio
	 * @param endDate Fecha de fin
	 * @return Lista de TimesheetResponseDTO
	 */
	List<TimesheetResponseDTO> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);

	/**
	 * Busca el timesheet de un usuario en una fecha específica
	 * @param userId ID del usuario
	 * @param date Fecha
	 * @return TimesheetDetailResponseDTO si existe, null en caso contrario
	 */
	TimesheetDetailResponseDTO findByUserIdAndDate(Long userId, LocalDate date);

	/**
	 * Busca timesheets por estado
	 * @param status Estado del timesheet
	 * @return Lista de TimesheetResponseDTO
	 */
	List<TimesheetResponseDTO> findByStatus(TimesheetStatus status);

	/**
	 * Busca timesheets por estado con paginación
	 * @param status Estado del timesheet
	 * @param pageable Configuración de paginación
	 * @return Página de TimesheetResponseDTO
	 */
	Page<TimesheetResponseDTO> findByStatus(TimesheetStatus status, Pageable pageable);

	/**
	 * Obtiene todos los timesheets abiertos (sin check-out)
	 * @return Lista de TimesheetResponseDTO
	 */
	List<TimesheetResponseDTO> findOpenTimesheets();

	/**
	 * Obtiene todos los timesheets cerrados (con check-out)
	 * @return Lista de TimesheetResponseDTO
	 */
	List<TimesheetResponseDTO> findClosedTimesheets();

	/**
	 * Cuenta timesheets por estado
	 * @param status Estado del timesheet
	 * @return Cantidad de timesheets con ese estado
	 */
	Long countByStatus(TimesheetStatus status);

	/**
	 * Busca timesheets de una fecha específica
	 * @param date Fecha
	 * @return Lista de TimesheetResponseDTO
	 */
	List<TimesheetResponseDTO> findByDate(LocalDate date);

	/**
	 * Busca timesheets en un rango de fechas
	 * @param startDate Fecha de inicio
	 * @param endDate Fecha de fin
	 * @return Lista de TimesheetResponseDTO
	 */
	List<TimesheetResponseDTO> findByDateRange(LocalDate startDate, LocalDate endDate);

	/**
	 * Obtiene los timesheets de hoy
	 * @return Lista de TimesheetResponseDTO
	 */
	List<TimesheetResponseDTO> findTodayTimesheets();

	/**
	 * Obtiene los timesheets de esta semana
	 * @return Lista de TimesheetResponseDTO
	 */
	List<TimesheetResponseDTO> findThisWeekTimesheets();

	/**
	 * Obtiene los timesheets de este mes
	 * @return Lista de TimesheetResponseDTO
	 */
	List<TimesheetResponseDTO> findThisMonthTimesheets();

	/**
	 * Encuentra timesheets con anomalías detectadas
	 * @return Lista de TimesheetResponseDTO con estado ANOMALY
	 */
	List<TimesheetResponseDTO> findAnomalies();

	/**
	 * Encuentra timesheets con check-out pendiente (más de X horas sin cerrar)
	 * @param hoursThreshold Horas sin cerrar para considerarse pendiente
	 * @return Lista de TimesheetResponseDTO
	 */
	List<TimesheetResponseDTO> findOverdueCheckouts(int hoursThreshold);

	/**
	 * Encuentra timesheets con horas trabajadas excesivas
	 * @param maxHours Horas máximas permitidas
	 * @return Lista de TimesheetResponseDTO
	 */
	List<TimesheetResponseDTO> findExcessiveWorkHours(long maxHours);

	/**
	 * Encuentra timesheets con tardanzas (más de X minutos de retraso)
	 * @param toleranceMinutes Minutos de tolerancia
	 * @return Lista de TimesheetResponseDTO
	 */
	List<TimesheetResponseDTO> findLateCheckIns(int toleranceMinutes);

	/**
	 * Corrige el estado de un timesheet marcado como anomalía
	 * @param timesheetId ID del timesheet
	 * @param notes Notas explicando la corrección
	 * @return TimesheetDetailResponseDTO corregido
	 */
	TimesheetDetailResponseDTO correctAnomaly(Long timesheetId, String notes);

	/**
	 * Obtiene estadísticas globales de timesheets
	 * @return TimesheetStatisticsDTO con todas las estadísticas
	 */
	TimesheetStatisticsDTO getGlobalStatistics();

	/**
	 * Calcula el total de horas trabajadas por un usuario en un período
	 * @param userId ID del usuario
	 * @param startDate Fecha de inicio
	 * @param endDate Fecha de fin
	 * @return Total de horas trabajadas
	 */
	Long getTotalWorkedHoursByUser(Long userId, LocalDate startDate, LocalDate endDate);

	/**
	 * Calcula el promedio de horas trabajadas en un período
	 * @param startDate Fecha de inicio
	 * @param endDate Fecha de fin
	 * @return Promedio de horas trabajadas
	 */
	Double getAverageWorkedHours(LocalDate startDate, LocalDate endDate);

	/**
	 * Obtiene estadísticas de asistencia por día en un rango de fechas
	 * @param startDate Fecha de inicio
	 * @param endDate Fecha de fin
	 * @return Mapa con estadísticas por día
	 */
	Map<String, Object> getAttendanceStatsByDay(LocalDate startDate, LocalDate endDate);

	/**
	 * Obtiene estadísticas de asistencia por usuario en un rango de fechas
	 * @param startDate Fecha de inicio
	 * @param endDate Fecha de fin
	 * @return Mapa con estadísticas por usuario
	 */
	Map<String, Object> getAttendanceStatsByUser(LocalDate startDate, LocalDate endDate);

	/**
	 * Obtiene estadísticas de asistencia por tipo de turno en un rango de fechas
	 * @param startDate Fecha de inicio
	 * @param endDate Fecha de fin
	 * @return Mapa con estadísticas por tipo de turno
	 */
	Map<String, Object> getAttendanceStatsByShiftType(LocalDate startDate, LocalDate endDate);

	/**
	 * Obtiene los usuarios más puntuales (menor promedio de tardanza)
	 * @param startDate Fecha de inicio
	 * @param endDate Fecha de fin
	 * @param limit Número de usuarios a retornar
	 * @return Lista con los usuarios más puntuales
	 */
	List<Map<String, Object>> getMostPunctualUsers(LocalDate startDate, LocalDate endDate, int limit);

	/**
	 * Obtiene los usuarios con más horas trabajadas
	 * @param startDate Fecha de inicio
	 * @param endDate Fecha de fin
	 * @param limit Número de usuarios a retornar
	 * @return Lista con los usuarios con más horas
	 */
	List<Map<String, Object>> getUsersWithMostHours(LocalDate startDate, LocalDate endDate, int limit);

	/**
	 * Genera un reporte detallado de asistencia de un usuario
	 * @param userId ID del usuario
	 * @param startDate Fecha de inicio
	 * @param endDate Fecha de fin
	 * @return Mapa con el reporte completo
	 */
	Map<String, Object> generateUserAttendanceReport(Long userId, LocalDate startDate, LocalDate endDate);

	/**
	 * Busca timesheets aplicando múltiples filtros opcionales
	 * @param userId Filtro por usuario (opcional)
	 * @param workshiftId Filtro por workshift (opcional)
	 * @param status Filtro por estado (opcional)
	 * @param startDate Fecha inicio del rango (opcional)
	 * @param endDate Fecha fin del rango (opcional)
	 * @param pageable Configuración de paginación
	 * @return Página de TimesheetResponseDTO que cumplen los filtros
	 */
	Page<TimesheetResponseDTO> findWithFilters(Long userId, Long workshiftId, TimesheetStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable);

	/**
	 * Cierra automáticamente timesheets abiertos antiguos (más de X días)
	 * @param daysThreshold Días desde el check-in para cerrar automáticamente
	 * @return Cantidad de timesheets cerrados automáticamente
	 */
	int autoCloseOldTimesheets(int daysThreshold);

	/**
	 * Detecta y marca anomalías en timesheets recientes
	 * @param daysBack Días hacia atrás para analizar
	 * @return Cantidad de anomalías detectadas
	 */
	int detectAnomalies(int daysBack);

	/**
	 * Busca timesheets duplicados para el mismo workshift
	 * @return Lista de IDs de workshifts con duplicados
	 */
	List<Long> findDuplicateTimesheets();

	/**
	 * Archiva timesheets antiguos (más de X meses)
	 * @param monthsThreshold Meses desde la creación para archivar
	 * @return Cantidad de timesheets archivados
	 */
	int archiveOldTimesheets(int monthsThreshold);
}
