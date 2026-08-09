package com.abcm0018.sai.scanstation.application;

import com.abcm0018.sai.scanstation.application.dtos.ScanStationRequestDTO;
import com.abcm0018.sai.scanstation.application.dtos.ScanStationResponseDTO;
import java.util.List;

public interface ScanStationService {
    ScanStationResponseDTO createStation(ScanStationRequestDTO request);
    ScanStationResponseDTO findById(Long id);
    List<ScanStationResponseDTO> findAll();
    ScanStationResponseDTO updateScanStation(Long id, ScanStationRequestDTO requestDTO);
    void deleteScanStation(Long id);;
    ScanStationResponseDTO activateScanStation(Long id);
}
