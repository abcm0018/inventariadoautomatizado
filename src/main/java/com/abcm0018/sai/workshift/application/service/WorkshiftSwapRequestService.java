package com.abcm0018.sai.workshift.application.service;

import java.util.List;

import com.abcm0018.sai.users.domain.entity.User;
import com.abcm0018.sai.workshift.domain.entity.WorkshiftSwapRequest;

public interface WorkshiftSwapRequestService {

	WorkshiftSwapRequest findById(Long id);

	WorkshiftSwapRequest createSwapRequest(User user, Long currentWorkshiftId, Long requestedWorkshiftId, String reason);

	WorkshiftSwapRequest approveRequest(Long requestId, User reviewer, String notes);

	WorkshiftSwapRequest rejectRequest(Long requestId, User reviewer, String notes);

	/**
	 * Cancela una solicitud de cambio de turno (Solo por el usuario que la creó
	 * @param requestId - ID de la solicitud
	 * @param requester - Usuario que realiza la solicitud
	 * @return - Solicitud cancelada
	 */
	WorkshiftSwapRequest cancelRequest(Long requestId, User requester);

	/**
	 * Elimina una solicitud de cambio de turno. Solo por un administrador
	 * @param requestId - ID de la solicitud
	 * @param admin - Usuario que realiza la solicitud
	 */
	void deleteRequest(Long requestId, User admin);

	boolean hasPendingRequests(User user);

	Long countPendingRequestsByUser(User user);

	Long countRequestsByUser(User user);

	/**
	 * Encuentra solicitudes que han caducado
	 * @return - Lista de solicitudes caducadas
	 */
	List<WorkshiftSwapRequest> findOverdueRequests();

	/**
	 * Encuentra solicitudes revisadas por un supervisor/administrador
	 */
	List<WorkshiftSwapRequest> findReviewedBy(User reviewer);
}
