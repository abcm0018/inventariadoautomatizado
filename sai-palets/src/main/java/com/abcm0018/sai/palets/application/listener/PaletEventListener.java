package com.abcm0018.sai.palets.application.listener;

import java.util.Set;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import com.abcm0018.sai.palets.application.dtos.PaletNotificationDTO;
import com.abcm0018.sai.palets.application.mappers.PaletMapper;
import com.abcm0018.sai.palets.domain.PaletCreatedEvent;
import com.abcm0018.sai.palets.domain.entity.Palet;
import com.abcm0018.sai.palets.domain.entity.PalletScan;
import com.abcm0018.sai.palets.domain.repository.PaletRepository;
import com.abcm0018.sai.productos.domain.entity.Product;
import com.abcm0018.sai.productos.domain.entity.ProductPackLevel;
import com.abcm0018.sai.workshift.domain.entity.Workshift;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Escucha eventos del dominio 'palets' y los remite
 * a través de WebSocket
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaletEventListener {

	// Objeto de Spring para enviar mensajes STOMP
	private final SimpMessagingTemplate messagingTemplate;

	// Inyectamos el registro de usuarios y suscripciones de STOMP
	private final SimpUserRegistry simpUserRegistry;

	// Necesitamos el repositorio para buscar los datos completos del palet
	private final PaletRepository paletRepository;

	private final PaletMapper paletMapper;

	// El "Destino" (Topic) al que enviaremos los mensajes
	// Debe coincidir con el prefijo del bróker en WebSocketConfig
	private static final String WS_DESTINATION_TOPIC = "/topic/palets";

	// REQUIRES_NEW garantiza que este método siempre abre una transacción propia,
	// independientemente del estado del hilo (la transacción original ya committeó).
	// Sin esto, la lambda de ifPresentOrElse opera con la entidad detached y cualquier
	// acceso a una relación lazy lanza LazyInitializationException.
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(PaletCreatedEvent.class)
	public void handlePaletCreatedEvent(PaletCreatedEvent event) {

		Set<SimpSubscription> subscriptionsForTopic = simpUserRegistry
				.findSubscriptions(subscription -> subscription.getDestination().equals(WS_DESTINATION_TOPIC));

		if (subscriptionsForTopic.isEmpty()) {
			log.info("Evento 'Palet creado' ID: {}. No hay clientes conectados a {}. Omitiendo envío de WebSocket.",
					event.getPaletId(), WS_DESTINATION_TOPIC);

			// Salimos. No se hace NADA MÁS.
			// Esto ahorra la consulta a la BD y el envío.
			return;
		}

		log.info("Evento 'Palet creado' recibido. ID: {}. Preparando notificación WebSocket...", event.getPaletId());

		// 1. Buscamos el palet (con la consulta optimizada)
		try {
			paletRepository.findWithDetailsById(event.getPaletId())
					.ifPresentOrElse(
							palet -> {
								// 2. Creamos el DTO de notificación
								PaletNotificationDTO notification = paletMapper.toResponsePaletNotification(palet);
								messagingTemplate.convertAndSend(WS_DESTINATION_TOPIC, notification);
							}, () -> {
								log.warn("El evento de 'Palet creado' no pudo ser procesado. Palet no encontrado. ID: {}", event.getPaletId());
							});
		} catch (Exception e) {
			log.error("Error al procesar evento y enviar WebSocket: {}", e.getMessage(), e);
		}
	}

	/**
	 * Helper para construir el DTO enriquecido
	 */
	private PaletNotificationDTO buildNotificationDTO(PalletScan palletScan) {
		Palet pallet = palletScan.getPallet();
		ProductPackLevel packLevel = pallet.getProductPackLevel();
		Product product = packLevel.getProduct();
		Workshift workshift = pallet.getLatestScan().getWorkshift();

		// Formateamos las dimensiones
		String dimensions = String.format("%dx%d",
				packLevel.getWidthMM().intValue(),
			//	packLevel.getDepthMM().intValue(),
				packLevel.getHeightMM().intValue()
		);

		return PaletNotificationDTO.builder()
				// IDs
				.sscc(pallet.getSscc())
				.batchNumber(pallet.getBatchNumber())
				.gtin(packLevel.getGtin())
				.productSku(product.getFormatCode())
				.productName(product.getName())
				.brand(product.getBrand())
				// Logística
				.packLevel(packLevel.getPackingLevel())
				.unitsInLevel(packLevel.getUnitsInLevel())
				.grossWeightKg(packLevel.getNetWeight())
				.stackingLimit(packLevel.getStackingLimit())
				.dimensionsMm(dimensions)
				// Trazabilidad
				.scannedAt(pallet.getLatestScan().getScannedAt())
				.packagingDateTime(pallet.getPackagingDateTime())
				.productUseByDate(pallet.getProductUseByDate())
				.isExpired(pallet.isExpired())
				// Operativa
				.employeeName(pallet.getLatestScan().getWorkshift().getUser().getFullName())
				.shiftType(workshift.getShift().getShiftType())
				.build();
	}
}
