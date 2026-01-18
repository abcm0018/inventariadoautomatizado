package com.abcm0018.sai.workshift.application.service.impl;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
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

	// Constantes de negocio
	private static final int DATA_RETENTION_MONTHS = 12;

	// --- CONSTANTES (copiadas de WorkshiftCacheScheduler) ---
	private static final String WORKSHIFT_CACHE_KEY_PATTERN = "workshift:user:%d:date:%s";
	private static final Duration CACHE_TTL = Duration.ofHours(24);

	@Override
	@Transactional
	public Integer generateWorkshiftSchedule(CreateWorkshiftRequestDTO requestDTO) {

		if (Objects.isNull(requestDTO)) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "Fecha inicio y fecha fin son obligatorias", HttpStatus.BAD_REQUEST);
		}

		if (requestDTO.getFromDate().isAfter(requestDTO.getToDate())) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "Fecha inicio no puede ser mayor a fecha fin", HttpStatus.BAD_REQUEST);
		}

		log.info("Preparando el contexto de planificación con fecha inicio {}  y fecha fin: {}", requestDTO.getFromDate(), requestDTO.getToDate());

		// 1. Preparar contexto
		PlanningContext context = preparePlanningContext(requestDTO.getFromDate(), requestDTO.getToDate());

		log.info("Planificando turnos para {} operarios usando para {} tipos de turnos.",
				context.getOperators().size(), context.getActiveShifts().size());

		// 2. Calulamos los turnos a asignar
		List<Workshift> workshiftsToSave = calculateRotation(context);

		// 3. Persistencia por Lotes (Batch)
		if (!workshiftsToSave.isEmpty()) {
			log.info("Guardando {} nuevos turnos...", workshiftsToSave.size());
			workshiftRepository.saveAll(workshiftsToSave);
			return workshiftsToSave.size();
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

		if (StringUtils.isEmpty(requestDTO.getReason())) {
			throw new WorkshiftServiceException(
					CustomErrorCode.BAD_REQUEST,
					"Debe proporcionar un motivo para actualizar un turno de forma manual", HttpStatus.BAD_REQUEST);
		}

		Workshift workshift = getWorkshiftEntityById(id);

		// Check de concurrencia
		// Comporamos la version de la BD con la versión que tenía el usuario en su pantalla
		if (!workshift.getVersion().equals(requestDTO.getVersion())) {
			throw new WorkshiftServiceException(CustomErrorCode.CONFLICT,
					"El turno ha sido modificado por otro usuario miestras estabas editando",
					HttpStatus.CONFLICT);
		}

		Shift newShift = null;

		if (requestDTO.getShiftId() != null) {
			newShift = shiftRepository
					.findById(requestDTO.getShiftId())
					.orElseThrow(() -> new WorkshiftServiceException(
							CustomErrorCode.NOT_FOUND, "No se ha encontrado el turno", HttpStatus.NOT_FOUND)
					);

			workshift.setShift(newShift);
		}

		if (requestDTO.getDate() != null) {
			validateNoConflict(workshift.getUser(), requestDTO.getDate(), newShift, id);
			workshift.setDate(requestDTO.getDate());
		}

		Workshift updated = workshiftRepository.save(workshift);

		log.warn("Workshift {} actualizado manualmente | Motivo: {}", id, requestDTO.getReason());

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
		int total = 1;
		Workshift workshift;
		try {
			workshift = getWorkshiftEntityById(id);
		} catch (Exception e) {
			if (e instanceof WorkshiftServiceException && ((WorkshiftServiceException) e).getHttpStatus() == HttpStatus.NOT_FOUND) {
				return 0;
			}
			throw new RuntimeException(e);
		}

		// Validar que no tenga datos asociados
		if (workshift.getTotalTimesheets() > 0) {
			String message = String.format("No se puede eliminar el turno porque tiene %d fichajes asociados", workshift.getTotalTimesheets());
			throw new WorkshiftServiceException(CustomErrorCode.CONFLICT, message, HttpStatus.CONFLICT);
		}

		workshiftRepository.delete(workshift);
		log.info("Workshift {} eliminado exitosamente", id);
		return total;
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
			PlanningContext context = preparePlanningContext(nextMonday, nextSunday);

			List<Workshift> generatedWorkshifts = calculateRotation(context);

			if (generatedWorkshifts.isEmpty()) {
				log.warn("No se generaron turnos (Quizás ya existen?)");
				return;
			}

			workshiftRepository.saveAll(generatedWorkshifts);
			log.info("[CRON] ÉXITO: Generados {} turnos para {} operadores.", generatedWorkshifts.size(), context.getOperators().size());
		} catch (Exception e) {
			log.error("[CRON] ERROR CRÍTICO: Fallo al generar la planificación semanal.", e);
			throw e;
		}
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
			log.error("Error al leer de Redis (key: {}). Cayendo a BBDD. Error: {}", cacheKey, e.getMessage());
		}

		// 4. CACHE HIT
		if (workshiftId != null) {
			log.debug("Cache HIT para clave {}. Workshift ID: {}", cacheKey, workshiftId);
			Workshift workshift =  workshiftRepository.findById(workshiftId).orElseThrow(
					() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "No existe el turno ", HttpStatus.NOT_FOUND));

			return Optional.of(workshift);
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
			log.error("Error al repoblar Redis (key: {}) tras un cache miss. Error: {}", cacheKey, e.getMessage());
		}

		// 7. Devolver el DTO
		return foundShift != null ? Optional.of(foundShift) : Optional.empty();
	}

	private PlanningContext preparePlanningContext(LocalDate start, LocalDate end) {

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

		// CONSTRUCCIÓN DEL CONTEXTO INMUTABLE
		return PlanningContext.builder()
				.workingDays(getWorkDaysBetween(start, end)) // Calculamos días hábiles
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

	private void validateNoConflict(User user, LocalDate date, Shift shift, Long excludeId) {
		List<Workshift> existing = workshiftRepository.findPotentialConflicts(user, date, shift);

		if (excludeId != null) {
			existing = existing.stream().filter(w -> !w.getId().equals(excludeId)).toList();
		}

		if (!existing.isEmpty()) {
			log.warn("ADVERTENCIA: El usuario {} ya tiene {} turno(s) para la fecha {}", user.getId(), existing.size(), date);
		}
	}

	private List<LocalDate> getWorkDaysBetween(LocalDate startDate, LocalDate endDate) {
		return startDate.datesUntil(endDate.plusDays(1))
				.filter(date -> {
					DayOfWeek day = date.getDayOfWeek();
					return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
				}).toList();
	}

}