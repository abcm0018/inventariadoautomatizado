package com.abcm0018.sai.scanstation.application.mapper;

import com.abcm0018.sai.scanstation.application.dtos.ScanStationRequestDTO;
import com.abcm0018.sai.scanstation.application.dtos.ScanStationResponseDTO;
import com.abcm0018.sai.scanstation.domain.entity.ScanStation;
import com.abcm0018.sai.shift.application.dtos.ShiftRequestDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftResponseDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftSummaryDTO;
import com.abcm0018.sai.shift.domain.entity.Shift;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper para ScanStation usando MapStruct
 * <p>
 * Responsabilidades:
 * - Convertir ScanStation ↔ DTOs
 * - Validar integridad de datos en mapeos
 * <p>
 * Buenas Prácticas:
 * - Métodos específicos para cada caso de uso
 * - @AfterMapping para lógica post-conversión
 * - Soporte para null safety
 * - No mezclar lógica de negocio compleja (delegar a Service)
 */
/**
 * Mapper para convertir entre ScanStation Entity y DTOs
 * MapStruct genera la implementación automáticamente en tiempo de compilación
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ScanStationMapper {
    /**
     * Convierte ScanStationRequestDTO a ScanStation Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ScanStation toScanStation(ScanStationRequestDTO requestDTO);

    /**
     * Actualiza una entidad existente con datos del ScanStationRequestDTO.
     * Solo actualiza campos no nulos.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ScanStationRequestDTO requestDTO, @MappingTarget ScanStation entity);

    /**
     * Convierte ScanStation a ScanStationResponseDTO (respuesta completa)
     */
    ScanStationResponseDTO toResponse(ScanStation station);

    /**
     * Convierte lista de Shifts a lista de ScanStationResponseDTO
     */
    List<ScanStationResponseDTO> toResponseList(List<ScanStation> scansStations);

}
