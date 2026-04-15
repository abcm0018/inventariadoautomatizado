package com.abcm0018.sai.scanstation.domain.enums;

import lombok.Getter;

import java.util.List;

/**
 * Estados posibles de una estación de escaneo en el sistema
 */
@Getter
public enum ScanStatus {
    ACTIVE("Activo"), //Activo
    INACTIVE("Inactivo"), //Inactivo
    MAINTENANCE("Mantenimiento"), //Mantenimiento
    OFFLINE("Fuera de servicio"); // Fuera de servicio

    private final String displayStatus;

    ScanStatus(String displayStatus) {
        this.displayStatus = displayStatus;
    }

    public static List<ScanStatus> getStatus(){
        return List.of(ACTIVE, INACTIVE, MAINTENANCE, OFFLINE);
    }
}
