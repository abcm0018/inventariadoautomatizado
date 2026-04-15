package com.abcm0018.sai.scanstation.application.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.abcm0018.sai.scanstation.domain.enums.ScanStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para ScanStation
 * Incluye información completa y campos calculados
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanStationResponseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @JsonProperty("station_code")
    private String stationCode;

    @JsonProperty("location_desc")
    private String locationDesc;

    @JsonProperty("camera_1_id")
    private String camera1Id;

    @JsonProperty("camera_2_id")
    private String camera2Id;

    @JsonProperty("status")
    private ScanStatus status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

}

