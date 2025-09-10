package com.abcm0018.inventarioautomatizado.palets.service;

import com.abcm0018.inventarioautomatizado.palets.dtos.PaletDTO;
import com.abcm0018.inventarioautomatizado.palets.dtos.PaletRequest;

import java.util.List;

public interface PaletService {
    void deletePalet(String sscc);
    PaletDTO updatePalet(String sscc, PaletRequest palet);
    List<PaletDTO> getAllPalets();
    List<PaletDTO> getPaletsByEAN(String ean);
    PaletDTO getPaletsBySSCC(String sscc);
    List<PaletDTO> getPaletsByBatchNumber(String batchNumber);
    List<PaletDTO> getPaletsByShift(String shift);
}
