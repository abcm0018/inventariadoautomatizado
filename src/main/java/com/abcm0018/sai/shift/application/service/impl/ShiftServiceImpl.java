package com.abcm0018.sai.shift.application.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.abcm0018.sai.shared.constants.CustomErrorCode;
import com.abcm0018.sai.shift.application.dtos.ShiftRequestDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftResponseDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftStatusChangeDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftSummaryDTO;
import com.abcm0018.sai.shift.application.mapper.IShiftMapper;
import com.abcm0018.sai.shift.domain.entity.Shift;
import com.abcm0018.sai.shift.domain.repository.ShiftRepository;
import com.abcm0018.sai.shift.domain.enums.ShiftType;
import com.abcm0018.sai.shift.exceptions.ShiftServiceException;
import com.abcm0018.sai.shift.application.service.ShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

	private final IShiftMapper shiftMapper;
    private final ShiftRepository shiftRepository;

	// Constantes de validación
	private static final int MIN_SHIFT_DURATION_HOURS = 4;
	private static final int MAX_SHIFT_DURATION_HOURS = 12;

	@Override
	@Transactional
	@CacheEvict(value = "shifts", allEntries = true)
	public ShiftResponseDTO createShift(ShiftRequestDTO request) {
		log.info("Creando nuevo turno - Tipo: {}, Horario: {} - {}", request.getShiftType(), request.getStartTime(), request.getEndTime());

		// Validaciones
		validateShiftTimes(request.getStartTime(), request.getEndTime());
		validateUniqueShiftType(request.getShiftType());
		validateNoOverlap(request.getStartTime(), request.getEndTime(), null);

		// Convertir DTO a entidad
		Shift shift = shiftMapper.toShift(request);

		// Guardar en BD
		Shift saved = shiftRepository.save(shift);

		log.info("Turno creado exitosamente - ID: {} Tipo: {}", saved.getId(), saved.getShiftType());

		return convertToResponseWithDetails(saved);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "shifts", key = "#id", unless = "#result == null")
	public ShiftResponseDTO findById(Long id) {
		return convertToResponseWithDetails(getShiftEntityById(id));
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "shifts", key = "'type-' + #shiftType", unless = "#result == null")
	public ShiftResponseDTO findByShiftType(ShiftType shiftType) {
		Shift shift = shiftRepository.findByShiftType(shiftType)
				.orElseThrow(() -> new ShiftServiceException(CustomErrorCode.NOT_FOUND, "Turno no encontrado con tipo: " + shiftType, HttpStatus.NOT_FOUND));

		return convertToResponseWithDetails(shift);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "shifts", key = "'all-shifts'")
	public List<ShiftSummaryDTO> findAll() {
		return shiftMapper.toSummaryDTOList(shiftRepository.findAll());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "shifts", key = "'all-ordered'")
	public List<ShiftSummaryDTO> findAllOrderedByStartTime() {
		return shiftMapper.toSummaryDTOList(shiftRepository.findAllByOrderByStartTimeAsc());
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "shifts", key = "#id"),
			@CacheEvict(value = "shifts", allEntries = true)
	})
	public ShiftResponseDTO updateShift(Long id, ShiftRequestDTO requestDTO) {
		log.info("Actualizando turno {}", id);

		Shift existingShift = getShiftEntityById(id);

		// Validar cambio de tipo
		if (!existingShift.getShiftType().equals(requestDTO.getShiftType())) {
			validateUniqueShiftType(requestDTO.getShiftType());
		}

		// Validar nuevo horario
		validateShiftTimes(requestDTO.getStartTime(), requestDTO.getEndTime());
		validateNoOverlap(requestDTO.getStartTime(), requestDTO.getEndTime(), id);

		// Actualizar usando el mapper
		shiftMapper.updateEntityFromRequest(requestDTO, existingShift);

		Shift saved = shiftRepository.save(existingShift);

		log.info("Turno {} actualizado exitosamente", id);

		return convertToResponseWithDetails(saved);
	}

	@Override
	@Transactional
	@CacheEvict(value = "shifts", allEntries = true)
	public void deleteShift(Long id) {
		log.info("Desactivando turno {}", id);

		Shift shift = getShiftEntityById(id);

		// Validar que no tenga workshifts asignados activos
		validateCanDeactivate(shift);

		shift.setActive(false);
		shiftRepository.save(shift);

		log.info("Turno {} desactivado exitosamente", id);
	}

	@Override
	@Transactional
	@CacheEvict(value = "shifts", allEntries = true)
	public void permanentlyDeleteShift(Long id) {
		log.warn("Eliminando permanentemente turno {}", id);

		Shift shift = getShiftEntityById(id);

		// Validar que no tenga workshifts asignados
		if (shiftRepository.hasAssignedWorkshifts(id)) {
			Long count = shiftRepository.countAssignedWorkshifts(id);
			String message = String.format(
					"No se puede eliminar el turno porque tiene %d workshifts asignados", count);
			throw new ShiftServiceException(
					CustomErrorCode.CONFLICT, message, HttpStatus.CONFLICT);
		}

		shiftRepository.delete(shift);

		log.warn("Turno {} eliminado permanentemente", id);
	}

	@Override
	@Transactional
	@CacheEvict(value = "shifts", allEntries = true)
	public ShiftResponseDTO activateShift(Long id) {
		log.info("Activando turno {}", id);

		Shift shift = getShiftEntityById(id);
		shift.setActive(true);

		Shift updated = shiftRepository.save(shift);

		log.info("Turno {} activado exitosamente", id);

		return convertToResponseWithDetails(updated);
	}

	@Override
	@Transactional
	@CacheEvict(value = "shifts", allEntries = true)
	public ShiftResponseDTO deactivateShift(Long id, ShiftStatusChangeDTO statusChangeDTO) {
		log.info("Desactivando turno {} - Razón: {}", id, statusChangeDTO.getReason());

		Shift shift = getShiftEntityById(id);

		// Validar razón si se requiere
		if (statusChangeDTO.getReason() == null || statusChangeDTO.getReason().trim().isEmpty()) {
			throw new ShiftServiceException(CustomErrorCode.BAD_REQUEST, "Debe proporcionar una razón para desactivar el turno", HttpStatus.BAD_REQUEST);
		}

		validateCanDeactivate(shift);

		shift.setActive(false);
		Shift updated = shiftRepository.save(shift);

		log.info("Turno {} desactivado - Razón: {}", id, statusChangeDTO.getReason());

		// TODO: Registrar en auditoría

		return convertToResponseWithDetails(updated);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "shifts", key = "'active-shifts'")
	public List<ShiftSummaryDTO> findAllActiveShifts() {
		return shiftMapper.toSummaryDTOList(shiftRepository.findByActiveTrue());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "shifts", key = "'active-ordered'")
	public List<ShiftSummaryDTO> findActiveShiftsOrdered() {
		return shiftMapper.toSummaryDTOList(shiftRepository.findAllActiveOrderedByStartTime());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "shifts", key = "'inactive-shifts'")
	public List<ShiftSummaryDTO> findInactiveShifts() {
		return shiftMapper.toSummaryDTOList(shiftRepository.findByActiveFalse());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "shifts", key = "'count-active'")
	public Long countActiveShifts() {
		return shiftRepository.countByActiveTrue();
	}

	/**
	 * Buscar turno que contiene una hora específica
	 */
	@Override
	@Transactional(readOnly = true)
	public ShiftResponseDTO findShiftAtTime(LocalTime time) {
		Shift shift = shiftRepository.findShiftContainingTime(time)
				.orElseThrow(() -> new ShiftServiceException(CustomErrorCode.NOT_FOUND, "No se encontró ningún turno activo que contenga la hora: " + time, HttpStatus.NOT_FOUND));

		return convertToResponseWithDetails(shift);
	}

	/**
	 * Obtener turnos que cruzan la medianoche
	 */
	@Override
	@Transactional(readOnly = true)
	@Cacheable(value = "shifts", key = "'crossing-midnight'")
	public List<ShiftSummaryDTO> findShiftsCrossingMidnight() {
		return shiftMapper.toSummaryDTOList(shiftRepository.findShiftsCrossingMidnight());
	}

	/**
	 * Buscar turno activo actual (hora del sistema)
	 */
	@Override
	@Transactional(readOnly = true)
	public ShiftResponseDTO getCurrentShift() {
		return findShiftAtTime(LocalTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public List<ShiftSummaryDTO> findShiftsByDuration(long hours) {
		List<Shift> shifts = shiftRepository.findAllForDurationFilter().stream()
				.filter(shift -> shift.getDurationInHours() == hours).toList();

		return shiftMapper.toSummaryDTOList(shifts);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ShiftSummaryDTO> findShiftsWithMinDuration(long minHours) {
		List<Shift> shifts = shiftRepository.findAllForDurationFilter().stream()
				.filter(shift -> shift.getDurationInHours() >= minHours).toList();

		return shiftMapper.toSummaryDTOList(shifts);
	}

	/**
	 * Verificar si existe un turno por tipo
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean existsByShiftType(ShiftType shiftType) {
		return shiftRepository.existsByShiftType(shiftType);
	}

	/**
	 * Crear turnos por defecto del sistema
	 * Útil para inicializar la aplicación
	 */
	@Override
	@Transactional
	@Caching(
			evict = {
					@CacheEvict(cacheNames = "shifts_all", allEntries = true),
					@CacheEvict(cacheNames = "shifts_active", allEntries = true),
					@CacheEvict(cacheNames = "shifts_inactive", allEntries = true),
					@CacheEvict(cacheNames = "shift_statistics", allEntries = true)
			}
	)
	public void createDefaultShifts() {
		log.info("Creando turnos por defecto del sistema");

		// Obtenemos todos los tipos de turno que YA existen en la base de datos.
		Set<ShiftType> existingTypes = shiftRepository.findAllShiftTypes();

		// 2. COMPROBAR EN MEMORIA (Sin BBDD)
		List<ShiftRequestDTO> shiftRequestDTOS = new ArrayList<>();

		// MAÑANA: 07:00 - 15:00
		if (!existingTypes.contains(ShiftType.MORNING)) {
			shiftRequestDTOS.add(ShiftRequestDTO.builder()
					.shiftType(ShiftType.MORNING)
					.startTime(LocalTime.of(7, 0))
					.endTime(LocalTime.of(15, 0))
					.description("Turno estándar de mañana (07:00 - 15:00)")
					.active(true)
					.build());
		}

		// TARDE: 15:00 - 23:00
		if (!existingTypes.contains(ShiftType.AFTERNOON)) {
			shiftRequestDTOS.add(ShiftRequestDTO.builder()
					.shiftType(ShiftType.AFTERNOON)
					.startTime(LocalTime.of(15, 0))
					.endTime(LocalTime.of(23, 0))
					.description("Turno estándar de tarde (15:00 - 23:00)")
					.active(true)
					.build());
		}

		// NOCHE: 23:00 - 07:00
		if (!existingTypes.contains(ShiftType.NIGHT)) {
			shiftRequestDTOS.add(ShiftRequestDTO.builder()
					.shiftType(ShiftType.NIGHT)
					.startTime(LocalTime.of(23, 0))
					.endTime(LocalTime.of(7, 0)) // Cruza la medianoche
					.description("Turno estándar de noche (23:00 - 07:00)")
					.active(true)
					.build());
		}

		if (shiftRequestDTOS.isEmpty()) {
			log.info("La base de datos ya contiene todos los turnos por defecto. No se requiere poblamiento.");
			return;
		}

		try {

			List<Shift> shiftsToSave = shiftRequestDTOS.stream()
					.map(dto -> {
						validateShiftTimes(dto.getStartTime(), dto.getEndTime());

						Shift newShift = shiftMapper.toShift(dto);

						if (dto.getActive() == null) {
							newShift.setActive(true);
						}

						return newShift;
					}).toList();

			shiftRepository.saveAll(shiftsToSave);

			log.info("✅ Población de turnos por defecto completada.");
		} catch (Exception e) {
			log.error("❌ Error durante la creación de turnos por defecto: {}", e.getMessage(), e);
			throw new ShiftServiceException(
					CustomErrorCode.INTERNAL_SERVER_ERROR,
					"Error interno al crear los turnos por defecto: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		log.info("Turnos por defecto creados exitosamente");
	}

	/**
	 * Obtiene una entidad Shift por ID
	 * Método interno para evitar conversión a DTO
	 */
	private Shift getShiftEntityById(Long id) {
		return shiftRepository.findById(id)
				.orElseThrow(() -> new ShiftServiceException(CustomErrorCode.NOT_FOUND, "Turno no encontrado con ID: " + id, HttpStatus.NOT_FOUND));
	}

	/**
	 * Convierte Shift a ShiftResponseDTO con información adicional
	 */
	private ShiftResponseDTO convertToResponseWithDetails(Shift shift) {
		Long totalWorkshifts = shiftRepository.countAssignedWorkshifts(shift.getId());
		Boolean canBeDeleted = totalWorkshifts == 0;
		return shiftMapper.toResponseWithDetails(shift, totalWorkshifts, canBeDeleted);
	}

	private void validateNoOverlap(LocalTime startTime, LocalTime endTime, Long excludeId) {
		Long excludeIdSafe = excludeId != null ? excludeId : -1L;

		boolean hasOverlap = shiftRepository.existsOverlappingShift(startTime, endTime, excludeIdSafe);

		if (hasOverlap) {
			throw new ShiftServiceException(CustomErrorCode.BAD_REQUEST,
					"El horario del turno se solapa con otro turno activo existente", HttpStatus.BAD_REQUEST);
		}
	}

	private void validateUniqueShiftType(ShiftType shiftType) {
		if (shiftRepository.existsByShiftType(shiftType)) {
			throw new ShiftServiceException(CustomErrorCode.NOT_FOUND, "Ya existe un turno con el tipo: " + shiftType, HttpStatus.NOT_FOUND);
		}
	}

	private void validateShiftTimes(LocalTime startTime, LocalTime endTime) {
		Duration duration = Duration.between(startTime, endTime);

		// Si el turno cruza la medianoche (ej: 22:00 - 06:00), la duración será negativa
		if (duration.isNegative()) {
			duration = duration.plusDays(1); // O plusHours(24)
		}

		long durationInHours = duration.toHours();

		if (durationInHours < MIN_SHIFT_DURATION_HOURS) {
			String msg = String.format("La duración del turno debe ser al menos %d horas", MIN_SHIFT_DURATION_HOURS);
			throw new ShiftServiceException(CustomErrorCode.BAD_REQUEST, msg, HttpStatus.BAD_REQUEST);
		}

		if (durationInHours > MAX_SHIFT_DURATION_HOURS) {
			String msg = String.format("La duración del turno no puede exceder %d horas", MAX_SHIFT_DURATION_HOURS);
			throw new ShiftServiceException(CustomErrorCode.BAD_REQUEST, msg, HttpStatus.BAD_REQUEST);
		}
	}

	private void validateCanDeactivate(Shift shift) {
		// Obtenemos solo los turnos asignados de hoy en adelante
		Long futureAssignments = shiftRepository.countFutureAssignedWorkshifts(shift.getId(), LocalDate.now());

		if (futureAssignments > 0) {
			String msg = String.format("No se puede desactivar el turno '%s' porque tiene %d asignaciones de trabajo futuras.",
					shift.getShiftType().getDisplayName(), futureAssignments);

			// En lugar de un warning, lanzamos un error
			throw new ShiftServiceException(CustomErrorCode.CONFLICT, msg, HttpStatus.CONFLICT);
		}
	}
}
