package com.abcm0018.sai.palets.infrastructure.messaging.engine;

import java.util.HashSet;
import java.util.Set;

import com.abcm0018.sai.palets.domain.enums.ScanQuality;
import com.abcm0018.sai.palets.infrastructure.messaging.dtos.PaletLecturaMessageDTO;

import lombok.Getter;

/**
 * Contenedor temporal para la fusión (merge) de las lecturas de las cámaras.
 */
@Getter
public class PalletAggregationState {

    private final PaletLecturaMessageDTO message;

    // Accedido únicamente dentro de compute()/computeIfPresent() de ConcurrentHashMap,
    // que garantiza exclusión mutua por clave. No requiere sincronización adicional (R5).
    private final Set<String> camerasReported = new HashSet<>();

    private boolean wasMerged = false;

    public PalletAggregationState(PaletLecturaMessageDTO initialMessage) {
        this.message = initialMessage;
        this.camerasReported.add(initialMessage.getStationId() + "-" + initialMessage.getCameraId());
    }

    /**
     * Rellena los campos vacíos del mensaje inicial con los datos del nuevo mensaje.
     */
    public void merge(PaletLecturaMessageDTO newMessage) {
        this.wasMerged = true;
        this.camerasReported.add(newMessage.getStationId() + "-" + newMessage.getCameraId());

        if (newMessage.getEan() != null) {
            this.message.setEan(newMessage.getEan());
        }
        if (newMessage.getBatchNumber() != null) {
            this.message.setBatchNumber(newMessage.getBatchNumber());
        }
        if (newMessage.getPackagingDateTime() != null) {
            this.message.setPackagingDateTime(newMessage.getPackagingDateTime());
        }
        if (newMessage.getProductUseByDate() != null) {
            this.message.setProductUseByDate(newMessage.getProductUseByDate());
        }
    }

    public boolean isComplete() {
        boolean hasAllFields = this.message.getSscc() != null
                && this.message.getEan() != null
                && this.message.getBatchNumber() != null;

        boolean allCamerasReported = this.camerasReported.size() >= 2;

        return hasAllFields || allCamerasReported;
    }

    /**
     * Determina la calidad del escaneo basándose en cuántas cámaras reportaron
     * y si fue necesario fusionar campos entre mensajes.
     */
    public ScanQuality computeScanQuality() {
        if (camerasReported.size() >= 2) {
            return wasMerged ? ScanQuality.MERGED : ScanQuality.PERFECT;
        }
        // Una sola cámara (timeout). El único elemento del Set tiene formato "stationId-cameraId".
        String entry = camerasReported.iterator().next();
        String cameraId = entry.substring(entry.lastIndexOf('-') + 1);
        return message.getCameraId().equals(cameraId) ? ScanQuality.CAM1_ONLY : ScanQuality.CAM2_ONLY;
    }
}
