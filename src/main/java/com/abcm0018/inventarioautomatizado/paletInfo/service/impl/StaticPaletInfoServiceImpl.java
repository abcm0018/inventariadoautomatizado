package com.abcm0018.inventarioautomatizado.paletInfo.service.impl;

import com.abcm0018.inventarioautomatizado.paletInfo.domain.entity.StaticPaletInfo;
import com.abcm0018.inventarioautomatizado.paletInfo.domain.repository.StaticPaletInfoRepository;
import com.abcm0018.inventarioautomatizado.paletInfo.dtos.StaticPaletInfoDTO;
import com.abcm0018.inventarioautomatizado.paletInfo.dtos.StaticPaletInfoRequest;
import com.abcm0018.inventarioautomatizado.paletInfo.exceptions.StaticPaletInfoServiceException;
import com.abcm0018.inventarioautomatizado.paletInfo.mapper.StaticPaletInfoMapper;
import com.abcm0018.inventarioautomatizado.paletInfo.service.StaticPaletInfoService;
import com.abcm0018.inventarioautomatizado.productos.constants.CustomErrorCode;
import com.abcm0018.inventarioautomatizado.shared.config.InventariadoCacheConfig;
import com.abcm0018.inventarioautomatizado.shared.utils.PaletUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = InventariadoCacheConfig.PRODUCT_INFO)
public class StaticPaletInfoServiceImpl implements StaticPaletInfoService {

    private final StaticPaletInfoRepository staticPaletInfoRepository;

    @Override
    @CacheEvict(allEntries = true)
    public StaticPaletInfoDTO addInfo(StaticPaletInfoRequest infoRequest) {
        Optional<StaticPaletInfo> existInfo = staticPaletInfoRepository.findBySscc(infoRequest.getSscc());
        if (existInfo.isPresent()) {
            throw new StaticPaletInfoServiceException(
                    CustomErrorCode.BAD_REQUEST,
                    "Ya existe una información estática para el producto con SSCC: " + infoRequest.getSscc(),
                    HttpStatus.BAD_REQUEST
            );
        }

        StaticPaletInfo info = StaticPaletInfoMapper.toEntity(infoRequest);
        info.setCreatedAt(LocalDateTime.now());

        StaticPaletInfo savedInfo = staticPaletInfoRepository.save(info);
        return StaticPaletInfoMapper.toDTO(savedInfo);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void deleteStaticPaletInfo(String sscc) {
        if(!PaletUtils.isSSCCValid(sscc)){
            throw new StaticPaletInfoServiceException(
                    CustomErrorCode.BAD_REQUEST,
                    "El SSCC no puede ser nulo o vacío",
                    HttpStatus.BAD_REQUEST
            );
        }

        StaticPaletInfo info = staticPaletInfoRepository.findBySscc(sscc)
                .orElseThrow(() -> new StaticPaletInfoServiceException(CustomErrorCode.NOT_FOUND,
                        "Información estática no encontrado con SSCC: " + sscc, HttpStatus.NOT_FOUND));

        info.setDeletedAt(LocalDateTime.now());
        staticPaletInfoRepository.save(info);
    }

    @Override
    @CacheEvict(allEntries = true)
    public StaticPaletInfoDTO updateInfo(String sscc, StaticPaletInfoRequest infoRequest) {

        if(!PaletUtils.isSSCCValid(sscc)){
            throw new StaticPaletInfoServiceException(
                    CustomErrorCode.BAD_REQUEST,
                    "El SSCC no puede ser nulo o vacío",
                    HttpStatus.BAD_REQUEST
            );
        }

        StaticPaletInfo existingInfo = staticPaletInfoRepository.findBySscc(sscc)
                .orElseThrow(() -> new StaticPaletInfoServiceException(
                        CustomErrorCode.NOT_FOUND,
                        "Información estática no encontrada con SSCC: " + sscc,
                        HttpStatus.BAD_REQUEST
                ));

        if(existingInfo.getDeletedAt() != null) {
            throw new StaticPaletInfoServiceException(
                    CustomErrorCode.NOT_FOUND,
                    "Información estática no encontrada con SSCC: " + sscc,
                    HttpStatus.BAD_REQUEST
            );
        }

        StaticPaletInfo infoToUpdate = StaticPaletInfoMapper.toEntity(infoRequest);
        infoToUpdate.setId(existingInfo.getId());
        infoToUpdate.setCreatedAt(existingInfo.getCreatedAt());
        infoToUpdate.setUpdatedAt(LocalDateTime.now());

        StaticPaletInfo updatedInfo = staticPaletInfoRepository.save(infoToUpdate);
        return StaticPaletInfoMapper.toDTO(updatedInfo);
    }

    @Override
    @CacheEvict(allEntries = true)
    public StaticPaletInfoDTO getInfo(String sscc) {
        if(!PaletUtils.isSSCCValid(sscc)){
            throw new StaticPaletInfoServiceException(
                    CustomErrorCode.BAD_REQUEST,
                    "El SSCC no puede ser nulo o vacío",
                    HttpStatus.BAD_REQUEST
            );
        }
        StaticPaletInfo info = staticPaletInfoRepository.findBySscc(sscc)
                .orElseThrow(() -> new StaticPaletInfoServiceException(
                        CustomErrorCode.NOT_FOUND,
                        "Información estática no encontrada con SSCC: " + sscc,
                        HttpStatus.BAD_REQUEST
                ));

        return StaticPaletInfoMapper.toDTO(info);
    }

    @Override
    @CacheEvict(allEntries = true)
    public List<StaticPaletInfoDTO> getAllInfo() {
        List<StaticPaletInfo> staticPaletInfo = staticPaletInfoRepository.findAll();
        return StaticPaletInfoMapper.toDTOList(staticPaletInfo);
    }
}
