package com.abcm0018.inventarioautomatizado.palets.service.impl;

import com.abcm0018.inventarioautomatizado.palets.domain.entity.Palet;
import com.abcm0018.inventarioautomatizado.palets.domain.repository.PaletRepository;
import com.abcm0018.inventarioautomatizado.palets.dtos.PaletDTO;
import com.abcm0018.inventarioautomatizado.palets.dtos.PaletRequest;
import com.abcm0018.inventarioautomatizado.palets.exceptions.PaletsServiceException;
import com.abcm0018.inventarioautomatizado.palets.mappers.PaletMapper;
import com.abcm0018.inventarioautomatizado.palets.service.PaletService;
import com.abcm0018.inventarioautomatizado.productos.constants.CustomErrorCode;
import com.abcm0018.inventarioautomatizado.shared.config.InventariadoCacheConfig;
import com.abcm0018.inventarioautomatizado.shared.utils.PaletUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = InventariadoCacheConfig.PRODUCT_INFO)
public class PaletServiceImpl implements PaletService {

    private final PaletRepository paletRepository;

    @Override
    public PaletDTO updatePalet(String sscc, PaletRequest request){

        if (!PaletUtils.isSSCCValid(sscc)) {
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "The SSCC to be updated cannot be empty.", HttpStatus.BAD_REQUEST);
        }
        Palet existPalet = paletRepository.findBySscc(sscc).orElseThrow(() ->
                new PaletsServiceException(CustomErrorCode.NOT_FOUND,
                        "The Pallet with SSCC " + sscc + " doesn't exist",
                        HttpStatus.NOT_FOUND));

        Palet paledToUpdated = PaletMapper.toEntity(request);
        paledToUpdated.setId(existPalet.getId());
        paledToUpdated.setProduct(existPalet.getProduct());
        paledToUpdated.setPackagingDate(parseDate(request.getPackagingDate()));
        paledToUpdated.setProductUseByDate(parseDate(request.getProductUseByDate()));
        paledToUpdated.setCreatedAt(existPalet.getCreatedAt());
        paledToUpdated.setUpdatedAt(LocalDateTime.now());

        paletRepository.save(paledToUpdated);

