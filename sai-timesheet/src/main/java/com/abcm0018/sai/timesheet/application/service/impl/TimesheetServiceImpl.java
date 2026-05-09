package com.abcm0018.sai.timesheet.application.service.impl;

import com.abcm0018.sai.shared.constants.CustomErrorCode;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetDetailResponseDTO;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetStatisticsDTO;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetSummaryDTO;
import com.abcm0018.sai.timesheet.domain.entity.Timesheet;
import com.abcm0018.sai.timesheet.domain.enums.TimesheetStatus;
import com.abcm0018.sai.timesheet.domain.repository.TimesheetRepository;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetRequestDTO;
import com.abcm0018.sai.timesheet.application.dtos.TimesheetResponseDTO;
import com.abcm0018.sai.timesheet.exceptions.TimesheetServiceException;
import com.abcm0018.sai.timesheet.application.mapper.TimesheetMapper;
import com.abcm0018.sai.timesheet.application.service.TimesheetService;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.users.domain.repository.UserRepository;
import com.abcm0018.sai.workshift.domain.entity.Workshift;
import com.abcm0018.sai.workshift.domain.repository.WorkshiftRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
//@CacheConfig(cacheNames = InventariadoCacheConfig.PRODUCT_INFO)
public class TimesheetServiceImpl implements TimesheetService {

	private final UserRepository userRepository;
	private final TimesheetMapper timesheetMapper;
	private final TimesheetRepository timesheetRepository;
	private final WorkshiftRepository workshiftRepository;


	private static final int LATE_TOLERANCE_MINUTES = 30; // Minutos de tolerancia para tardanza
	private static final int OVERDUE_CHECKOUT_HOURS = 12; // Horas sin checkout para considerarse pendiente
	private static final long EXCESSIVE_WORK_HOURS = 8; // Horas máximas de trabajo por día
	private static final int AUTO_CLOSE_DAYS_THRESHOLD = 2; // Días para cerrar automáticamente timesheets abiertos
	private static final int ANOMALY_DETECTION_DAYS = 7; // Días hacia atrás para detectar anomalías
	private static final int ARCHIVE_MONTHS_THRESHOLD = 6; // Meses para archivar timesheets antiguos
	private static final long OVERTIME_THRESHOLD_HOURS = 1; // Hora extra mínima para considerar overtime

