package com.abcm0018.sai.workshift.application.service.impl;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abcm0018.sai.shared.constants.CustomErrorCode;
import com.abcm0018.sai.shift.domain.entity.Shift;
import com.abcm0018.sai.shift.domain.enums.ShiftType;
import com.abcm0018.sai.shift.domain.repository.ShiftRepository;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.users.domain.enums.Role;
import com.abcm0018.sai.users.domain.repository.UserRepository;
import com.abcm0018.sai.workshift.application.dtos.CreateWorkshiftRequestDTO;
import com.abcm0018.sai.workshift.application.dtos.UpdateWorkshiftRequestDTO;
import com.abcm0018.sai.workshift.application.dtos.WeeklyScheduleResponseDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftConflictDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftDetailResponseDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftFilterDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftResponseDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftStatisticsDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftSummaryDTO;
import com.abcm0018.sai.workshift.application.mapper.WorkshiftMapper;
import com.abcm0018.sai.workshift.domain.entity.Workshift;
import com.abcm0018.sai.workshift.domain.repository.WorkshiftRepository;
import com.abcm0018.sai.workshift.exceptions.WorkshiftServiceException;
import com.abcm0018.sai.workshift.application.service.WorkshiftService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkshiftServiceImpl implements WorkshiftService {

	private final UserRepository userRepository;
	private final ShiftRepository shiftRepository;
	private final WorkshiftMapper workshiftMapper;
	private final WorkshiftRepository workshiftRepository;

	private final RedisTemplate<String, Long> workshiftRedisTemplate;

	// Constantes de negocio
	private static final int DATA_RETENTION_MONTHS = 12;

	// --- CONSTANTES (copiadas de WorkshiftCacheScheduler) ---
	private static final String WORKSHIFT_CACHE_KEY_PATTERN = "workshift:user:%d:date:%s";
	private static final Duration CACHE_TTL = Duration.ofHours(24);

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "workshifts", allEntries = true),
			@CacheEvict(value = "workshiftsByUser", allEntries = true),
			@CacheEvict(value = "workshiftsByDate", allEntries = true)
	})
	public WorkshiftDetailResponseDTO createExceptionalWorkshift(CreateWorkshiftRequestDTO requestDTO) {
		log.warn("Creando turno EXCEPCIONAL - Usuario: {}, Fecha: {}, Motivo: {}", requestDTO.getUserId(), requestDTO.getDate(), requestDTO.getReason());

		// Validar motivo obligatorio
		if (StringUtils.isEmpty(requestDTO.getReason())) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "Debe proporcionar un motivo para crear un turno de forma manual", HttpStatus.BAD_REQUEST);
		}

		// Obtener usuario y shift
		User user = getUserEntityById(requestDTO.getUserId());
		Shift shift = getShift(requestDTO.getShiftId());

		// Validaciones de negocio
		validateWorkshiftCreation(user, shift);

		// Verificar conflictos (solo advertencia)
		WorkshiftConflictDTO conflicts = checkConflicts(requestDTO.getUserId(), requestDTO.getDate(), null);

		if (conflicts.getHasConflicts()) {
			log.warn("ADVERTENCIA: {}", conflicts.getMessage());
		}

		// Crear workshift
		Workshift workshift = workshiftMapper.toWorkshift(requestDTO);
		workshift.setUser(user);
		workshift.setShift(shift);

		Workshift saved = workshiftRepository.save(workshift);

		log.info("Turno excepcional creado - ID: {} | Motivo: {}", saved.getId(), requestDTO.getReason());

		// TODO: Notificar al usuario y registrar en auditoría
		// auditService.logExceptionalWorkshift(saved, requestDTO.getReason());

		return workshiftMapper.toDetailResponse(saved);
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "workshifts", key = "#id"),
			@CacheEvict(value = "workshiftsByUser", allEntries = true),
			@CacheEvict(value = "workshiftsByDate", allEntries = true)
	})
	public WorkshiftDetailResponseDTO updateWorkshift(Long id, UpdateWorkshiftRequestDTO requestDTO) {
		log.warn("Actualizando workshift {} manualmente - Motivo: {}", id, requestDTO.getReason());

		if (StringUtils.isEmpty(requestDTO.getReason())) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "Debe proporcionar un motivo para actualizar un turno de forma manual", HttpStatus.BAD_REQUEST);
		}

		Workshift workshift = getWorkshiftEntityById(id);

		// Actualizar campos si vienen en el request
		if (requestDTO.getUserId() != null) {
			User newUser = getUserEntityById(requestDTO.getUserId());
			validateNoConflict(newUser, requestDTO.getDate() != null ? requestDTO.getDate() : workshift.getDate(), id);
			workshift.setUser(newUser);
		}

		if (requestDTO.getShiftId() != null) {
			Shift newShift = getShift(requestDTO.getShiftId());
			workshift.setShift(newShift);
		}

		if (requestDTO.getDate() != null) {
			validateNoConflict(workshift.getUser(), requestDTO.getDate(), id);
			workshift.setDate(requestDTO.getDate());
		}

		Workshift updated = workshiftRepository.save(workshift);

		log.warn("Workshift {} actualizado manualmente | Motivo: {}", id, requestDTO.getReason());

		// TODO: Registrar en auditoría
		// auditService.logWorkshiftUpdate(updated, requestDTO.getReason());

		return workshiftMapper.toDetailResponse(updated);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workshifts", key = "#id")
	public WorkshiftDetailResponseDTO findById(Long id) {
		return workshiftMapper.toDetailResponse(getWorkshiftEntityById(id));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<WorkshiftSummaryDTO> findAll(Pageable pageable) {
		return workshiftRepository.findAll(pageable).map(workshiftMapper::toSummary);
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "workshifts", key = "#id"),
			@CacheEvict(value = "workshiftsByUser", allEntries = true),
			@CacheEvict(value = "workshiftsByDate", allEntries = true)
	})
	public void deleteWorkshift(Long id) {
		log.info("Eliminando workshift {}", id);

		Workshift workshift = getWorkshiftEntityById(id);

		// Validar que no tenga datos asociados
		validateCanDelete(workshift);

		workshiftRepository.delete(workshift);

		log.info("Workshift {} eliminado exitosamente", id);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workshiftsByUser", key = "#userId")
	public List<WorkshiftResponseDTO> findByUser(Long userId) {
		User user = getUserEntityById(userId);
		return workshiftMapper.toResponseList(workshiftRepository.findByUserOrderByDateDesc(user));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<WorkshiftSummaryDTO> findByUserPaginated(Long userId, Pageable pageable) {
		User user = getUserEntityById(userId);
		return workshiftRepository.findByUser(user, pageable).map(workshiftMapper::toSummary);
	}

	@Override
	@Transactional(readOnly = true)
	public WorkshiftDetailResponseDTO getTodayWorkshift(Long userId) {
		User user = getUserEntityById(userId);
		Workshift workshift = workshiftRepository.findTodayWorkshiftByUser(user, LocalDate.now())
				.orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "El usuario no tiene turno asignado para hoy", HttpStatus.NOT_FOUND));
		return workshiftMapper.toDetailResponse(workshift);
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkshiftResponseDTO> findFutureWorkshifts(Long userId) {
		User user = getUserEntityById(userId);
		return workshiftMapper.toResponseList(workshiftRepository.findFutureWorkshiftsByUser(user, LocalDate.now()));
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkshiftResponseDTO> findPastWorkshifts(Long userId) {
		User user = getUserEntityById(userId);
		return workshiftMapper.toResponseList(workshiftRepository.findPastWorkshiftsByUser(user, LocalDate.now()));
	}

	@Override
	@Transactional(readOnly = true)
	public WorkshiftDetailResponseDTO getNextWorkshift(Long userId) {
		User user = getUserEntityById(userId);
		Workshift workshift = workshiftRepository.findNextWorkshiftByUser(user, LocalDate.now())
				.orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "El usuario no tiene turnos futuros asignados", HttpStatus.NOT_FOUND));
		return workshiftMapper.toDetailResponse(workshift);
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkshiftResponseDTO> findByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
		validateDateRange(startDate, endDate);
		User user = getUserEntityById(userId);
		return workshiftMapper.toResponseList(workshiftRepository.findByUserAndDateBetweenOrderByDateAsc(user, startDate, endDate));
	}

	@Override
	@Transactional(readOnly = true)
	public Long countWorkshiftsByUser(Long userId) {
		User user = getUserEntityById(userId);
		return workshiftRepository.countByUser(user);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workshiftsByDate", key = "#date")
	public List<WorkshiftResponseDTO> findByDate(LocalDate date) {
		return workshiftMapper.toResponseList(workshiftRepository.findByDate(date));
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkshiftResponseDTO> findTodayWorkshifts() {
		return workshiftMapper.toResponseList(workshiftRepository.findByDate(LocalDate.now()));
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkshiftResponseDTO> findByDateRange(LocalDate startDate, LocalDate endDate) {
		validateDateRange(startDate, endDate);
		return workshiftMapper.toResponseList(workshiftRepository.findByDateBetweenOrderByDateAsc(startDate, endDate));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> findUserIdsWorkingOnDate(LocalDate date) {
		return workshiftRepository.findUsersWorkingOnDate(date).stream().map(User::getId).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Long countWorkshiftsByDate(LocalDate date) {
		return workshiftRepository.countByDate(date);
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkshiftResponseDTO> findCurrentWeekWorkshifts() {
		LocalDate today = LocalDate.now();
		LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

		return workshiftMapper.toResponseList(workshiftRepository.findWorkshiftsOfWeek(startOfWeek, endOfWeek));
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkshiftResponseDTO> findCurrentMonthWorkshifts() {
		LocalDate today = LocalDate.now();
		LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

		return workshiftMapper.toResponseList(workshiftRepository.findWorkshiftsOfMonth(startOfMonth, endOfMonth));
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkshiftResponseDTO> findWorkshiftsOfWeek(LocalDate anyDayOfWeek) {
		LocalDate startOfWeek = anyDayOfWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate endOfWeek = anyDayOfWeek.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

		return workshiftMapper.toResponseList(workshiftRepository.findWorkshiftsOfWeek(startOfWeek, endOfWeek));
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkshiftResponseDTO> findWorkshiftsOfMonth(int year, int month) {
		LocalDate startOfMonth = LocalDate.of(year, month, 1);
		LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());

		return workshiftMapper.toResponseList(workshiftRepository.findWorkshiftsOfMonth(startOfMonth, endOfMonth));
	}


	@Override
	@Transactional(readOnly = true)
	public List<WorkshiftResponseDTO> findByShift(Long shiftId) {
		Shift shift = getShift(shiftId);
		return workshiftMapper.toResponseList(workshiftRepository.findByShift(shift));
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkshiftResponseDTO> findByShiftType(ShiftType shiftType) {
		return workshiftMapper.toResponseList(workshiftRepository.findByShiftType(shiftType));
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkshiftResponseDTO> findByUserAndShiftType(Long userId, ShiftType shiftType) {
		return workshiftMapper.toResponseList(workshiftRepository.findByUserAndShiftType(getUserEntityById(userId), shiftType));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<WorkshiftSummaryDTO> findWithFilters(WorkshiftFilterDTO filterDTO, Pageable pageable) {
		log.debug("Buscando workshifts con filtros: {}", filterDTO);

		// Aplicar filtros progresivamente
		List<Workshift> results = workshiftRepository.findAll();

		// Filtrar por usuario
		if (filterDTO.getUserId() != null) {
			User user = getUserEntityById(filterDTO.getUserId());
			results = results.stream().filter(w -> w.getUser().getId().equals(user.getId())).toList();
		}

		// Filtrar por shift
		if (filterDTO.getShiftId() != null) {
			results = results.stream().filter(w -> w.getShift().getId().equals(filterDTO.getShiftId())).toList();
		}

		// Filtrar por tipo de turno
		if (filterDTO.getShiftType() != null) {
			ShiftType type = ShiftType.valueOf(filterDTO.getShiftType());
			results = results.stream().filter(w -> w.getShift().getShiftType() == type).toList();
		}

		// Filtrar por fecha exacta
		if (filterDTO.getExactDate() != null) {
			results = results.stream().filter(w -> w.getDate().equals(filterDTO.getExactDate())).toList();
		}

		// Filtrar por rango de fechas
		if (filterDTO.getStartDate() != null && filterDTO.getEndDate() != null) {
			results = results.stream().filter(w -> !w.getDate().isBefore(filterDTO.getStartDate()) && !w.getDate().isAfter(filterDTO.getEndDate())).toList();
		}

		// Filtrar por estado temporal
		if (Boolean.TRUE.equals(filterDTO.getIsToday())) {
			results = results.stream().filter(w -> w.getDate().equals(LocalDate.now())).toList();
		}
		if (Boolean.TRUE.equals(filterDTO.getIsPast())) {
			results = results.stream().filter(w -> w.getDate().isBefore(LocalDate.now())).toList();
		}
		if (Boolean.TRUE.equals(filterDTO.getIsFuture())) {
			results = results.stream().filter(w -> w.getDate().isAfter(LocalDate.now())).toList();
		}

		// Convertir a Page
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), results.size());
		List<Workshift> pageContent = results.subList(start, end);

		return new PageImpl<>(workshiftMapper.toSummaryList(pageContent), pageable, results.size());
	}

	@Override
	@Transactional(readOnly = true)
	public WorkshiftConflictDTO checkConflicts(Long userId, LocalDate date, Long excludeId) {
		User user = getUserEntityById(userId);
		List<Workshift> existing = workshiftRepository.findPotentialConflicts(user, date);

		// Excluir el workshift actual si se está actualizando
		if (excludeId != null) {
			existing = existing.stream().filter(w -> !w.getId().equals(excludeId)).toList();
		}

		WorkshiftConflictDTO.WorkshiftConflictDTOBuilder conflictBuilder = WorkshiftConflictDTO.builder()
				.userId(userId)
				.userFullName(user.getFullName())
				.date(date)
				.conflictingWorkshifts(workshiftMapper.toSummaryList(existing))
				.warnings(new ArrayList<>());

		if (!existing.isEmpty()) {
			conflictBuilder
					.hasConflicts(true)
					.conflictType("DUPLICATE")
					.message(String.format("El usuario ya tiene %d turno(s) asignado(s) para la fecha %s", existing.size(), date))
					.canProceed(true); // Permitir pero con advertencia

			conflictBuilder.warnings(List.of("Este usuario ya tiene turnos asignados para esta fecha", "La creación manual puede generar duplicados", "Verifique que esto sea intencional"));
		} else {
			conflictBuilder.hasConflicts(false).message("No se detectaron conflictos").canProceed(true);
		}

		return conflictBuilder.build();
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workshiftStats", key = "#startDate + '-' + #endDate")
	public WorkshiftStatisticsDTO getStatistics(LocalDate startDate, LocalDate endDate) {
		log.debug("Obteniendo estadísticas de workshifts");

		LocalDate start = startDate != null ? startDate : LocalDate.now().minusMonths(1);
		LocalDate end = endDate != null ? endDate : LocalDate.now();

		// Estadísticas generales
		long totalWorkshifts = workshiftRepository.count();
		long totalActiveOperators = userRepository.findByRoleAndActiveTrue(Role.OPERATOR).size();
		long totalShiftsConfigured = shiftRepository.findByActiveTrue().size();

		// Estadísticas por período
		long workshiftsThisWeek = findCurrentWeekWorkshifts().size();
		long workshiftsThisMonth = findCurrentMonthWorkshifts().size();
		long workshiftsToday = countWorkshiftsByDate(LocalDate.now());

		// Estadísticas futuras
		List<Workshift> futureWorkshifts = workshiftRepository.findByDateAfterOrderByDateAsc(LocalDate.now());
		Long futureWorkshiftsCount = (long) futureWorkshifts.size();

		LocalDate nextMonday = getNextMonday();
		LocalDate nextFriday = nextMonday.plusDays(4);
		Boolean nextWeekPlanned = workshiftRepository.countByDateBetween(nextMonday, nextFriday) > 0;

		// Distribución por tipo de turno
		Map<String, Long> byShiftType = new HashMap<>();
		for (ShiftType type : ShiftType.values()) {
			Long count = workshiftRepository.countByShiftType(type);
			byShiftType.put(type.getDisplayName(), count);
		}

		// Auditoría
		Long modifiedWorkshifts = workshiftRepository.countModifiedWorkshifts();
		Long workshiftsWithoutPalets = (long) workshiftRepository.findWorkshiftsWithoutPalets(LocalDate.now()).size();
		Long workshiftsWithoutTimesheets = (long) workshiftRepository.findWorkshiftsWithoutTimesheets(LocalDate.now()).size();

		return WorkshiftStatisticsDTO.builder()
				.totalWorkshifts(totalWorkshifts)
				.totalActiveOperators(totalActiveOperators)
				.totalShiftsConfigured(totalShiftsConfigured)
				.workshiftsThisWeek(workshiftsThisWeek)
				.workshiftsThisMonth(workshiftsThisMonth)
				.workshiftsToday(workshiftsToday)
				.futureWorkshifts(futureWorkshiftsCount)
				.nextWeekPlanned(nextWeekPlanned)
				.workshiftsByShiftType(byShiftType)
				.averageWorkshiftsPerUser(totalActiveOperators > 0 ? (double) totalWorkshifts / totalActiveOperators : 0.0)
				.modifiedWorkshifts(modifiedWorkshifts)
				.workshiftsWithoutPalets(workshiftsWithoutPalets)
				.workshiftsWithoutTimesheets(workshiftsWithoutTimesheets)
				.queryStartDate(start)
				.queryEndDate(end)
				.build();
	}

	@Override
	@Transactional
	public void cleanOldWorkshifts() {
		LocalDate cutoffDate = LocalDate.now().minusMonths(DATA_RETENTION_MONTHS);

		log.info("Eliminando workshifts anteriores a {}", cutoffDate);

		workshiftRepository.deleteWorkshiftsOlderThan(cutoffDate);

		log.info("Limpieza de workshifts antiguos completada");
	}

	@Override
	@Transactional
	@CacheEvict(value = "workshifts", allEntries = true)
	public WeeklyScheduleResponseDTO generateNextWeekSchedule() {
		log.info("╔════════════════════════════════════════════════════════════════╗");
		log.info("║   INICIANDO GENERACIÓN AUTOMÁTICA DE TURNOS SEMANALES        ║");
		log.info("║   Fecha/Hora: {}                             ║", LocalDateTime.now());
		log.info("╚════════════════════════════════════════════════════════════════╝");

		// 1. CALCULAR FECHAS
		LocalDate nextMonday = getNextMonday();
		LocalDate nextFriday = nextMonday.plusDays(4);
		List<LocalDate> workDays = getWorkDaysBetween(nextMonday, nextFriday);

		log.info("Generando turnos desde {} hasta {}", nextMonday, nextFriday);

		// 2. OBTENER ENTIDADES MAESTRAS
		List<User> operators = userRepository.findByRoleAndActiveTrue(Role.OPERATOR);
		List<Shift> shifts = shiftRepository.findAllActiveOrderedByStartTime();

		// Validar que tenemos datos para trabajar
		if (operators.isEmpty()) {
			log.error("No se encontraron usuarios OPERATOR activos para asignar turnos.");
			return buildErrorResponse(nextMonday, nextFriday, "No hay operarios activos para planificar.");
		}
		if (shifts.isEmpty()) {
			log.error("No se encontraron plantillas de Shift (turnos) activas en la base de datos.");
			return buildErrorResponse(nextMonday, nextFriday, "No hay plantillas de turno activas para planificar.");
		}

		log.info("Planificando turnos para {} operarios usando {} plantillas de turno.", operators.size(), shifts.size());

		List<Workshift> workshiftsToSave = new ArrayList<>();

		// 3. ITERAR Y ASIGNAR TURNOS
		for (User operator : operators) {

			// Obtener el último turno del operario para saber dónde continuar la rotación
			ShiftType lastShiftType = workshiftRepository.findLastWorkshiftByUser(operator)
					.map(Workshift::getShiftType).orElse(null); // Si es nuevo, empezará por el primero

			log.debug("Procesando operario: {}. Último tipo de turno: {}", operator.getEmployeeNumber(), lastShiftType);

			// Iterar por cada día laborable (Lun-Vie)
			for (LocalDate date : workDays) {

				// Obtener el siguiente turno en la rotación
				Shift nextShift = getNextShift(shifts, lastShiftType);

				// Validamos si YA EXISTE un turno para este operario en esta fecha.
				// Esto permite "rellenar huecos" si un admin creó un turno manual.
				if (workshiftRepository.findByUserAndDate(operator, date).isEmpty()) {

					// No existe, así que lo creamos
					Workshift newWorkshift = Workshift.create(operator, nextShift, date);

					workshiftsToSave.add(newWorkshift);

				} else {
					// Ya existe un turno (ej. asignado manualmente).
					// Lo registramos y continuamos al día siguiente.
					log.warn("Saltando generación para operario {} en fecha {}: Ya existe un turno asignado.",
							operator.getEmployeeNumber(), date);
				}

				// Actualizamos el "último turno" para la siguiente iteración del día
				lastShiftType = nextShift.getShiftType();
			}
		}

		// 4. GUARDAR Y CONSTRUIR RESPUESTA
		if (!workshiftsToSave.isEmpty()) {
			log.info("Guardando {} nuevas asignaciones de turno...", workshiftsToSave.size());
			workshiftRepository.saveAll(workshiftsToSave);
		} else {
			log.warn("No se generaron nuevas asignaciones de turno (probablemente ya existían todas).");
		}

		// Obtenemos la lista COMPLETA de turnos de esa semana
		// (los que acabamos de guardar + los que ya existían y nos saltamos)
		List<Workshift> allWorkshiftsForWeek = workshiftRepository.findByDateBetweenOrderByDateAsc(nextMonday, nextFriday);

		return buildSuccessResponse(allWorkshiftsForWeek, operators, shifts, nextMonday, nextFriday);
	}

	@Override
	public Optional<Workshift> findCachedWorkshiftByEmployeeAndDate(String employeeNumber, LocalDate date) {

		log.debug("Buscando workshift cacheado para empleado {} en fecha {}", employeeNumber, date);

		// 1. Buscar el usuario
		Optional<User> userOpt = userRepository.findByEmployeeNumber(employeeNumber);
		if (userOpt.isEmpty()) {
			log.warn("No se encontró usuario con employeeNumber: {}. No se puede buscar turno.", employeeNumber);
			return Optional.empty();
		}
		User user = userOpt.get();

		// 2. Construir clave de caché
		String cacheKey = String.format(WORKSHIFT_CACHE_KEY_PATTERN, user.getId(), date.toString());

		Long workshiftId = null;
		try {
			// 3. Intentar leer ID desde Redis
			workshiftId = workshiftRedisTemplate.opsForValue().get(cacheKey);
		} catch (Exception e) {
			log.error("❌ Error al leer de Redis (key: {}). Cayendo a BBDD. Error: {}", cacheKey, e.getMessage());
		}

		// 4. CACHE HIT
		if (workshiftId != null) {
			log.debug("Cache HIT para clave {}. Workshift ID: {}", cacheKey, workshiftId);
			Workshift workshift =  workshiftRepository.findById(workshiftId).orElseThrow(
					() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "No existe el turno ", HttpStatus.NOT_FOUND));

			return Optional.of(workshift);
			// Mapeamos el resultado a DTO
			//return Optional.of(workshiftMapper.toResponse(workshift));
		}

		// 5. CACHE MISS
		log.warn("Cache MISS para clave {}. Consultando base de datos...", cacheKey);
		List<Workshift> shiftsFromDB = workshiftRepository.findByUserAndDate(user, date);

		if (shiftsFromDB.isEmpty()) {
			log.debug("No se encontró Workshift en BBDD para clave {}", cacheKey);
			return Optional.empty(); // Realmente no existe
		}

		// 6. Encontrado en BBDD, repoblar caché
		Workshift foundShift = shiftsFromDB.get(0);
		try {
			log.info("Repoblando caché (miss) para clave {}.", cacheKey);
			workshiftRedisTemplate.opsForValue().set(cacheKey, foundShift.getId(), CACHE_TTL);
		} catch (Exception e) {
			log.error("❌ Error al repoblar Redis (key: {}) tras un cache miss. Error: {}", cacheKey, e.getMessage());
		}

		// 7. Devolver el DTO
		return foundShift != null ? Optional.of(foundShift) : Optional.empty();
	}

	/**
	 * Determina el siguiente Shift (plantilla de turno) en la rotación.
	 *
	 * @param shifts        La lista de todos los Shift (plantillas) activos, ordenados.
	 * @param lastShiftType El tipo de turno (MORNING, AFTERNOON...) que tuvo el
	 * usuario en su última asignación.
	 * @return El siguiente Shift en la secuencia.
	 */
	private Shift getNextShift(List<Shift> shifts, ShiftType lastShiftType) {
		// 1. Si no hay turno previo (ej. empleado nuevo), se le asigna el primero de la lista.
		if (lastShiftType == null) {
			return shifts.get(0);
		}

		// 2. Encontrar el índice del último turno en la lista de plantillas
		int lastIndex = IntStream.range(0, shifts.size())
				.filter(i -> shifts.get(i).getShiftType().equals(lastShiftType))
				.findFirst()
				.orElse(-1);

		// 3. Si por alguna razón no se encuentra (ej. el turno se desactivó),
		//    asignamos el primero para reiniciar el ciclo.
		if (lastIndex == -1) {
			return shifts.get(0);
		}

		// 4. Lógica de rotación (Round-Robin)
		// Calculamos el siguiente índice. Usamos el módulo (%) para que "dé la vuelta"
		// (ej. si hay 3 turnos, índice 0, 1, 2. Si lastIndex=2 -> (2+1)%3 = 0)
		int nextIndex = (lastIndex + 1) % shifts.size();

		return shifts.get(nextIndex);
	}

	private User getUserEntityById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "Usuario no encontrado con ID: " + id, HttpStatus.NOT_FOUND));
	}

	private Shift getShift(Long shiftId) {
		return shiftRepository.findById(shiftId).orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "No se ha encontrado el turno", HttpStatus.NOT_FOUND));
	}

	private Workshift getWorkshiftEntityById(Long id) {
		return workshiftRepository.findById(id)
				.orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "Asignación de turno no encontrada con ID: " + id, HttpStatus.NOT_FOUND));
	}

	private void validateWorkshiftCreation(User user, Shift shift) {
		if (!user.isActive()) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "No se puede asignar turno a un usuario inactivo", HttpStatus.BAD_REQUEST);
		}

		if (user.isBlocked()) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "No se puede asignar turno a un usuario bloqueado", HttpStatus.BAD_REQUEST);
		}

		if (!shift.isActive()) {
			throw new WorkshiftServiceException(CustomErrorCode.CONFLICT, "No se puede asignar un turno (shift) inactivo", HttpStatus.CONFLICT);
		}
	}

	private void validateNoConflict(User user, LocalDate date, Long excludeId) {
		List<Workshift> existing = workshiftRepository.findPotentialConflicts(user, date);

		if (excludeId != null) {
			existing = existing.stream().filter(w -> !w.getId().equals(excludeId)).toList();
		}

		if (!existing.isEmpty()) {
			log.warn("ADVERTENCIA: El usuario {} ya tiene {} turno(s) para la fecha {}", user.getId(), existing.size(), date);
		}
	}

	private void validateCanDelete(Workshift workshift) {
		if (workshift.getTotalScannedPalets() > 0) {
			String message = String.format("No se puede eliminar el turno porque tiene %d palets asociados", workshift.getTotalScannedPalets());
			throw new WorkshiftServiceException(CustomErrorCode.CONFLICT, message, HttpStatus.CONFLICT);
		}

		if (workshift.getTotalTimesheets() > 0) {
			String message = String.format("No se puede eliminar el turno porque tiene %d fichajes asociados", workshift.getTotalTimesheets());
			throw new WorkshiftServiceException(CustomErrorCode.CONFLICT, message, HttpStatus.CONFLICT);
		}
	}

	private void validateDateRange(LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "Las fechas de inicio y fin son obligatorias", HttpStatus.BAD_REQUEST);
		}

		if (startDate.isAfter(endDate)) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "La fecha de inicio no puede ser posterior a la fecha de fin", HttpStatus.BAD_REQUEST);
		}
	}

	private List<LocalDate> getWorkDaysBetween(LocalDate startDate, LocalDate endDate) {
		return startDate.datesUntil(endDate.plusDays(1))
				.filter(date -> {
					DayOfWeek day = date.getDayOfWeek();
					return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
				}).toList();
	}

	private LocalDate getNextMonday() {
		LocalDate today = LocalDate.now();
		return today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
	}

	private WeeklyScheduleResponseDTO buildSuccessResponse(List<Workshift> workshifts, List<User> operators, List<Shift> shifts, LocalDate startDate, LocalDate endDate) {

		// Calcular distribuciones
		Map<String, Integer> distributionByUser = new HashMap<>();
		Map<String, Integer> distributionByShiftType = new HashMap<>();
		Map<LocalDate, Integer> distributionByDay = new HashMap<>();

		for (Workshift w : workshifts) {
			// Por usuario
			String userName = w.getUser().getFullName();
			distributionByUser.put(userName, distributionByUser.getOrDefault(userName, 0) + 1);

			// Por tipo de turno
			String shiftType = w.getShift().getShiftType().getDisplayName();
			distributionByShiftType.put(shiftType, distributionByShiftType.getOrDefault(shiftType, 0) + 1);

			// Por día
			distributionByDay.put(w.getDate(), distributionByDay.getOrDefault(w.getDate(), 0) + 1);
		}

		return WeeklyScheduleResponseDTO.builder()
				.startDate(startDate)
				.endDate(endDate)
				.totalWorkshiftsGenerated(workshifts.size())
				.totalOperators(operators.size())
				.totalShifts(shifts.size())
				.workDays(getWorkDaysBetween(startDate, endDate).size())
				.workshifts(workshiftMapper.toSummaryList(workshifts))
				.distributionByUser(distributionByUser)
				.distributionByShiftType(distributionByShiftType)
				.distributionByDay(distributionByDay)
				.generatedAt(LocalDateTime.now())
				.success(true)
				.message("Planificación semanal generada exitosamente")
				.build();
	}

	private WeeklyScheduleResponseDTO buildErrorResponse(LocalDate startDate, LocalDate endDate, String errorMessage) {

		return WeeklyScheduleResponseDTO.builder()
				.startDate(startDate)
				.endDate(endDate)
				.totalWorkshiftsGenerated(0)
				.totalOperators(0)
				.totalShifts(0)
				.workDays(0)
				.workshifts(List.of())
				.distributionByUser(Map.of())
				.distributionByShiftType(Map.of())
				.distributionByDay(Map.of())
				.generatedAt(LocalDateTime.now())
				.success(false)
				.message("Error en generación: " + errorMessage)
				.build();
	}
}