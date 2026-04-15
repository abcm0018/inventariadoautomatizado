package com.abcm0018.sai.scanstation.infrastructure;

import com.abcm0018.sai.scanstation.application.ScanStationService;
import com.abcm0018.sai.scanstation.application.dtos.ScanStationRequestDTO;
import com.abcm0018.sai.scanstation.application.dtos.ScanStationResponseDTO;
import com.abcm0018.sai.scanstation.domain.entity.ScanStation;
import com.abcm0018.sai.shared.response.ResponseBuilder;
import com.abcm0018.sai.shared.response.StandardResponse;
import com.abcm0018.sai.shift.application.dtos.ShiftRequestDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftResponseDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftSummaryDTO;
import com.abcm0018.sai.shift.application.service.ShiftService;
import com.abcm0018.sai.shift.domain.enums.ShiftType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/scanstations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shifts", description = "Endpoints para las estaciones de escaneo")
public class ScanStationController {
    private final ScanStationService scanStationService;

    /**
     * Crear un nuevo turno
     * Solo administradores
     */
    @CrossOrigin
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Crear una nueva estación de escaneo",
            description = "Crea una nueva estación de escaneo. Valida que no se solape con estaciones de escaneo existentes."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Estación de escaneo creada exitosamente", content = @Content(schema = @Schema(implementation = ScanStationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o solapamiento de estaciones de escaneo"),
            @ApiResponse(responseCode = "403", description = "No está autorizado para realizar esta operación"),
            @ApiResponse(responseCode = "409", description = "Ya existe una estación de escaneoo")
    })
    public StandardResponse<ScanStationResponseDTO> createScanStation(@Valid @RequestBody ScanStationRequestDTO requestDTO) {

        log.info("Creando nueva estación de escaneo - Código: {}, Camara 1: {}, Camara 2: {}", requestDTO.getStationCode(), requestDTO.getCamera1Id(), requestDTO.getCamera2Id());

        ScanStationResponseDTO created = scanStationService.createStation(requestDTO);

        return ResponseBuilder.withCreatedElements(HttpStatus.CREATED, true, 1, "Estación de escaneo creada exitosamente", created);
    }

    /**
     * Obtener un turno por ID
     */
    @CrossOrigin
    @GetMapping("/{id}")
    @Operation(summary = "Obtener estación de escaneo por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Turno encontrado", content = @Content(schema = @Schema(implementation = ShiftResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Turno no encontrado")
    })
    public StandardResponse<ScanStationResponseDTO> getShiftById(
            @PathVariable @Parameter(description = "ID del turno") Long id) {

        log.debug("Consultando turno con ID: {}", id);

        ScanStationResponseDTO scanStation = scanStationService.findById(id);

        return ResponseBuilder.with(HttpStatus.OK, true, "Estación de escaneo encontrada", scanStation);
    }

    /**
     * Listar todos los turnos
     */
    @CrossOrigin
    @GetMapping
    @Operation(summary = "Listar todos las estaciones de escaneo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de estaciones de escaneo")
    })
    public StandardResponse<List<ScanStationResponseDTO>> getAllScanStations() {

        log.debug("Listando todos las estaciones de escaneo");

        List<ScanStationResponseDTO> scans = scanStationService.findAll();

        String message = String.format("Encontrados %d estaciones de escaneo", scans.size());

        return ResponseBuilder.with(HttpStatus.OK, true, message, scans);
    }

    /**
     * Actualizar un turno
     * Solo administradores
     */
    @CrossOrigin
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Actualizar una estación de escaneo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estación de escaneo actualizada exitosamente", content = @Content(schema = @Schema(implementation = ShiftResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o solapamiento"),
            @ApiResponse(responseCode = "404", description = "Estación de escaneo no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autorizado para realizar esta operación"),
    })
    public StandardResponse<ScanStationResponseDTO> updateScanStation(@PathVariable Long id,
                                                          @Valid @RequestBody ScanStationRequestDTO requestDTO) {

        log.info("Actualizando estación de escaneo {}", requestDTO.getStationCode());

        ScanStationResponseDTO updated = scanStationService.updateScanStation(id, requestDTO);

        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Estación de escaneo actualizada exitosamente", updated);
    }

    /**
     * Desactivar una estación de trabajo (soft delete)
     * Solo administradores
     */
    @CrossOrigin
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Desactivar una estación de trabajo (soft delete)",
            description = "Desactiva una estación de escaneo sin eliminarlo de la base de datos"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estación de escaneo desactivada exitosamente"),
            @ApiResponse(responseCode = "403", description = "No autorizado a realizar esta operación"),
            @ApiResponse(responseCode = "404", description = "Turno no encontrado"),
            @ApiResponse(responseCode = "409", description = "La estación de escaneo tiene asignaciones activas")
    })
    public StandardResponse<Void> deleteShift(@PathVariable @Parameter(description = "ID del turno") Long id) {

        log.warn("Desactivando turno con ID: {}", id);

        scanStationService.deleteScanStation(id);

        return ResponseBuilder.withDeletedElements(HttpStatus.OK, true, 1, "Estación de escaneo desactivada exitosamente");
    }


}
