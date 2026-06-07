package com.abcm0018.sai.workshift.application.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
import com.abcm0018.sai.workshift.application.dtos.PlanningContext;
import com.abcm0018.sai.workshift.application.dtos.UpdateWorkshiftRequestDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftFilterDTO;
import com.abcm0018.sai.workshift.application.dtos.WorkshiftResponseDTO;
import com.abcm0018.sai.workshift.application.mapper.WorkshiftMapper;
import com.abcm0018.sai.workshift.domain.entity.Workshift;
import com.abcm0018.sai.workshift.domain.repository.WorkshiftRepository;
import com.abcm0018.sai.workshift.domain.specifications.WorkshiftSpecificationBuilder;
import com.abcm0018.sai.workshift.exceptions.WorkshiftServiceException;
import com.abcm0018.sai.workshift.application.service.WorkshiftService;
import com.abcm0018.sai.workshift.shared.WorkshiftCacheConstants;

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
	private final WorkshiftSpecificationBuilder specificationBuilder;

	private final RedisTemplate<String, Long> workshiftRedisTemplate;

	private static final int DATA_RETENTION_MONTHS = 12;

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "workshifts",       allEntries = true),
			@CacheEvict(value = "workshiftsByUser", allEntries = true),
			@CacheEvict(value = "workshiftsByDate", allEntries = true)
	})
	public Integer generateWorkshiftSchedule(CreateWorkshiftRequestDTO requestDTO) {

		if (Objects.isNull(requestDTO)) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "Fecha inicio y fecha fin son obligatorias", HttpStatus.BAD_REQUEST);
		}

		if (requestDTO.getFromDate().isAfter(requestDTO.getToDate())) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "Fecha inicio no puede ser mayor a fecha fin", HttpStatus.BAD_REQUEST);
		}

		// Fail-fast: dos COUNT queries baratos evitan cargar el contexto completo
		// si el rango ya está planificado al 100% (operarios × días del rango).
		List<LocalDate> workingDays = getAllDaysBetween(requestDTO.getFromDate(), requestDTO.getToDate());
		if (!workingDays.isEmpty()) {
			long activeOperators = userRepository.countByRoleAndActiveTrue(Role.OPERATOR);
			long expected = (long) workingDays.size() * activeOperators;
			long existing  = workshiftRepository.countByDateBetween(requestDTO.getFromDate(), requestDTO.getToDate());
			if (expected > 0 && existing >= expected) {
				log.info("Rango [{} – {}] ya planificado ({}/{} turnos). Sin cambios.",
						requestDTO.getFromDate(), requestDTO.getToDate(), existing, expected);
				return 0;
			}
		}

		log.info("Preparando el contexto de planificación: {} → {}", requestDTO.getFromDate(), requestDTO.getToDate());

		PlanningContext context = preparePlanningContext(requestDTO.getFromDate(), requestDTO.getToDate(), false);

		log.info("Planificando turnos para {} operarios, {} tipos de turno.", context.getOperators().size(), context.getActiveShifts().size());

		List<Workshift> workshiftsToSave = calculateRotation(context);

		if (!workshiftsToSave.isEmpty()) {
			log.info("Guardando {} nuevos turnos...", workshiftsToSave.size());
			List<Workshift> saved = workshiftRepository.saveAll(workshiftsToSave);
			cacheWorkshiftsInRedis(saved);
			return saved.size();
		}

		log.warn("No se han generado asignaciones de turno para las fechas indicadas.");
		return 0;
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "workshifts", key = "#id"),
			@CacheEvict(value = "workshiftsByUser", allEntries = true),
			@CacheEvict(value = "workshiftsByDate", allEntries = true)
	})
	public WorkshiftResponseDTO updateWorkshift(Long id, UpdateWorkshiftRequestDTO requestDTO) {
		log.warn("Actualizando workshift {} manualmente - Motivo: {}", id, requestDTO.getReason());

		if (StringUtils.isBlank(requestDTO.getReason())) {
			throw new WorkshiftServiceException(
					CustomErrorCode.BAD_REQUEST,
					"Debe proporcionar un motivo para actualizar un turno de forma manual", HttpStatus.BAD_REQUEST);
		}

		Workshift currentWorkshift = getWorkshiftEntityById(id);

		// Check de concurrencia
		// Comporamos la version de la BD con la versión que tenía el usuario en su pantalla
		if (requestDTO.getVersion() != null && !Objects.equals(requestDTO.getVersion(), currentWorkshift.getVersion())) {
			throw new WorkshiftServiceException(CustomErrorCode.CONFLICT,
					"El registro ha sido modificado por otro usuario. Por favor, recargue la página.",
					HttpStatus.CONFLICT);
		}

		Shift effectiveShift = currentWorkshift.getShift();
		// Si no viene ID nuevo, usamos el que ya tiene la entidad.
		if (requestDTO.getShiftId() != null) {
			effectiveShift = shiftRepository
					.findById(requestDTO.getShiftId())
					.orElseThrow(() -> new WorkshiftServiceException(
							CustomErrorCode.NOT_FOUND, "No se ha encontrado el turno", HttpStatus.NOT_FOUND)
					);
		}

		LocalDate effectiveDate = requestDTO.getDate() != null ? requestDTO.getDate() : currentWorkshift.getDate();

		// Validamos si el cambio es legal según la hora actual y el estado del turno
		validateShiftChangeRules(currentWorkshift, effectiveShift, effectiveDate);

		// Validación de negocio (conflictos)
		boolean isDateChange = !effectiveDate.equals(currentWorkshift.getDate());
		boolean isShiftChange = !effectiveShift.equals(currentWorkshift.getShift());

		if (isShiftChange || isDateChange) {
			validateNoConflict(currentWorkshift.getUser(), effectiveDate, effectiveShift, id);
		}

		// Capturar la clave Redis antes de mutar la entidad
		Long userId  = currentWorkshift.getUser().getId();
		LocalDate oldDate = currentWorkshift.getDate();

		currentWorkshift.setDate(effectiveDate);
		currentWorkshift.setShift(effectiveShift);
		Workshift updated = workshiftRepository.save(currentWorkshift);

		log.warn("AUDIT: Workshift {} actualizado manualmente | Motivo: {}", id, requestDTO.getReason());

		// Sincronizar Redis: evictar la clave antigua si la fecha cambió, luego escribir la nueva
		if (!oldDate.equals(effectiveDate)) {
			evictRedisKey(userId, oldDate);
		}
		cacheWorkshiftsInRedis(List.of(updated));

		// TODO: Registrar en auditoría
		// auditService.logWorkshiftUpdate(updated, requestDTO.getReason());

		return workshiftMapper.toResponse(updated);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "workshifts", key = "#id")
	public WorkshiftResponseDTO findById(Long id) {
		return workshiftMapper.toResponse(getWorkshiftEntityById(id));
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "workshifts", key = "#id"),
			@CacheEvict(value = "workshiftsByUser", allEntries = true),
			@CacheEvict(value = "workshiftsByDate", allEntries = true)
	})
	public int deleteWorkshift(Long id) {
		log.info("Eliminando workshift {}", id);
		Workshift workshift;
		try {
			workshift = getWorkshiftEntityById(id);
		} catch (Exception e) {
			if (e instanceof WorkshiftServiceException && ((WorkshiftServiceException) e).getHttpStatus() == HttpStatus.NOT_FOUND) {
				return 0;
			}
			throw new RuntimeException(e);
		}

		// Capturar datos para Redis antes de eliminar la entidad
		Long userId = workshift.getUser().getId();
		LocalDate date = workshift.getDate();

		// Timesheet count check delegated to sai-timesheet module (cross-module boundary)

		workshiftRepository.delete(workshift);
		evictRedisKey(userId, date);
		log.info("Workshift {} eliminado exitosamente", id);
		return 1;
	}

	@Override
	@Transactional(readOnly = true)
	public WorkshiftResponseDTO getTodayWorkshift(Long userId) {
		User user = getUserEntityById(userId);
		Workshift workshift = workshiftRepository.findTodayWorkshiftByUser(user, LocalDate.now())
				.orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "El usuario no tiene turno asignado para hoy", HttpStatus.NOT_FOUND));
		return workshiftMapper.toResponse(workshift);
	}

	@Override
	@Transactional(readOnly = true)
	public WorkshiftResponseDTO getNextWorkshift(Long userId) {
		User user = getUserEntityById(userId);

		List<Workshift> workshifts = workshiftRepository.findNextWorkshiftByUser(
				user,
				LocalDate.now(),
				PageRequest.of(0, 1)
		);

		return workshifts.stream()
				.findFirst()
				.map(workshiftMapper::toResponse)
				.orElse(null);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<WorkshiftResponseDTO> findWithFilters(WorkshiftFilterDTO filterDTO, Pageable pageable) {
		log.debug("Buscando workshifts con filtros: {}", filterDTO);

		// Construimos la consulta
		Specification<Workshift> spec = specificationBuilder.buildFrom(filterDTO);
		// Ejecutamos la consulta
		Page<Workshift> workshifts = workshiftRepository.findAll(spec, pageable);

		return workshifts.map(workshiftMapper::toResponse);
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
	public void generateNextWeekSchedule() {

		LocalDate today = LocalDate.now();
		LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		LocalDate nextSunday = nextMonday.plusDays(6);

		log.info("[CRON] Iniciando generación de turnos: {} al {}", nextMonday, nextSunday);

		try {
			PlanningContext context = preparePlanningContext(nextMonday, nextSunday, true);

			List<Workshift> generatedWorkshifts = calculateRotation(context);

			if (generatedWorkshifts.isEmpty()) {
				log.warn("No se generaron turnos (Quizás ya existen?)");
				return;
			}

			List<Workshift> saved = workshiftRepository.saveAll(generatedWorkshifts);
			cacheWorkshiftsInRedis(saved);
			log.info("[CRON] ÉXITO: Generados {} turnos para {} operadores.", saved.size(), context.getOperators().size());
		} catch (Exception e) {
			log.error("[CRON] ERROR CRÍTICO: Fallo al generar la planificación semanal.", e);
			throw e;
		}
	}

	@Override
	public Optional<Workshift> findCachedWorkshiftByEmployeeAndDate(String employeeNumber, LocalDateTime scanDateTime) {

		final LocalDate date     = scanDateTime.toLocalDate();
		final LocalTime scanTime = scanDateTime.toLocalTime();

		log.debug("Buscando workshift para empleado {} en fecha {} hora {}", employeeNumber, date, scanTime);

		// 1. Buscar el usuario
		Optional<User> userOpt = userRepository.findByEmployeeNumber(employeeNumber);
		if (userOpt.isEmpty()) {
			log.warn("No se encontró usuario con employeeNumber: {}. No se puede buscar turno.", employeeNumber);
			return Optional.empty();
		}
		User user = userOpt.get();

		// 2. Construir clave de caché (por fecha)
		String cacheKey = String.format(WorkshiftCacheConstants.KEY_PATTERN, user.getId(), date.toString());

		Long workshiftId = null;
		try {
			workshiftId = workshiftRedisTemplate.opsForValue().get(cacheKey);
		} catch (Exception e) {
			log.error("Error al leer de Redis (key: {}). Cayendo a BBDD. Error: {}", cacheKey, e.getMessage());
		}

		// 3. CACHE HIT: validar que la hora del escaneo sigue dentro de la franja del turno cacheado
		if (workshiftId != null) {
			log.debug("Cache HIT para clave {}. Workshift ID: {}", cacheKey, workshiftId);
			Workshift cached = workshiftRepository.findById(workshiftId).orElseThrow(
					() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "No existe el turno ", HttpStatus.NOT_FOUND));

			if (cached.getShift().isTimeWithinShift(scanTime)) {
				return Optional.of(cached);
			}

			// La hora no coincide con el turno cacheado: evictar y releer desde BD
			log.warn("Cache HIT pero hora {} fuera de la franja del turno {} ({}–{}). Evictando y releyendo.",
					scanTime, cached.getShift().getShiftType(),
					cached.getShift().getStartTime(), cached.getShift().getEndTime());
			evictRedisKey(user.getId(), date);
		}

		// 4. CACHE MISS o time-mismatch: consultar BD con Shift cargado (evita N+1)
		log.warn("Cache MISS para clave {}. Consultando base de datos...", cacheKey);
		List<Workshift> shiftsFromDB = workshiftRepository.findByUserAndDateWithShift(user, date);

		if (shiftsFromDB.isEmpty()) {
			log.debug("No se encontró Workshift en BBDD para clave {}", cacheKey);
			return Optional.empty();
		}

		// 5. Seleccionar el turno cuya franja horaria contiene la hora del escaneo
		Workshift matched = shiftsFromDB.stream()
				.filter(w -> w.getShift().isTimeWithinShift(scanTime))
				.findFirst()
				.orElseGet(() -> {
					log.warn("Hora {} no encaja en ninguna franja para empleado {} en {}. Usando primer turno del día como fallback.",
							scanTime, employeeNumber, date);
					return shiftsFromDB.get(0);
				});

		// 6. Repoblar caché con el turno seleccionado
		try {
			log.info("Repoblando caché para clave {}. Turno seleccionado: {} ({}–{}).",
					cacheKey, matched.getShift().getShiftType(),
					matched.getShift().getStartTime(), matched.getShift().getEndTime());
			workshiftRedisTemplate.opsForValue().set(cacheKey, matched.getId(), WorkshiftCacheConstants.TTL);
		} catch (Exception e) {
			log.error("Error al repoblar Redis (key: {}) tras un cache miss. Error: {}", cacheKey, e.getMessage());
		}

		return Optional.of(matched);
	}

	/**
	 * Valida las reglas estrictas de tiempo:
	 * 1. No se pueden modificar turnos de días pasados.
	 * 2. Si es HOY:
	 * - El turno origen no debe haber comenzado.
	 * - El turno destino no debe haber comenzado o pasado.
	 */
	private void validateShiftChangeRules(Workshift currentAssignment, Shift targetShift, LocalDate targetDate) {
		LocalDate today = LocalDate.now();

		// REGLA 1: Bloqueo de historial pasado
		if (targetDate.isBefore(today)) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST,
					"No está permitido modificar ni asignar turno en fechas pasdas", HttpStatus.BAD_REQUEST);
		}

		// Si la adignación original era de un día pasado
		// generalmente se prohíbe cambiar la historia, salvo que sea corrección de error
		if (currentAssignment.getDate().isBefore(today)) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST,
					"No se puede modificar un registro histórico (fechas pasadas).", HttpStatus.BAD_REQUEST);
		}

		// Si es futuro, seguimos con la modificación
		if (targetDate.isAfter(today)) {
			return;
		}

		// REGLA 2: Lógica del "Mismo Día" (Same Day Policy)
		if (targetDate.equals(today)) {
			LocalTime now = LocalTime.now();
			Shift currentShift = currentAssignment.getShift();

			// Condición A: El turno ACTUAL (Origen) no debe haber comenzado
			if (now.equals(currentShift.getStartTime()) || now.isAfter(currentShift.getStartTime())) {
				throw new WorkshiftServiceException(CustomErrorCode.CONFLICT,
						String.format("El turno actual (%s) ya ha comenzado. No se permiten cambios una vez iniciada la jornada.",
								currentShift.getShiftType().getDisplayName()), HttpStatus.CONFLICT);
			}

			// Condición B: El turno NUEVO (Destino) no deber haber comenzado
			if (now.equals(targetShift.getStartTime()) || now.isAfter(targetShift.getStartTime())) {
				throw new WorkshiftServiceException(CustomErrorCode.CONFLICT,
						String.format("El turno nuevo (%s) ya ha comenzado. No se permiten cambios una vez iniciada la jornada.",
								targetShift.getShiftType().getDisplayName()), HttpStatus.CONFLICT);
			}
		}
	}

	private PlanningContext preparePlanningContext(LocalDate start, LocalDate end, boolean workDaysOnly) {

		List<User> operators = userRepository.findByRoleAndActiveTrue(Role.OPERATOR);
		if (operators.isEmpty()) {
			throw new WorkshiftServiceException(CustomErrorCode.INTERNAL_SERVER_ERROR,
					"No hay operarios activos para planificar.", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		List<Shift> shifts = shiftRepository.findAllActiveOrderedByStartTime();
		if (shifts.isEmpty()) {
			throw new WorkshiftServiceException(CustomErrorCode.INTERNAL_SERVER_ERROR,
					"No hay plantillas de turno activas para planificar.", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		//  BULK DATA FETCHING (Solo si las validaciones pasaron)
		// Traemos colisiones y últimos estados en paralelo (conceptualmente)
		List<Workshift> existingCollisions = workshiftRepository.findByDateBetweenAndUserIn(start, end, operators);
		List<Workshift> latestWorkshiftsForUsers = workshiftRepository.findLatestShiftTypesForUsers(operators);

		// MAPEO EN MEMORIA (Optimizaciones O(1))
		// Mapa de Colisiones: UserID -> Set<LocalDate>
		Map<Long, Set<LocalDate>> occupiedDatesMap = new HashMap<>();

		// El bucle for-each es excelente aquí, muy legible y performante
		for (Workshift workshift : existingCollisions) {
			occupiedDatesMap
					.computeIfAbsent(workshift.getUser().getId(), k -> new HashSet<>())
					.add(workshift.getDate());
		}

		// Mapa de Últimos Turnos: UserID -> ShiftType
		Map<Long, ShiftType> lastShiftMap = latestWorkshiftsForUsers.stream()
				.collect(Collectors.toMap(
						ws -> ws.getUser().getId(),
						Workshift::getShiftType,
						(existing, replacement) -> existing
				));

		List<LocalDate> days = workDaysOnly
				? getWorkDaysBetween(start, end)
				: getAllDaysBetween(start, end);

		// CONSTRUCCIÓN DEL CONTEXTO INMUTABLE
		return PlanningContext.builder()
				.workingDays(days)
				.operators(operators)
				.activeShifts(shifts)
				.occupiedDatesMap(occupiedDatesMap)
				.latestShiftTypeMap(lastShiftMap)
				.build();
	}


	private List<Workshift> calculateRotation(PlanningContext context) {
		List<Workshift> workshiftsToSave = new ArrayList<>();

		for (User operator : context.getOperators()) {
			// Recuperamos estado inicial de memoria (Map), no de DB
			ShiftType lastShiftType = context.getLastShiftType(operator.getId());

			for (LocalDate date : context.getWorkingDays()) {
				Shift nextShift = getNextShift(context.getActiveShifts(), lastShiftType);

				if (!context.hasCollision(operator.getId(), date)) {
					Workshift newWorkshift = Workshift.create(operator, nextShift, date);
					workshiftsToSave.add(newWorkshift);
				} else {
					log.debug("Omitiendo turno para usuario {} en {}: Ya ocupado.", operator.getId(), date);
				}

				lastShiftType = nextShift.getShiftType();
			}
		}
		return workshiftsToSave;
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

	private Workshift getWorkshiftEntityById(Long id) {
		return workshiftRepository.findById(id)
				.orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "Asignación de turno no encontrada con ID: " + id, HttpStatus.NOT_FOUND));
	}

	private void validateNoConflict(User user, LocalDate date, Shift shift, Long currentWorkhiftId) {
		// Regla: "Un turno por día"
		boolean exists = workshiftRepository.existsByUserAndDateAndIdNot(user, date, currentWorkhiftId);

		if (exists) {
			throw new WorkshiftServiceException(CustomErrorCode.CONFLICT,
					"El usuario " + user.getEmployeeNumber() + " ya tiene un turno asignado para la fecha " + date,
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * Elimina la clave Redis {@code workshift:user:{userId}:date:{date}}.
	 * Un fallo de Redis no propaga excepción: la BD sigue siendo la fuente de verdad.
	 */
	private void evictRedisKey(Long userId, LocalDate date) {
		try {
			String key = String.format(WorkshiftCacheConstants.KEY_PATTERN, userId, date.toString());
			workshiftRedisTemplate.delete(key);
			log.debug("Redis evictado: {}", key);
		} catch (Exception e) {
			log.error("Error evictando Redis key (userId={}, date={}): {}", userId, date, e.getMessage());
		}
	}

	/**
	 * Escribe las claves Redis {@code workshift:user:{userId}:date:{date} → workshiftId}
	 * para cada turno de la lista. El TTL es de 24 horas.
	 * <p>
	 * Cada clave se escribe de forma independiente: un fallo en Redis no aborta la
	 * transacción ni impide que los datos queden persistidos en MySQL.
	 */
	private void cacheWorkshiftsInRedis(List<Workshift> workshifts) {
		int cached = 0;
		for (Workshift ws : workshifts) {
			try {
				String key = String.format(WorkshiftCacheConstants.KEY_PATTERN,
						ws.getUser().getId(), ws.getDate().toString());
				workshiftRedisTemplate.opsForValue().set(key, ws.getId(), WorkshiftCacheConstants.TTL);
				cached++;
			} catch (Exception e) {
				log.error("Error poblando Redis para workshift {} (user={}, date={}): {}",
						ws.getId(), ws.getUser().getId(), ws.getDate(), e.getMessage());
			}
		}
		log.info("Redis: {}/{} claves de turno escritas.", cached, workshifts.size());
	}

	private List<LocalDate> getWorkDaysBetween(LocalDate startDate, LocalDate endDate) {
		return startDate.datesUntil(endDate.plusDays(1))
				.filter(date -> {
					DayOfWeek day = date.getDayOfWeek();
					return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
				})
				.toList();
	}

	private List<LocalDate> getAllDaysBetween(LocalDate startDate, LocalDate endDate) {
		return startDate.datesUntil(endDate.plusDays(1)).toList();
	}

}