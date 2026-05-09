package com.abcm0018.sai.palets.domain.enums;

public enum ScanQuality {
    PERFECT,    // Ambas cámaras reportaron y todos los campos llegaron en el primer mensaje
    MERGED,     // Ambas cámaras reportaron y fue necesario fusionar campos entre mensajes
    CAM1_ONLY,  // Solo la cámara 1 reportó (timeout con datos parciales)
    CAM2_ONLY,  // Solo la cámara 2 reportó (timeout con datos parciales)
    MANUAL      // Palet creado manualmente vía REST (sin lectura MQTT de cámara)
}
