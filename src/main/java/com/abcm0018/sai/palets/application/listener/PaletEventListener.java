package com.abcm0018.sai.palets.application.listener;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import com.abcm0018.sai.palets.application.dtos.PaletNotificationDTO;
import com.abcm0018.sai.palets.domain.PaletCreatedEvent;
import com.abcm0018.sai.palets.domain.entity.Palet;
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

	// Necesitamos el repositorio para buscar los datos completos del palet
	private final PaletRepository paletRepository;

	// El "Destino" (Topic) al que enviaremos los mensajes
	// Debe coincidir con el prefijo del bróker en WebSocketConfig
	private static final String WS_DESTINATION_TOPIC = "/topic/palets";

	@TransactionalEventListener(PaletCreatedEvent.class)
	// Necesitamos una transacción para poder cargar las relaciones
	// "lazy" (como palet.user y palet.packeLevel.product)
	// Sin que ocurra un 'LazyInitializationException'
	public void handlePaletCreatedEvent(PaletCreatedEvent event) {

		log.info("📡 Evento 'Palet creado' recibido. ID: {}. Preparando notificación WebSocket...", event.getPaletId());

		// 1. Buscamos el palet (con la consulta optimizada)
		try {
			paletRepository.findWithDetailsById(event.getPaletId())
					.ifPresentOrElse(
							palet -> {
								// 2. Creamos el DTO de notificación
								PaletNotificationDTO notification = buildNotificationDTO(palet);
								messagingTemplate.convertAndSend(WS_DESTINATION_TOPIC, notification);
							}, () -> {
								log.warn("El evento de 'Palet creado' no pudo ser procesado. Palet no encontrado. ID: {}", event.getPaletId());
							});
		} catch (Exception e) {
			log.error("❌ Error al procesar evento y enviar WebSocket: {}", e.getMessage(), e);
		}
	}

	/**
	 * Helper para construir el DTO enriquecido
	 */
	private PaletNotificationDTO buildNotificationDTO(Palet palet) {
		ProductPackLevel packLevel = palet.getProductPackLevel();
		Product product = packLevel.getProduct();
		Workshift workshift = palet.getWorkshift();

		// Formateamos las dimensiones
		String dimensions = String.format("%dx%d",
				packLevel.getWidthMM().intValue(),
			//	packLevel.getDepthMM().intValue(),
				packLevel.getHeightMM().intValue()
		);

		return PaletNotificationDTO.builder()
				// IDs
				.sscc(palet.getSscc())
				.batchNumber(palet.getBatchNumber())
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
				.scannedAt(palet.getScannedAt())
				.packagingDate(palet.getPackagingDate())
				.productUseByDate(palet.getProductUseByDate())
				.isExpired(palet.isExpired())
				// Operativa
				.employeeName(palet.getUser().getFullName())
				.shiftType(workshift.getShift().getShiftType())
				.build();
	}
}