        return PaletMapper.toDTO(paledToUpdated);

    }

    @Override
    public void deletePalet(String sscc) {
        if (!PaletUtils.isSSCCValid(sscc)) {
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "The SSCC does not have a valid format.", HttpStatus.BAD_REQUEST);
        }

        Palet existPalet = paletRepository.findBySscc(sscc).orElseThrow(() ->
                new PaletsServiceException(CustomErrorCode.NOT_FOUND,
                        "The Pallet with SSCC " + sscc + " doesn't exist",
                        HttpStatus.NOT_FOUND));

        paletRepository.delete(existPalet);
    }

    @Override
    @CacheEvict(allEntries = true)
    public List<PaletDTO> getAllPalets() {
        //Datos impresos
        List<Palet> palets = paletRepository.findAll();
        return PaletMapper.toDTOList(palets);
    }

    @Override
    @CacheEvict(allEntries = true)
    public List<PaletDTO> getPaletsByEAN(String ean) {
        if(!PaletUtils.isEANValid(ean)){
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "The EAN does not have a valid format.", HttpStatus.BAD_REQUEST);
        }
        //Datos impresos
        List<Palet> palets = paletRepository.findAllByEan(ean);
        //Datos estáticos
        return PaletMapper.toDTOList(palets);
    }

    @Override
    @CacheEvict(allEntries = true)
    public PaletDTO getPaletsBySSCC(String sscc) {
        if (!PaletUtils.isSSCCValid(sscc)) {
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "The SSCC does not have a valid format.", HttpStatus.BAD_REQUEST);
        }
        //Datos impresos
        Palet palet = paletRepository.findBySscc(sscc).orElseThrow(() ->
                new PaletsServiceException(CustomErrorCode.NOT_FOUND,"The Pallet with SSCC " + sscc + " doesn't exist", HttpStatus.NOT_FOUND));

        //Crear un método para rellenar los campos estáticos del dto
        return PaletMapper.toDTO(palet);
    }

    @Override
    @CacheEvict(allEntries = true)
    public List<PaletDTO> getPaletsByBatchNumber(String batchNumber) {
        if (!PaletUtils.isBatchValid(batchNumber)) {
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "The batch number does not have a valid format.", HttpStatus.BAD_REQUEST);
        }
        //Datos impresos
        List<Palet> palets = paletRepository.findByBatchNumber(batchNumber).orElseThrow(() ->
                new PaletsServiceException(CustomErrorCode.NOT_FOUND,"Pallet not found: " + batchNumber, HttpStatus.NOT_FOUND));
        return PaletMapper.toDTOList(palets);
    }

    @Override
    @CacheEvict(allEntries = true)
    public List<PaletDTO> getPaletsByShift(String shift) {
        if (!PaletUtils.isShiftValid(shift)) {
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "The shift does not have a valid format.", HttpStatus.BAD_REQUEST);
        }
        List<Palet> palets = paletRepository.findByShift(shift.toUpperCase()).orElseThrow(() ->
                new PaletsServiceException(CustomErrorCode.NOT_FOUND,"Pallet not found: " + shift, HttpStatus.NOT_FOUND));
        return PaletMapper.toDTOList(palets);    }

    @Override
    @Cacheable(key = "{#root.methodName, #ean, #batchNumber, #productionTime, #shift, #packagingDate, #productUseByDate}")
    public List<PaletDTO> findByFilters(String ean, String batchNumber, String packagingDate, String productUseByDate, String productionTime, String shift) {
        validateInputFilter(ean, batchNumber, packagingDate, productUseByDate, productionTime, shift);

        final List<Palet> palets = paletRepository.findPalets(ean, batchNumber, packagingDate, productUseByDate, productionTime, shift);
        return PaletMapper.toDTOList(palets);
    }

    private void validateInputFilter(String ean, String batchNumber, String packagingDate, String productUseByDate, String productionTime, String shift) throws PaletsServiceException {
        // Regex para EAN-14 (14 dígitos numéricos)
        String eanRegex = "^[0-9]{14}$";

        // Regex para batch (números y letras)
        String batchNumberRegex = "^[0-9]{7}[A-Z]$";

        // Regex para fechas en formato dd/MM/yyyy (permite 01/01/2025, etc.)
        String dateRegex = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$";

        // Regex para turno
        String shiftRegex = "^(MORNING|AFTERNOON|NIGHT)$";

        // Regex para hora
        String timeRegex = "^([01]\\d|2[0-3]):[0-5]\\d$";

        // Validaciones
        if (ean != null && !ean.matches(eanRegex)) {
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "Invalid EAN. Must contain 14 digits.", HttpStatus.BAD_REQUEST);
        }

        if (batchNumber != null && !batchNumber.matches(batchNumberRegex)) {
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "Invalid batch number. Must contain number and one letter.", HttpStatus.BAD_REQUEST);
        }

        if (packagingDate != null && !packagingDate.matches(dateRegex)) {
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "Invalid start date. Expected format: dd/mm/yyyy.", HttpStatus.BAD_REQUEST);
        }

        if (productUseByDate != null && !productUseByDate.matches(dateRegex)) {
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "Invalid start date. Expected format: dd/mm/yyyy.", HttpStatus.BAD_REQUEST);
        }

        if (shift != null && !shift.toUpperCase().matches(shiftRegex)) {
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "Invalid shift. Must be MORNING, AFTERNOON or NIGHT", HttpStatus.BAD_REQUEST);
        }

        if (productionTime != null && !productionTime.matches(timeRegex)) {
            throw new PaletsServiceException(CustomErrorCode.BAD_REQUEST, "Invalid time format. Expected format: HH:MM", HttpStatus.BAD_REQUEST);
        }
    }

    private LocalDate parseDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            throw new PaletsServiceException(
                    CustomErrorCode.BAD_REQUEST,
                    "The date must be in the format dd/MM/yyyy. Value received: " + dateStr,
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

}
