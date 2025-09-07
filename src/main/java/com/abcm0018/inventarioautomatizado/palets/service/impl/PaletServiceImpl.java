package com.abcm0018.inventarioautomatizado.palets.service.impl;

import com.abcm0018.inventarioautomatizado.palets.domain.entity.Palet;
import com.abcm0018.inventarioautomatizado.paletInfo.domain.entity.StaticPaletInfo;
import com.abcm0018.inventarioautomatizado.palets.domain.repository.PaletRepository;
import com.abcm0018.inventarioautomatizado.paletInfo.domain.repository.StaticPaletInfoRepository;
import com.abcm0018.inventarioautomatizado.palets.dtos.PaletDTO;
import com.abcm0018.inventarioautomatizado.palets.dtos.PaletRequest;
import com.abcm0018.inventarioautomatizado.palets.exceptions.PaletsServiceException;
import com.abcm0018.inventarioautomatizado.paletInfo.exceptions.StaticPaletInfoServiceException;
import com.abcm0018.inventarioautomatizado.palets.mappers.PaletDTOMapper;
import com.abcm0018.inventarioautomatizado.palets.mappers.PaletMapper;
import com.abcm0018.inventarioautomatizado.palets.service.PaletService;
import com.abcm0018.inventarioautomatizado.productos.constants.CustomErrorCode;
import com.abcm0018.inventarioautomatizado.shared.config.InventariadoCacheConfig;
import com.abcm0018.inventarioautomatizado.shared.utils.PaletUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = InventariadoCacheConfig.PRODUCT_INFO)
public class PaletServiceImpl implements PaletService {

    private final PaletRepository paletRepository;
    private final StaticPaletInfoRepository staticPaletInfoRepository;

    @Override
    public void deletePalet(String sscc) {
        if (!PaletUtils.isSSCCValid(sscc)) {
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "El SSCC no tiene un formato válido", HttpStatus.BAD_REQUEST);
        }
        Optional<Palet> existPalet = paletRepository.findBySscc(sscc);

        Palet palet = existPalet.orElseThrow(() ->
                new PaletsServiceException(CustomErrorCode.NOT_FOUND,
                        "El Palet con SSCC " + sscc + " no existe",
                        HttpStatus.BAD_REQUEST));

        paletRepository.delete(palet);
    }

    @Override
    public PaletDTO updatePalet(String sscc, PaletRequest request){

        if (!PaletUtils.isSSCCValid(sscc)) {
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "El SSCC a eliminar no puede estar vacío", HttpStatus.BAD_REQUEST);
        }
        Palet existPalet = paletRepository.findBySscc(sscc).orElseThrow(() ->
                new PaletsServiceException(CustomErrorCode.NOT_FOUND,
                        "El Palet con SSCC " + sscc + " no existe",
                        HttpStatus.BAD_REQUEST));

        StaticPaletInfo existInfo = staticPaletInfoRepository.findBySscc(sscc).orElseThrow(() ->
                new PaletsServiceException(CustomErrorCode.NOT_FOUND,
                        "El Palet con SSCC " + sscc + " no tiene información estática asociada",
                        HttpStatus.BAD_REQUEST));
        Palet paledToUpdated = PaletMapper.toEntity(request);
        paledToUpdated.setProductionDate(parseDate(request.getProductionDate()));
        paledToUpdated.setExpirationDate(parseDate(request.getExpirationDate()));

        return PaletDTOMapper.toDTO(paledToUpdated, existInfo);

    }

    @Override
    @CacheEvict(allEntries = true)
    public List<PaletDTO> getAllPalets() {
        //Datos impresos
        List<Palet> palets = paletRepository.findAll();
        return palets.stream()
                .map(palet -> {
                    StaticPaletInfo staticPaletInfo = staticPaletInfoRepository.findBySscc(palet.getSscc()).orElseThrow(() ->
                            new StaticPaletInfoServiceException(CustomErrorCode.NOT_FOUND,"Información estática del palet asociada no encontrada: " + palet.getEan(), HttpStatus.BAD_REQUEST));
                    return PaletDTOMapper.toDTO(palet, staticPaletInfo);
                }).toList();
    }

    @Override
    @CacheEvict(allEntries = true)
    public List<PaletDTO> getPaletsByEAN(String ean) {
        if(!PaletUtils.isEANValid(ean)){
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "El EAN no tiene un formato válido", HttpStatus.BAD_REQUEST);
        }
        //Datos impresos
        List<Palet> palets = paletRepository.findAllByEan(ean);
        //Datos estáticos
        return palets.stream()
                .map(palet -> {
                    StaticPaletInfo staticPaletInfo = staticPaletInfoRepository.findBySscc(palet.getSscc()).orElseThrow(() ->
                            new StaticPaletInfoServiceException(CustomErrorCode.NOT_FOUND,"Información estática del palet asociada no encontrada: " + palet.getEan(), HttpStatus.BAD_REQUEST));
                    return PaletDTOMapper.toDTO(palet, staticPaletInfo);
                }).toList();
    }

    @Override
    @CacheEvict(allEntries = true)
    public PaletDTO getPaletsBySSCC(String sscc) {
        if (!PaletUtils.isSSCCValid(sscc)) {
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "El SSCC no tiene un formato válido", HttpStatus.BAD_REQUEST);
        }
        //Datos impresos
        Palet palet = paletRepository.findBySscc(sscc).orElseThrow(() ->
                new PaletsServiceException(CustomErrorCode.NOT_FOUND,"Palet no encontrado: " + sscc, HttpStatus.BAD_REQUEST));
        //Datos estáticos
        StaticPaletInfo staticPaletInfo = staticPaletInfoRepository.findBySscc(palet.getSscc()).orElseThrow(() ->
                new StaticPaletInfoServiceException(CustomErrorCode.NOT_FOUND,"Información estática del palet asociada no encontrada: " + palet.getEan(), HttpStatus.BAD_REQUEST));
        //Crear un método para rellenar los campos estáticos del dto
        return PaletDTOMapper.toDTO(palet, staticPaletInfo);
    }

    @Override
    @CacheEvict(allEntries = true)
    public List<PaletDTO> getPaletsByBatchNumber(String batchNumber) {
        //Datos impresos
        List<Palet> palets = paletRepository.findByBatchNumber(batchNumber).orElseThrow(() ->
                new PaletsServiceException(CustomErrorCode.NOT_FOUND,"Palet no encontrado: " + batchNumber, HttpStatus.BAD_REQUEST));
        return palets.stream()
                .map(palet -> {
                    StaticPaletInfo staticPaletInfo = staticPaletInfoRepository.findBySscc(palet.getSscc()).orElseThrow(() ->
                        new StaticPaletInfoServiceException(CustomErrorCode.NOT_FOUND,"Información estática del palet asociada no encontrada: " + palet.getEan(), HttpStatus.BAD_REQUEST));
                    return PaletDTOMapper.toDTO(palet, staticPaletInfo);
                }).toList();
    }

    private LocalDate parseDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            throw new PaletsServiceException(
                    CustomErrorCode.BAD_REQUEST,
                    "La fecha debe tener el formato dd/MM/yyyy. Valor recibido: " + dateStr,
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

}