	/**
	 * Crea un nuevo timesheet
	 * Invalida cachés relacionados
	 */
	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "timesheets", allEntries = true),
			@CacheEvict(value = "openTimesheets", allEntries = true),
			@CacheEvict(value = "timesheetStats", allEntries = true),
			@CacheEvict(value = "userTimesheets", key = "#requestDTO.workshiftId")
	})
	public TimesheetDetailResponseDTO createTimesheet(TimesheetRequestDTO requestDTO) {
		log.info("Creando nuevo timesheet - Workshift: {}", requestDTO.getWorkshiftId());

		try {
			// Validar que el workshift existe
			Workshift workshift = getWorkshiftById(requestDTO.getWorkshiftId());

			// Validar que no exista ya un timesheet para este workshift
			validateNoExistingTimesheet(workshift);

			// Crear entidad
			Timesheet timesheet = timesheetMapper.toEntity(requestDTO);
			timesheet.setWorkshift(workshift);

			// Determinar estado inicial
			if (requestDTO.getStatus() == null) {
				timesheet.setStatus(determineInitialStatus(timesheet));
			}

			// Guardar
			Timesheet saved = timesheetRepository.save(timesheet);

			log.info("Timesheet creado exitosamente - ID: {}, Usuario: {}", saved.getId(), workshift.getUser().getFullName());
			return timesheetMapper.toDetailResponse(saved);

		} catch (TimesheetServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error inesperado al crear timesheet", e);
			throw new TimesheetServiceException(CustomErrorCode.INTERNAL_SERVER_ERROR, "Error inesperado al crear el timesheet", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheets", key = "#id", unless = "#result == null")
	public TimesheetDetailResponseDTO findById(Long id) {
		log.debug("Buscando timesheet por ID: {}", id);
		return timesheetMapper.toDetailResponse(getTimesheetById(id));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<TimesheetSummaryDTO> findAll(Pageable pageable) {
		log.debug("Listando todos los timesheets - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
		return timesheetRepository.findAll(pageable).map(timesheetMapper::toSummary);
	}

	/**
	 * Actualiza un timesheet existente
	 * Actualiza caché y elimina cachés relacionados
	 */
	@Override
	@Transactional
	@Caching(
			put = @CachePut(value = "timesheets", key = "#id"),
			evict = {
					@CacheEvict(value = "openTimesheets", allEntries = true),
					@CacheEvict(value = "timesheetStats", allEntries = true),
					@CacheEvict(value = "userTimesheets", allEntries = true)
			}
	)
	public TimesheetDetailResponseDTO updateTimesheet(Long id, TimesheetRequestDTO requestDTO) {
		log.info("Actualizando timesheet {}", id);

		Timesheet existing = getTimesheetById(id);

		// Validar fechas si se actualizan
		if (requestDTO.getCheckInAt() != null || requestDTO.getCheckOutAt() != null) {
			LocalDateTime checkIn = requestDTO.getCheckInAt() != null ? requestDTO.getCheckInAt() : existing.getCheckInAt();
			LocalDateTime checkOut = requestDTO.getCheckOutAt() != null ? requestDTO.getCheckOutAt() : existing.getCheckOutAt();
		}

		// Actualizar campos
		timesheetMapper.updateEntityFromRequest(requestDTO, existing);

		// Recalcular estado si cambió check-out
		if (requestDTO.getCheckOutAt() != null && existing.getCheckOutAt() == null) {
			existing.setStatus(TimesheetStatus.CLOSED);
		}

		Timesheet updated = timesheetRepository.save(existing);

		log.info("Timesheet {} actualizado exitosamente", id);

		return timesheetMapper.toDetailResponse(updated);
	}

	/**
	 * Elimina un timesheet
	 * Invalida todos los cachés relacionados
	 */
	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "timesheets", key = "#id"),
			@CacheEvict(value = "openTimesheets", allEntries = true),
			@CacheEvict(value = "timesheetStats", allEntries = true),
			@CacheEvict(value = "userTimesheets", allEntries = true)
	})
	public void deleteTimesheet(Long id) {
		log.info("Eliminando timesheet {}", id);

		Timesheet timesheet = getTimesheetById(id);
		timesheetRepository.delete(timesheet);

		log.info("Timesheet {} eliminado exitosamente", id);
	}

	/**
	 * Registra la entrada (check-in) de un usuario
	 * Crea automáticamente un timesheet para el workshift del día
	 */
	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "timesheets", allEntries = true),
			@CacheEvict(value = "openTimesheets", allEntries = true),
			@CacheEvict(value = "userTimesheets", key = "#userId")
	})
	public TimesheetDetailResponseDTO checkIn(Long userId) {
		return checkInWithTime(userId, LocalDateTime.now());
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "timesheets", allEntries = true),
			@CacheEvict(value = "openTimesheets", allEntries = true),
			@CacheEvict(value = "userTimesheets", key = "#userId")
	})
	public TimesheetDetailResponseDTO checkInWithTime(Long userId, LocalDateTime checkInTime) {
		log.info("Registrando check-in - Usuario: {}, Hora: {}", userId, checkInTime);

		try {
			// Validar que el usuario existe
			User user = getUser(userId);

			// Verificar que no tenga un timesheet abierto
			if (hasOpenTimesheetWithoutCache(userId)) {
				throw new TimesheetServiceException(CustomErrorCode.CONFLICT, "El usuario ya tiene un timesheet abierto. Debe hacer check-out primero.", HttpStatus.CONFLICT);
			}

			// Obtener el workshift del día actual para este usuario
			Workshift todayWorkshift = getTodayWorkshiftForUser(user);

			// Validar que no exista ya un timesheet para este workshift
			validateNoExistingTimesheet(todayWorkshift);

			// Crear el timesheet
			Timesheet timesheet = Timesheet.builder().workshift(todayWorkshift).checkInAt(checkInTime).status(TimesheetStatus.OPEN).build();

			// Determinar si llegó tarde
			if (timesheet.isLate(LATE_TOLERANCE_MINUTES)) {
				timesheet.setStatus(TimesheetStatus.ANOMALY);
				timesheet.setNotes(String.format("Tardanza de %d minutos", timesheet.getLateMinutes()));
				log.warn("Check-in con tardanza - Usuario: {}, Minutos: {}", userId, timesheet.getLateMinutes());

				// TODO: Guardar auditoria de tardanza
			}

			Timesheet saved = timesheetRepository.save(timesheet);

			log.info("Check-in registrado exitosamente - Timesheet ID: {}, Usuario: {}", saved.getId(), user.getFullName());

			return timesheetMapper.toDetailResponse(saved);

		} catch (TimesheetServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error al registrar check-in para usuario {}", userId, e);
			throw new TimesheetServiceException(CustomErrorCode.INTERNAL_SERVER_ERROR, "Error al registrar el check-in", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Registra la salida (check-out) del usuario
	 */
	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "timesheets", allEntries = true),
			@CacheEvict(value = "openTimesheets", allEntries = true),
			@CacheEvict(value = "userTimesheets", key = "#userId"),
			@CacheEvict(value = "timesheetStats", allEntries = true)
	})
	public TimesheetDetailResponseDTO checkOut(Long userId) {
		return checkOutWithTime(userId, LocalDateTime.now());
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "timesheets", allEntries = true),
			@CacheEvict(value = "openTimesheets", allEntries = true),
			@CacheEvict(value = "userTimesheets", key = "#userId"),
			@CacheEvict(value = "timesheetStats", allEntries = true)
	})
	public TimesheetDetailResponseDTO checkOutWithTime(Long userId, LocalDateTime checkOutTime) {
		log.info("Registrando check-out - Usuario: {}, Hora: {}", userId, checkOutTime);

		try {
			// Buscar el último timesheet abierto del usuario
			Timesheet openTimesheet = timesheetRepository.findLastOpenTimesheetByUser(userId)
					.orElseThrow(() -> new TimesheetServiceException(CustomErrorCode.NOT_FOUND, "No se encontró un timesheet abierto para el usuario", HttpStatus.NOT_FOUND));

			// Validar que el check-out no sea anterior al check-in
			if (checkOutTime.isBefore(openTimesheet.getCheckInAt())) {
				throw new TimesheetServiceException(CustomErrorCode.BAD_REQUEST, "La hora de salida no puede ser anterior a la hora de entrada", HttpStatus.BAD_REQUEST);
			}

			// Registrar check-out
			openTimesheet.checkOut(checkOutTime);

			// Detectar anomalías
			detectAndMarkAnomalies(openTimesheet);

			Timesheet saved = timesheetRepository.save(openTimesheet);

			log.info("Check-out registrado exitosamente - Timesheet ID: {}, Horas trabajadas: {}", saved.getId(), saved.getWorkedDuration());

			return timesheetMapper.toDetailResponse(saved);

		} catch (TimesheetServiceException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error al registrar check-out para usuario {}", userId, e);
			throw new TimesheetServiceException(CustomErrorCode.INTERNAL_SERVER_ERROR, "Error al registrar el check-out", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "openTimesheets", key = "#userId")
	public boolean hasOpenTimesheet(Long userId) {
		return timesheetRepository.hasOpenTimesheet(userId);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "openTimesheets", key = "'user-' + #userId", unless = "#result == null")
	public TimesheetDetailResponseDTO getOpenTimesheet(Long userId) {
		return timesheetRepository.findLastOpenTimesheetByUser(userId)
				.map(timesheetMapper::toDetailResponse)
				.orElse(null);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheets", key = "'workshift-' + #workshiftId", unless = "#result == null")
	public TimesheetDetailResponseDTO findByWorkshiftId(Long workshiftId) {
		return timesheetRepository.findByWorkshift(getWorkshiftById(workshiftId))
				.map(timesheetMapper::toDetailResponse)
				.orElse(null);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TimesheetResponseDTO> findByWorkshift(Long workshiftId) {
		return timesheetMapper.toResponseList(timesheetRepository.findByWorkshiftId(workshiftId));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "userTimesheets", key = "#userId")
	public List<TimesheetResponseDTO> findByUserId(Long userId) {
		return timesheetMapper.toResponseList(timesheetRepository.findByUserId(userId));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<TimesheetResponseDTO> findByUserId(Long userId, Pageable pageable) {
		return timesheetRepository.findByUserId(userId, pageable).map(timesheetMapper::toResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TimesheetResponseDTO> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
		validateDateRange(startDate, endDate);

		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		return timesheetMapper.toResponseList(
				timesheetRepository.findByUserIdAndDateRange(userId, startDateTime, endDateTime)
		);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheets", key = "'user-' + #userId + '-date-' + #date", unless = "#result == null")
	public TimesheetDetailResponseDTO findByUserIdAndDate(Long userId, LocalDate date) {
		return timesheetRepository.findByUserIdAndDate(userId, date)
				.map(timesheetMapper::toDetailResponse)
				.orElse(null);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TimesheetResponseDTO> findByStatus(TimesheetStatus status) {
		return timesheetMapper.toResponseList(timesheetRepository.findByStatus(status));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<TimesheetResponseDTO> findByStatus(TimesheetStatus status, Pageable pageable) {
		return timesheetRepository.findByStatus(status, pageable).map(timesheetMapper::toResponse);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "openTimesheets", key = "'all'")
	public List<TimesheetResponseDTO> findOpenTimesheets() {
		return timesheetMapper.toResponseList(timesheetRepository.findOpenTimesheets());
	}

	@Override
	@Transactional(readOnly = true)
	public List<TimesheetResponseDTO> findClosedTimesheets() {
		return timesheetMapper.toResponseList(timesheetRepository.findClosedTimesheets());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheetStats", key = "'count-' + #status")
	public Long countByStatus(TimesheetStatus status) {
		return timesheetRepository.countByStatus(status);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheets", key = "'date-' + #date")
	public List<TimesheetResponseDTO> findByDate(LocalDate date) {
		return timesheetMapper.toResponseList(timesheetRepository.findByDate(date));
	}

	@Override
	@Transactional(readOnly = true)
	public List<TimesheetResponseDTO> findByDateRange(LocalDate startDate, LocalDate endDate) {
		validateDateRange(startDate, endDate);

		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		return timesheetMapper.toResponseList(timesheetRepository.findByDateRange(startDateTime, endDateTime));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheets", key = "'today'")
	public List<TimesheetResponseDTO> findTodayTimesheets() {
		return timesheetMapper.toResponseList(timesheetRepository.findTodayTimesheets());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheets", key = "'this-week'")
	public List<TimesheetResponseDTO> findThisWeekTimesheets() {
		return timesheetMapper.toResponseList(timesheetRepository.findThisWeekTimesheets());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheets", key = "'this-month'")
	public List<TimesheetResponseDTO> findThisMonthTimesheets() {
		return timesheetMapper.toResponseList(timesheetRepository.findThisMonthTimesheets());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheets", key = "'anomalies'")
	public List<TimesheetResponseDTO> findAnomalies() {
		return timesheetMapper.toResponseList(timesheetRepository.findAnomalies());
	}

	@Override
	@Transactional(readOnly = true)
	public List<TimesheetResponseDTO> findOverdueCheckouts(int hoursThreshold) {
		LocalDateTime threshold = LocalDateTime.now().minusHours(hoursThreshold);
		return timesheetMapper.toResponseList(timesheetRepository.findOverdueCheckouts(threshold));
	}

	@Override
	@Transactional(readOnly = true)
	public List<TimesheetResponseDTO> findExcessiveWorkHours(long maxHours) {
		return timesheetMapper.toResponseList(timesheetRepository.findExcessiveWorkHours(maxHours));
	}

	@Override
	@Transactional(readOnly = true)
	public List<TimesheetResponseDTO> findLateCheckIns(int toleranceMinutes) {
		List<Timesheet> allTimesheets = timesheetRepository.findClosedTimesheets();

		return allTimesheets.stream()
				.filter(t -> t.isLate(toleranceMinutes))
				.map(timesheetMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "timesheets", key = "#timesheetId"),
			@CacheEvict(value = "timesheetStats", allEntries = true)
	})
	public TimesheetDetailResponseDTO correctAnomaly(Long timesheetId, String notes) {
		log.info("Corrigiendo anomalía en timesheet {}", timesheetId);

		Timesheet timesheet = getTimesheetById(timesheetId);

		if (timesheet.getStatus() != TimesheetStatus.ANOMALY) {
			throw new TimesheetServiceException(CustomErrorCode.BAD_REQUEST, "Solo se pueden corregir timesheets marcados como ANOMALY", HttpStatus.BAD_REQUEST);
		}

		timesheet.setStatus(TimesheetStatus.CORRECTED);
		timesheet.setNotes(notes);

		Timesheet corrected = timesheetRepository.save(timesheet);

		log.info("Anomalía corregida en timesheet {}", timesheetId);

		return timesheetMapper.toDetailResponse(corrected);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheetStats", key = "'global'")
	public TimesheetStatisticsDTO getGlobalStatistics() {
		log.debug("Obteniendo estadísticas globales de timesheets");

		// Contadores básicos
		Long totalTimesheets = timesheetRepository.count();
		Long openTimesheets = countByStatus(TimesheetStatus.OPEN);
		Long closedTimesheets = countByStatus(TimesheetStatus.CLOSED);
		Long anomalies = countByStatus(TimesheetStatus.ANOMALY);

		// Promedio de horas trabajadas (último mes)
		LocalDate startDate = LocalDate.now().minusMonths(1);
		LocalDate endDate = LocalDate.now();
		Double averageWorkedHours = getAverageWorkedHours(startDate, endDate);

		// Total de horas trabajadas (último mes)
		Long totalWorkedHours = calculateTotalWorkedHoursInPeriod(startDate, endDate);

		// Estadísticas por estado
		Map<String, Long> timesheetsByStatus = new HashMap<>();
		for (TimesheetStatus status : TimesheetStatus.values()) {
			timesheetsByStatus.put(status.name(), countByStatus(status));
		}

		// Estadísticas por tipo de turno (último mes)
		Map<String, Long> timesheetsByShiftType = getTimesheetCountByShiftType(startDate, endDate);

		// Estadísticas de puntualidad
		Map<String, Object> punctualityStats = getPunctualityStatistics(startDate, endDate);

		// Estadísticas de horas trabajadas
		Map<String, Object> workHoursStats = getWorkHoursStatistics(startDate, endDate);

		return TimesheetStatisticsDTO.builder()
				.totalTimesheets(totalTimesheets)
				.openTimesheets(openTimesheets)
				.closedTimesheets(closedTimesheets)
				.anomalies(anomalies)
				.averageWorkedHours(averageWorkedHours != null ? averageWorkedHours : 0.0)
				.totalWorkedHours(totalWorkedHours)
				.timesheetsByStatus(timesheetsByStatus)
				.timesheetsByShiftType(timesheetsByShiftType)
				.punctualityStats(punctualityStats)
				.workHoursStats(workHoursStats)
				.build();
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheetStats", key = "'user-' + #userId + '-hours-' + #startDate + '-' + #endDate")
	public Long getTotalWorkedHoursByUser(Long userId, LocalDate startDate, LocalDate endDate) {
		validateDateRange(startDate, endDate);

		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		Long total = timesheetRepository.getTotalWorkedHoursByUser(userId, startDateTime, endDateTime);
		return total != null ? total : 0L;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheetStats", key = "'avg-hours-' + #startDate + '-' + #endDate")
	public Double getAverageWorkedHours(LocalDate startDate, LocalDate endDate) {
		validateDateRange(startDate, endDate);

		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		Double average = timesheetRepository.getAverageWorkedHours(startDateTime, endDateTime);
		return average != null ? average : 0.0;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheetStats", key = "'attendance-by-day-' + #startDate + '-' + #endDate")
	public Map<String, Object> getAttendanceStatsByDay(LocalDate startDate, LocalDate endDate) {
		log.debug("Obteniendo estadísticas de asistencia por día del {} al {}", startDate, endDate);

		validateDateRange(startDate, endDate);

		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		List<Object[]> dailyCounts = timesheetRepository.countTimesheetsByDay(startDateTime, endDateTime);

		List<Map<String, Object>> dailyStats = new ArrayList<>();
		long totalTimesheets = 0;

		for (Object[] result : dailyCounts) {
			Map<String, Object> dayStat = new HashMap<>();
			LocalDate date = (LocalDate) result[0];
			Long count = (Long) result[1];

			dayStat.put("date", date);
			dayStat.put("count", count);
			dayStat.put("dayOfWeek", date.getDayOfWeek().name());

			dailyStats.add(dayStat);
			totalTimesheets += count;
		}

		// Calcular promedios
		long daysCount = dailyStats.size();
		double averagePerDay = daysCount > 0 ? (double) totalTimesheets / daysCount : 0.0;

		Map<String, Object> stats = new HashMap<>();
		stats.put("startDate", startDate);
		stats.put("endDate", endDate);
		stats.put("totalTimesheets", totalTimesheets);
		stats.put("totalDays", daysCount);
		stats.put("averagePerDay", Math.round(averagePerDay * 100.0) / 100.0);
		stats.put("dailyStats", dailyStats);

		return stats;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheetStats", key = "'attendance-by-user-' + #startDate + '-' + #endDate")
	public Map<String, Object> getAttendanceStatsByUser(LocalDate startDate, LocalDate endDate) {
		log.debug("Obteniendo estadísticas de asistencia por usuario del {} al {}", startDate, endDate);

		validateDateRange(startDate, endDate);

		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		List<Object[]> userCounts = timesheetRepository.countTimesheetsByUser(startDateTime, endDateTime);

		List<Map<String, Object>> userStats = new ArrayList<>();
		long totalTimesheets = 0;

		for (Object[] result : userCounts) {
			User user = (User) result[0];
			Long count = (Long) result[1];

			Map<String, Object> stat = new HashMap<>();
			stat.put("userId", user.getId());
			stat.put("employeeNumber", user.getEmployeeNumber());
			stat.put("fullName", user.getFullName());
			stat.put("count", count);

			// Calcular total de horas trabajadas
			Long totalHours = getTotalWorkedHoursByUser(user.getId(), startDate, endDate);
			stat.put("totalHours", totalHours);

			userStats.add(stat);
			totalTimesheets += count;
		}

		// Ordenar por cantidad descendente
		userStats.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));

		Map<String, Object> stats = new HashMap<>();
		stats.put("startDate", startDate);
		stats.put("endDate", endDate);
		stats.put("totalTimesheets", totalTimesheets);
		stats.put("totalUsers", userStats.size());
		stats.put("userStats", userStats);

		return stats;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheetStats", key = "'attendance-by-shift-' + #startDate + '-' + #endDate")
	public Map<String, Object> getAttendanceStatsByShiftType(LocalDate startDate, LocalDate endDate) {
		log.debug("Obteniendo estadísticas de asistencia por tipo de turno del {} al {}", startDate, endDate);

		validateDateRange(startDate, endDate);

		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		List<Object[]> shiftCounts = timesheetRepository.countTimesheetsByShiftType(startDateTime, endDateTime);

		List<Map<String, Object>> shiftStats = new ArrayList<>();
		long totalTimesheets = 0;

		for (Object[] result : shiftCounts) {
			String shiftType = result[0].toString();
			Long count = (Long) result[1];

			Map<String, Object> stat = new HashMap<>();
			stat.put("shiftType", shiftType);
			stat.put("count", count);

			shiftStats.add(stat);
			totalTimesheets += count;
		}

		Map<String, Object> stats = new HashMap<>();
		stats.put("startDate", startDate);
		stats.put("endDate", endDate);
		stats.put("totalTimesheets", totalTimesheets);
		stats.put("shiftStats", shiftStats);

		return stats;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getMostPunctualUsers(LocalDate startDate, LocalDate endDate, int limit) {
		validateDateRange(startDate, endDate);

		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		List<User> punctualUsers = timesheetRepository.findMostPunctualUsers(startDateTime, endDateTime, PageRequest.of(0, limit));

		return punctualUsers.stream()
				.map(user -> {
					Map<String, Object> stat = new HashMap<>();
					stat.put("userId", user.getId());
					stat.put("employeeNumber", user.getEmployeeNumber());
					stat.put("fullName", user.getFullName());

					// Calcular promedio de tardanza
					List<Timesheet> userTimesheets = timesheetRepository.findByUserIdAndDateRange(
							user.getId(), startDateTime, endDateTime
					);

					double avgLateMinutes = userTimesheets.stream()
							.mapToLong(Timesheet::getLateMinutes)
							.average()
							.orElse(0.0);

					stat.put("averageLateMinutes", Math.round(avgLateMinutes * 100.0) / 100.0);

					return stat;
				}).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getUsersWithMostHours(LocalDate startDate, LocalDate endDate, int limit) {
		validateDateRange(startDate, endDate);

		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		List<Object[]> usersWithHours = timesheetRepository.findUsersWithMostHours(
				startDateTime, endDateTime, PageRequest.of(0, limit)
		);

		return usersWithHours.stream()
				.map(result -> {
					User user = (User) result[0];
					Long totalHours = (Long) result[1];

					Map<String, Object> stat = new HashMap<>();
					stat.put("userId", user.getId());
					stat.put("employeeNumber", user.getEmployeeNumber());
					stat.put("fullName", user.getFullName());
					stat.put("totalHours", totalHours);

					return stat;
				}).toList();
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "timesheetStats", key = "'user-report-' + #userId + '-' + #startDate + '-' + #endDate")
	public Map<String, Object> generateUserAttendanceReport(Long userId, LocalDate startDate, LocalDate endDate) {
		log.info("Generando reporte de asistencia - Usuario: {}, Período: {} a {}",
				userId, startDate, endDate);

		validateDateRange(startDate, endDate);

		User user = getUser(userId);

		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		// Obtener todos los timesheets del usuario en el período
		List<Timesheet> timesheets = timesheetRepository.findByUserIdAndDateRange(
				userId, startDateTime, endDateTime
		);

		// Calcular estadísticas
		long totalDays = timesheets.size();
		long daysLate = timesheets.stream().filter(t -> t.isLate(LATE_TOLERANCE_MINUTES)).count();
		long daysWithOvertime = timesheets.stream().filter(t -> t.hasOvertime(OVERTIME_THRESHOLD_HOURS)).count();

		Long totalHours = getTotalWorkedHoursByUser(userId, startDate, endDate);

		double avgHoursPerDay = totalDays > 0 ? (double) totalHours / totalDays : 0.0;

		double avgLateMinutes = timesheets.stream()
				.mapToLong(Timesheet::getLateMinutes)
				.average()
				.orElse(0.0);

		// Construir reporte
		Map<String, Object> report = new HashMap<>();

		// Información del usuario
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put("id", user.getId());
		userInfo.put("employeeNumber", user.getEmployeeNumber());
		userInfo.put("fullName", user.getFullName());
		userInfo.put("email", user.getEmail());
		userInfo.put("jobPosition", user.getJobPosition());
		report.put("user", userInfo);

		// Período del reporte
		report.put("startDate", startDate);
		report.put("endDate", endDate);
		report.put("totalDays", ChronoUnit.DAYS.between(startDate, endDate) + 1);

		// Estadísticas generales
		Map<String, Object> stats = new HashMap<>();
		stats.put("totalTimesheets", totalDays);
		stats.put("totalWorkedHours", totalHours);
		stats.put("averageHoursPerDay", Math.round(avgHoursPerDay * 100.0) / 100.0);
		stats.put("daysLate", daysLate);
		stats.put("averageLateMinutes", Math.round(avgLateMinutes * 100.0) / 100.0);
		stats.put("daysWithOvertime", daysWithOvertime);
		report.put("statistics", stats);

		// Detalle de timesheets
		List<TimesheetResponseDTO> timesheetDetails = timesheetMapper.toResponseList(timesheets);
		report.put("timesheets", timesheetDetails);

		// Indicadores de desempeño
		Map<String, Object> performance = new HashMap<>();
		double punctualityRate = totalDays > 0 ? ((double) (totalDays - daysLate) / totalDays) * 100 : 100.0;
		performance.put("punctualityRate", Math.round(punctualityRate * 100.0) / 100.0);
		performance.put("attendanceRate", 100.0); // TODO: Calcular basado en días laborables
		report.put("performance", performance);

		log.info("Reporte de asistencia generado - Usuario: {}, Total días: {}, Horas: {}",
				userId, totalDays, totalHours);

		return report;
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(
			value = "timesheetFilters",
			key = "T(java.util.Objects).hash(#userId, #workshiftId, #status, #startDate, #endDate, " +
					"#pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString())", unless = "#result == null || #result.isEmpty()"
	)
	public Page<TimesheetResponseDTO> findWithFilters(Long userId, Long workshiftId, TimesheetStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
		log.debug("Buscando timesheets con filtros - Usuario: {}, Workshift: {}, Estado: {}, FechaInicio: {}, FechaFin: {}, Página: {}, Tamaño: {}",
				userId, workshiftId, status, startDate, endDate, pageable.getPageNumber(), pageable.getPageSize());

		if (startDate != null && endDate != null) {
			validateDateRange(startDate, endDate);
		}

		LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
		LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

		Page<Timesheet> timesheets = timesheetRepository.findWithFilters(userId, workshiftId, status, startDateTime, endDateTime, pageable);

		log.info("Búsqueda con filtros completada - Encontrados: {} timesheets, Página: {}/{}, Total: {}",
				timesheets.getNumberOfElements(), timesheets.getNumber() + 1, timesheets.getTotalPages(), timesheets.getTotalElements());

		return timesheets.map(timesheetMapper::toResponse);
	}

	@Override
	@Transactional
	@CacheEvict(value = {"timesheets", "openTimesheets", "timesheetStats"}, allEntries = true)
	public int autoCloseOldTimesheets(int daysThreshold) {
		log.info("Iniciando cierre automático de timesheets antiguos - Umbral: {} días", daysThreshold);

		LocalDateTime threshold = LocalDateTime.now().minusDays(daysThreshold);
		List<Timesheet> overdueTimesheets = timesheetRepository.findOverdueCheckouts(threshold);

		int closedCount = 0;
		for (Timesheet timesheet : overdueTimesheets) {
			try {
				// Cerrar con la hora esperada del turno
				LocalDateTime expectedCheckOut = LocalDateTime.of(
						timesheet.getWorkshift().getDate(),
						timesheet.getWorkshift().getShift().getEndTime()
				);

				if (timesheet.getWorkshift().getShift().crossesMidnight()) {
					expectedCheckOut = expectedCheckOut.plusDays(1);
				}

				timesheet.checkOut(expectedCheckOut);
				timesheet.setStatus(TimesheetStatus.ANOMALY);
				timesheet.setNotes("Cerrado automáticamente por el sistema - Sin check-out registrado");

				timesheetRepository.save(timesheet);
				closedCount++;

				log.debug("Timesheet {} cerrado automáticamente", timesheet.getId());

			} catch (Exception e) {
				log.error("Error al cerrar automáticamente timesheet {}", timesheet.getId(), e);
			}
		}

		log.info("Cierre automático completado - {} timesheets cerrados", closedCount);

		return closedCount;
	}

	@Override
	@Transactional
	@CacheEvict(value = {"timesheets", "timesheetStats"}, allEntries = true)
	public int detectAnomalies(int daysBack) {
		log.info("Detectando anomalías en timesheets - Últimos {} días", daysBack);

		LocalDate startDate = LocalDate.now().minusDays(daysBack);
		LocalDate endDate = LocalDate.now();

		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		List<Timesheet> recentTimesheets = timesheetRepository.findByDateRange(startDateTime, endDateTime);

		int anomaliesDetected = 0;

		for (Timesheet timesheet : recentTimesheets) {
			if (timesheet.getStatus() == TimesheetStatus.ANOMALY || timesheet.getStatus() == TimesheetStatus.CORRECTED) {
				continue; // Ya procesado
			}

			boolean hasAnomaly = detectAndMarkAnomalies(timesheet);
			if (hasAnomaly) {
				timesheetRepository.save(timesheet);
				anomaliesDetected++;
			}
		}

		log.info("Detección de anomalías completada - {} anomalías detectadas", anomaliesDetected);

		return anomaliesDetected;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> findDuplicateTimesheets() {
		log.debug("Buscando timesheets duplicados");

		List<Object[]> duplicates = timesheetRepository.findDuplicateTimesheets();

		return duplicates.stream().map(result -> (Long) result[0]).toList();
	}

	/**
	 * TODO: Convertir este método en un CRON
	 */
	@Override
	@Transactional
	@CacheEvict(value = {"timesheets", "timesheetStats"}, allEntries = true)
	public int archiveOldTimesheets(int monthsThreshold) {
		log.info("Archivando timesheets antiguos - Umbral: {} meses", monthsThreshold);

		LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(monthsThreshold);
		List<Timesheet> oldTimesheets = timesheetRepository.findOldTimesheetsForArchiving(cutoffDate);

		// TODO: Implementar lógica de archivado (mover a tabla de archivo, exportar, etc.)

		int archivedCount = oldTimesheets.size();

		log.info("Archivado completado - {} timesheets procesados", archivedCount);

		return archivedCount;
	}

	/**
	 * Obtiene una entidad Timesheet por ID.
	 * Método interno para evitar conversión a DTO
	 */
	private Timesheet getTimesheetById(Long id) {
		return timesheetRepository.findById(id)
				.orElseThrow(() -> new TimesheetServiceException(CustomErrorCode.NOT_FOUND, "Timesheet no encontrado con ID: " + id, HttpStatus.NOT_FOUND));
	}

	/**
	 * Obtiene estadísticas de puntualidad
	 */
	private Map<String, Object> getPunctualityStatistics(LocalDate startDate, LocalDate endDate) {
		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		List<Timesheet> timesheets = timesheetRepository.findByDateRange(startDateTime, endDateTime);

		long totalTimesheets = timesheets.size();
		long lateTimesheets = timesheets.stream().filter(t -> t.isLate(LATE_TOLERANCE_MINUTES)).count();

		double punctualityRate = totalTimesheets > 0 ?
				((double) (totalTimesheets - lateTimesheets) / totalTimesheets) * 100 : 100.0;

		double avgLateMinutes = timesheets.stream()
				.mapToLong(Timesheet::getLateMinutes)
				.average()
				.orElse(0.0);

		Map<String, Object> stats = new HashMap<>();
		stats.put("totalTimesheets", totalTimesheets);
		stats.put("lateTimesheets", lateTimesheets);
		stats.put("punctualityRate", Math.round(punctualityRate * 100.0) / 100.0);
		stats.put("averageLateMinutes", Math.round(avgLateMinutes * 100.0) / 100.0);

		return stats;
	}

	/**
	 * Obtiene estadísticas de horas trabajadas
	 */
	private Map<String, Object> getWorkHoursStatistics(LocalDate startDate, LocalDate endDate) {
		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		List<Timesheet> timesheets = timesheetRepository.findByDateRange(startDateTime, endDateTime)
				.stream()
				.filter(t -> t.getWorkedHours() != null)
				.toList();

		long totalHours = timesheets.stream()
				.mapToLong(Timesheet::getWorkedHours)
				.sum();

		double avgHours = timesheets.stream()
				.mapToLong(Timesheet::getWorkedHours)
				.average()
				.orElse(0.0);

		long overtimeTimesheets = timesheets.stream()
				.filter(t -> t.hasOvertime(OVERTIME_THRESHOLD_HOURS))
				.count();

		Map<String, Object> stats = new HashMap<>();
		stats.put("totalHours", totalHours);
		stats.put("averageHours", Math.round(avgHours * 100.0) / 100.0);
		stats.put("overtimeTimesheets", overtimeTimesheets);

		return stats;
	}

	/**
	 * Obtiene el conteo de timesheets por tipo de turno
	 */
	private Map<String, Long> getTimesheetCountByShiftType(LocalDate startDate, LocalDate endDate) {
		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		List<Object[]> shiftCounts = timesheetRepository.countTimesheetsByShiftType(startDateTime, endDateTime);

		Map<String, Long> result = new HashMap<>();
		for (Object[] row : shiftCounts) {
			String shiftType = row[0].toString();
			Long count = (Long) row[1];
			result.put(shiftType, count);
		}

		return result;
	}

	/**
	 * Calcula el total de horas trabajadas en un período
	 */
	private Long calculateTotalWorkedHoursInPeriod(LocalDate startDate, LocalDate endDate) {
		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		List<Timesheet> timesheets = timesheetRepository.findByDateRange(startDateTime, endDateTime);

		return timesheets.stream()
				.filter(t -> t.getWorkedHours() != null)
				.mapToLong(Timesheet::getWorkedHours)
				.sum();
	}

	/**
	 * Detecta y marca anomalías en un timesheet
	 */
	private boolean detectAndMarkAnomalies(Timesheet timesheet) {
		boolean hasAnomaly = false;
		StringBuilder notes = new StringBuilder();

		// Detectar tardanza
		if (timesheet.isLate(LATE_TOLERANCE_MINUTES)) {
			hasAnomaly = true;
			notes.append(String.format("Tardanza de %d minutos. ", timesheet.getLateMinutes()));
		}

		// Detectar horas excesivas
		if (timesheet.getCheckOutAt() != null && timesheet.getWorkedHours() != null && timesheet.getWorkedHours() > EXCESSIVE_WORK_HOURS) {
			hasAnomaly = true;
			notes.append(String.format("Horas excesivas trabajadas: %d horas. ", timesheet.getWorkedHours()));
		}

		// Detectar salida anticipada
		if (timesheet.isEarlyExit(LATE_TOLERANCE_MINUTES)) {
			hasAnomaly = true;
			notes.append("Salida anticipada. ");
		}

		if (hasAnomaly) {
			timesheet.setStatus(TimesheetStatus.ANOMALY);

			// Agregar notas solo si no existían previamente
			if (timesheet.getNotes() == null || timesheet.getNotes().isEmpty()) {
				timesheet.setNotes(notes.toString().trim());
			}
		}

		return hasAnomaly;
	}

	/**
	 * Obtiene el usuario a partir del identificador
	 */
	private User getUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new TimesheetServiceException(CustomErrorCode.NOT_FOUND, "El usuario con ID " + userId + " no existe", HttpStatus.NOT_FOUND));
	}

	/**
	 * Devuelve el workshift a partir de su Identificador
	 */
	private Workshift getWorkshiftById(Long workshiftId) {
		return workshiftRepository.findById(workshiftId)
				.orElseThrow(() -> new TimesheetServiceException(CustomErrorCode.NOT_FOUND, "Turno no encontrado con ID: " + workshiftId, HttpStatus.NOT_FOUND));
	}

	/**
	 * Valida un rango de fechas
	 */
	private void validateDateRange(LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null) {
			throw new TimesheetServiceException(CustomErrorCode.BAD_REQUEST, "Las fechas de inicio y fin son obligatorias", HttpStatus.BAD_REQUEST);
		}

		if (startDate.isAfter(endDate)) {
			String msg = String.format("La fecha de inicio (%s) no puede ser posterior a la de fin (%s)", startDate, endDate);
			throw new TimesheetServiceException(CustomErrorCode.BAD_REQUEST, msg, HttpStatus.BAD_REQUEST);
		}

		long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);

		if (daysBetween > 365) {
			log.warn("Rango de fechas muy amplio: {} días", daysBetween);
		}
	}

	/**
	 * Obtiene el workshift del día actual para un usuario
	 */
	private Workshift getTodayWorkshiftForUser(User user) {
		LocalDate today = LocalDate.now();

		// Buscar el workshift del usuario para hoy
		Optional<Workshift> todayWorkshift = workshiftRepository.findTodayWorkshiftByUser(user, today);

		if (todayWorkshift.isEmpty()) {
			String message = String.format("No se encontró un workshift asignado para el usuario %s en la fecha %s", user.getFullName(), today);
			throw new TimesheetServiceException(CustomErrorCode.NOT_FOUND, message, HttpStatus.NOT_FOUND);
		}

		return todayWorkshift.get();
	}

	/**
	 * Determina el estado inicial de un timesheet
	 */
	private TimesheetStatus determineInitialStatus(Timesheet timesheet) {
		if (timesheet.getCheckOutAt() != null) {
			// Si ya tiene check-out, verificar anomalías
			if (timesheet.isLate(LATE_TOLERANCE_MINUTES) || timesheet.hasOvertime(EXCESSIVE_WORK_HOURS)) {
				return TimesheetStatus.ANOMALY;
			}
			return TimesheetStatus.CLOSED;
		}

		// Si solo tiene check-in
		if (timesheet.isLate(LATE_TOLERANCE_MINUTES)) {
			return TimesheetStatus.ANOMALY;
		}

		return TimesheetStatus.OPEN;
	}

	/**
	 * Valida que no exista ya un timesheet para un workshift
	 */
	private void validateNoExistingTimesheet(Workshift workshift) {
		if (timesheetRepository.existsByWorkshift(workshift)) {
			throw new TimesheetServiceException(CustomErrorCode.CONFLICT, "Ya existe un timesheet para este workshift", HttpStatus.CONFLICT);
		}
	}

	private boolean hasOpenTimesheetWithoutCache(Long userId) {
		return timesheetRepository.hasOpenTimesheet(userId);
	}
}
