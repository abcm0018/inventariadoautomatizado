package com.abcm0018.sai.workshift.application.service.impl;

import com.abcm0018.sai.shared.constants.CustomErrorCode;
import com.abcm0018.sai.users.domain.enums.Role;
import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.workshift.domain.enums.SwapRequestStatus;
import com.abcm0018.sai.workshift.domain.entity.WorkshiftSwapRequest;
import com.abcm0018.sai.workshift.domain.entity.Workshift;
import com.abcm0018.sai.workshift.domain.repository.WorkshiftSwapRequestRepository;
import com.abcm0018.sai.workshift.domain.repository.WorkshiftRepository;
import com.abcm0018.sai.workshift.exceptions.WorkshiftServiceException;
import com.abcm0018.sai.workshift.application.service.WorkshiftSwapRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkshiftSwapRequestServiceImpl implements WorkshiftSwapRequestService {

    private final WorkshiftRepository workshiftRepository;
    private final WorkshiftSwapRequestRepository workshiftChangeRequestRepository;

	// Constantes de negocio
	private static final int MAX_PENDING_REQUESTS_PER_USER = 3;
	private static final int MAX_DAYS_TO_CANCEL = 1; // Días antes del turno para cancelar
	private static final int OVERDUE_THRESHOLD_DAYS = 7; // Días para considerar solicitud vencida

	@Override
	public WorkshiftSwapRequest findById(Long id) {
		return workshiftChangeRequestRepository.findById(id).orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "Request not found", HttpStatus.NOT_FOUND));
	}

	@Override
	@Transactional
	public WorkshiftSwapRequest createSwapRequest(User user, Long currentWorkshiftId, Long requestedWorkshiftId, String reason) {

		log.info("Creando solicitud de cambio de horario - Usuario: {}, Turno actual: {}, Turno solicitado: {}", user.getId(), currentWorkshiftId, requestedWorkshiftId);

		validateUserCanRequestSwap(user);

		Workshift currentWorkshift = workshiftRepository.findById(currentWorkshiftId)
				.orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "Current workshift not found", HttpStatus.NOT_FOUND));

		Workshift requestedWorkshift = workshiftRepository.findById(requestedWorkshiftId)
				.orElseThrow(() -> new WorkshiftServiceException(CustomErrorCode.NOT_FOUND, "Current workshift not found", HttpStatus.NOT_FOUND));

		validateSwapRequest(user, currentWorkshift, requestedWorkshift);
		validateNoDuplicateRequest(user, currentWorkshift, requestedWorkshift);
		validateReason(reason);

		// Crear la solicitud de cambio de turno
		WorkshiftSwapRequest swapRequest = WorkshiftSwapRequest.builder()
				.user(user)
				.currentWorkshift(currentWorkshift)
				.requestedWorkshift(requestedWorkshift)
				.reason(reason.trim())
				.status(SwapRequestStatus.PENDING)
				.build();

		WorkshiftSwapRequest saved = workshiftChangeRequestRepository.save(swapRequest);
		log.info("Solicitud de cambio de turno creada - Usuario: {}, Turno actual: {}, Turno solicitado: {}", user.getId(), currentWorkshiftId, requestedWorkshiftId);

		// TODO: Enviar notificación a supervisores/administradores
		return saved;
	}

	@Override
	public WorkshiftSwapRequest approveRequest(Long requestId, User reviewer, String notes) {
		log.info("Aprobando solicitud {} por revisor {}", requestId, reviewer.getId());

		validateReviewerPermissions(reviewer);

		WorkshiftSwapRequest request = findById(requestId);

		if (!request.isPending()) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "La solicitud no se encuentra en estado pendiente", HttpStatus.BAD_REQUEST);
		}

		// Validar que el turno solicitado sigue disponible
		validateWorkshiftAvailability(request.getRequestedWorkshift());

		// Aprobar la solicitud
		request.approve(reviewer, notes);
		WorkshiftSwapRequest approved = workshiftChangeRequestRepository.save(request);

		// Realizar el intercambio de turnos
		performWorkshiftSwap(request);

		log.info("Solicitud {} aprobada exitosamente", requestId);

		return approved;
	}

	@Override
	public WorkshiftSwapRequest rejectRequest(Long requestId, User reviewer, String notes) {
		log.info("Rechazando solicitud {} por revisor {}", requestId, reviewer.getId());

		validateReviewerPermissions(reviewer);

		if (StringUtils.isEmpty(notes)) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "Debe proporcionar una razón para el rechazo", HttpStatus.BAD_REQUEST);
		}

		WorkshiftSwapRequest request = findById(requestId);

		if (!request.isPending()) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "La solicitud no se encuentra en estado pendiente", HttpStatus.BAD_REQUEST);
		}

		// Rechazar la solicitud
		request.reject(reviewer, notes);
		WorkshiftSwapRequest rejected = workshiftChangeRequestRepository.save(request);

		log.info("Solicitud {} rechazada exitosamente", requestId);

		// TODO: Enviar notificación al usuario solicitante

		return rejected;
	}

	@Override
	public WorkshiftSwapRequest cancelRequest(Long requestId, User requester) {
		log.info("Usuario {} intentando cancelar solicitud {}", requester.getId(), requestId);

		WorkshiftSwapRequest swapRequest = findById(requestId);

		if (!swapRequest.getUser().getId().equals(requester.getId())) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "Solo el solicitante puede cancelar esta solicitud", HttpStatus.BAD_REQUEST);
		}

		if (!swapRequest.isPending()) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "Solo se pueden cancelar solicitudes pendientes", HttpStatus.BAD_REQUEST);
		}

		// Validar que aún hay tiempo para cancelar la solicitud
		validateCancellationDeadline(swapRequest);

		// Cancelar la solicitud
		swapRequest.cancel(requester);
		// TODO: Enviar una notificación al usuario solicitante indicando que la solicitud fue cancelada
		WorkshiftSwapRequest cancelled = workshiftChangeRequestRepository.save(swapRequest);

		log.info("Solicitud {} cancelada exitosamente", requestId);
		return cancelled;
	}

	@Override
	public void deleteRequest(Long requestId, User admin) {
		log.info("Administrador {} eliminando solicitud {}", admin.getId(), requestId);

		if (!admin.getRole().equals(Role.ADMIN)) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "Solo el administrador puede eliminar solicitudes", HttpStatus.BAD_REQUEST);
		}

		WorkshiftSwapRequest swapRequest = findById(requestId);

		workshiftChangeRequestRepository.delete(swapRequest);

		log.info("Solicitud {} eliminada exitosamente", requestId);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean hasPendingRequests(User user) {
		return countPendingRequestsByUser(user) > 0;
	}

	@Override
	@Transactional(readOnly = true)
	public Long countPendingRequestsByUser(User user) {
		return workshiftChangeRequestRepository.countByUserAndStatus(user, SwapRequestStatus.PENDING);
	}

	@Override
	@Transactional(readOnly = true)
	public Long countRequestsByUser(User user) {
		return workshiftChangeRequestRepository.countByUser(user);
	}

	@Override
	@Transactional(readOnly = true)
	public List<WorkshiftSwapRequest> findOverdueRequests() {
		LocalDateTime thresholdDate = LocalDateTime.now().minusDays(OVERDUE_THRESHOLD_DAYS);
		return workshiftChangeRequestRepository.findOverdueRequests(thresholdDate);
	}

	@Override
	public List<WorkshiftSwapRequest> findReviewedBy(User reviewer) {
		return workshiftChangeRequestRepository.findByReviewedByOrderByReviewedAtDesc(reviewer);
	}

	private void performWorkshiftSwap(WorkshiftSwapRequest request) {
		log.info("Realizando intercambio de turnos para solicitud {}", request.getId());

		Workshift currentWorkshift = request.getCurrentWorkshift();
		Workshift requestedWorkshift = request.getRequestedWorkshift();
		User requestingUser = request.getUser();
		User targetUser = requestedWorkshift.getUser();

		// Intercambiar usuarios en los turnos
		currentWorkshift.setUser(targetUser);
		requestedWorkshift.setUser(requestingUser);

		workshiftRepository.save(currentWorkshift);
		workshiftRepository.save(requestedWorkshift);

		log.info("Intercambio de turnos completado - Usuario {} obtiene turno {} y Usuario {} obtiene turno {}",
				requestingUser.getId(), requestedWorkshift.getId(), targetUser.getId(), currentWorkshift.getId());

		// TODO: Enviar notificaciones a ambos usuarios.
	}

	// ========== VALIDACIONES PRIVADAS ==========

	private void validateCancellationDeadline(WorkshiftSwapRequest swapRequest) {
		Workshift currentWorkshift = swapRequest.getCurrentWorkshift();

		if (currentWorkshift.isToday()) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "No se puede cancelar una solicitud el mismo día del turno", HttpStatus.BAD_REQUEST);
		}

		// Validar días mínimos antes del turno
		long daysUntilWorkshift = ChronoUnit.DAYS.between(LocalDate.now(), currentWorkshift.getDate());

		if (daysUntilWorkshift < MAX_DAYS_TO_CANCEL) {
			String mensaje = String.format("No se puede cancelar con menos de %d día(s) de anticipación", MAX_DAYS_TO_CANCEL);
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, mensaje, HttpStatus.BAD_REQUEST);
		}
	}

	private void validateWorkshiftAvailability(Workshift workshift) {
		if (workshift.isPast()) {
			throw new WorkshiftServiceException(CustomErrorCode.CONFLICT, "El turno solicitado ya ha pasado y no está disponible", HttpStatus.CONFLICT);
		}

		// Verificar si hay solicitudes conflictivas aprobadas
		List<WorkshiftSwapRequest> conflictingRequests = workshiftChangeRequestRepository.findByWorkshiftAndStatus(workshift, SwapRequestStatus.APPROVED);

		if (!conflictingRequests.isEmpty()) {
			throw new WorkshiftServiceException(CustomErrorCode.CONFLICT, "Ya hay solicitudes aprobadas para este turno", HttpStatus.CONFLICT);
		}
	}

	/**
	 * Verifica que el usuario tenga permisos de revisor
	 */
	private void validateReviewerPermissions(User reviewer) {
		if (reviewer.getRole() != Role.SUPERVISOR && reviewer.getRole() != Role.ADMIN) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "El usuario no tiene permisos de revisor", HttpStatus.BAD_REQUEST);
		}

		if (!reviewer.isActive()) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "El usuario no puede ser revisor", HttpStatus.BAD_REQUEST);
		}
	}

	private void validateReason(String reason) {
		if (StringUtils.isEmpty(reason)) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "La razón es obligatoria", HttpStatus.BAD_REQUEST);
		}

		if (reason.length() < 10) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "El motivo debe tener al menos 10 caracteres", HttpStatus.BAD_REQUEST);
		}

		if (reason.length() > 500) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "El motivo no puede exceder los 500 caracteres", HttpStatus.BAD_REQUEST);
		}
	}

	private void validateNoDuplicateRequest(User user, Workshift currentWorkshift, Workshift workshiftRequested) {
		boolean exists = workshiftChangeRequestRepository.existsPendingDuplicateRequest(user, currentWorkshift, workshiftRequested);
		if (exists) {
			throw new WorkshiftServiceException(CustomErrorCode.CONFLICT, "Ya existe una solicitud de cambio de horario pendiente", HttpStatus.CONFLICT);
		}
	}

	private void validateSwapRequest(User user, Workshift currentWorkshift, Workshift requestedWorkshift) {
		// Verifica que el turno actual pertence al usuario
		if (!currentWorkshift.getUser().getId().equals(user.getId())) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "El turno actual no pertenece al usuario", HttpStatus.BAD_REQUEST);
		}

		// Validar que los turnos son diferentes
		if (currentWorkshift.getId().equals(requestedWorkshift.getId())) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "Los turnos son iguales", HttpStatus.BAD_REQUEST);
		}

		// Validar que el turno actual no haya pasado
		if (currentWorkshift.isPast()) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "No se puede cambiar turno pasado", HttpStatus.BAD_REQUEST);
		}

		// Validar que el turno solicitado no haya pasado
		if (requestedWorkshift.isPast()) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "No se puede solicitar un turno pasado", HttpStatus.BAD_REQUEST);
		}

		// Validar que hay tiempo suficiente antes del turno
		if (currentWorkshift.isToday()) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "No se puede solicitar el cambio el mismo día del turno", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Verifica que el usuario pueda crear nuevas solicitudes
	 */
	private void validateUserCanRequestSwap(User user) {
		if (!user.isActive()) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "El usuario no puede solicitar cambios de turno", HttpStatus.BAD_REQUEST);
		}

		if (user.isBlocked()) {
			throw new WorkshiftServiceException(CustomErrorCode.BAD_REQUEST, "El usaurio no puede solicitar cambios de turno", HttpStatus.BAD_REQUEST);
		}

		// Verificar límite de solicitudes pendientes
		Long pendingCount = countPendingRequestByUser(user);
		if (pendingCount >= MAX_PENDING_REQUESTS_PER_USER) {
			String mensaje = String.format("El usuario ya tiene %d solicitudes pendientes. Máximo permitido: %d", pendingCount, MAX_PENDING_REQUESTS_PER_USER);
			throw new WorkshiftServiceException(CustomErrorCode.CONFLICT, mensaje, HttpStatus.CONFLICT);
		}
	}

	private Long countPendingRequestByUser(User user) {
		return workshiftChangeRequestRepository.countByUser(user);
	}
}
