package com.abcm0018.sai.scanstation.application.impl;

import com.abcm0018.sai.scanstation.application.ScanStationService;
import com.abcm0018.sai.scanstation.application.dtos.ScanStationRequestDTO;
import com.abcm0018.sai.scanstation.application.dtos.ScanStationResponseDTO;
import com.abcm0018.sai.scanstation.application.mapper.ScanStationMapper;
import com.abcm0018.sai.scanstation.domain.entity.ScanStation;
import com.abcm0018.sai.scanstation.domain.enums.ScanStatus;
import com.abcm0018.sai.scanstation.domain.repository.ScanStationRepository;
import com.abcm0018.sai.scanstation.exceptions.ScanStationServiceException;
import com.abcm0018.sai.shared.constants.CustomErrorCode;
import com.abcm0018.sai.shift.application.dtos.ShiftRequestDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftResponseDTO;
import com.abcm0018.sai.shift.application.dtos.ShiftSummaryDTO;
import com.abcm0018.sai.shift.application.mapper.IShiftMapper;
import com.abcm0018.sai.shift.domain.entity.Shift;
import com.abcm0018.sai.shift.domain.enums.ShiftType;
import com.abcm0018.sai.shift.exceptions.ShiftServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanStationServiceImpl implements ScanStationService {

    private final ScanStationRepository scanStationRepository;
    private final ScanStationMapper scanStationMapper;

    @Override
    @Transactional
    @CacheEvict(value = "scans", allEntries = true)
    public ScanStationResponseDTO createStation(ScanStationRequestDTO request) {
        log.info("Creando nueva estación de escaneo - Código: {}, Camara 1: {}, Camara 2: {}", request.getStationCode(), request.getCamera1Id(), request.getCamera2Id());

        // Convertir DTO a entidad
        ScanStation scanStation = scanStationMapper.toScanStation(request);

        // Guardar en BD
        ScanStation saved = scanStationRepository.save(scanStation);

        log.info("Estación de escaneo creado exitosamente - Código: {}, Camara 1: {}, Camara 2: {}", saved.getStationCode(), saved.getCamera1Id(), saved.getCamera2Id());

        return convertToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "scans", key = "#id", unless = "#result == null")
    public ScanStationResponseDTO findById(Long id) {
        return convertToResponse(getScanStationEntityById(id));
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "scans", key = "'all-scans'")
    public List<ScanStationResponseDTO> findAll() {
        return scanStationMapper.toResponseList(scanStationRepository.findAll());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "scans", key = "#id"),
            @CacheEvict(value = "scans", allEntries = true)
    })
    public ScanStationResponseDTO updateScanStation(Long id, ScanStationRequestDTO requestDTO) {
        log.info("Actualizando estación de escaneo {}", id);

        ScanStation existingScanStation = getScanStationEntityById(id);

        // Actualizar usando el mapper
        scanStationMapper.updateEntityFromRequest(requestDTO, existingScanStation);

        ScanStation saved = scanStationRepository.save(existingScanStation);

        log.info("Estación de escaneo {} actualizada exitosamente", id);

        return convertToResponse(saved);

    }

    @Override
    public void deleteScanStation(Long id) {
        log.info("Desactivando estación de trabajo {}", id);

        ScanStation station = getScanStationEntityById(id);

        station.setStatus(ScanStatus.INACTIVE);
        scanStationRepository.save(station);

        log.info("Turno {} desactivado exitosamente", id);
    }

    /**
     * Obtiene una entidad ScanStation por ID
     * Método interno para evitar conversión a DTO
     */
    private ScanStation getScanStationEntityById(Long id) {
        return scanStationRepository.findById(id)
                .orElseThrow(() -> new ScanStationServiceException(CustomErrorCode.NOT_FOUND, "Estación de escaneo no encontrado con ID: " + id, HttpStatus.NOT_FOUND));
    }

    /**
     * Convierte Shift a ScanStationResponseDTO con información adicional
     */
    private ScanStationResponseDTO convertToResponse(ScanStation scanStation) {
        return scanStationMapper.toResponse(scanStation);
    }

}

