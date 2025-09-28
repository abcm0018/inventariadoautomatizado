package com.abcm0018.inventarioautomatizado.shift.service.impl;

import com.abcm0018.inventarioautomatizado.productos.constants.CustomErrorCode;
import com.abcm0018.inventarioautomatizado.shared.config.InventariadoCacheConfig;
import com.abcm0018.inventarioautomatizado.shared.utils.ShiftUtils;
import com.abcm0018.inventarioautomatizado.shift.dtos.ShiftDTO;
import com.abcm0018.inventarioautomatizado.shift.dtos.ShiftRequest;
import com.abcm0018.inventarioautomatizado.shift.domain.entity.Shift;
import com.abcm0018.inventarioautomatizado.shift.domain.entity.ShiftType;
import com.abcm0018.inventarioautomatizado.shift.domain.repository.ShiftRepository;
import com.abcm0018.inventarioautomatizado.shift.exceptions.ShiftServiceException;
import com.abcm0018.inventarioautomatizado.shift.mapper.ShiftMapper;
import com.abcm0018.inventarioautomatizado.shift.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = InventariadoCacheConfig.PRODUCT_INFO)
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;

    private static final Integer SUCCESS = 1;
    private static final Integer ERROR = 0;

    @Override
    public ShiftDTO addShift(ShiftRequest shiftRequest) {
        Optional<Shift> existInfo = shiftRepository.findByShiftType(ShiftType.valueOf(shiftRequest.getShiftType().toUpperCase()));
        if (existInfo.isPresent()) {
            throw new ShiftServiceException(
                    CustomErrorCode.CONFLICT,
                    "Shift already exists: " + shiftRequest.getShiftType(),
                    HttpStatus.CONFLICT
            );
        }

        Shift shift = ShiftMapper.toEntity(shiftRequest);
        shift = shiftRepository.save(shift);
        return ShiftMapper.toDTO(shift);
    }

    @Override
    public int updateShift(String shiftType, ShiftRequest shiftRequest) {
        if(!ShiftUtils.isShiftValid(shiftType.toUpperCase())){
            throw new ShiftServiceException(
                    CustomErrorCode.BAD_REQUEST,
                    "Shift type cannot be null or empty.",
                    HttpStatus.BAD_REQUEST
            );
        }

        Shift existingShift = shiftRepository.findByShiftType(ShiftType.valueOf(shiftType.toUpperCase()))
                .orElseThrow(() -> new ShiftServiceException(
                        CustomErrorCode.NOT_FOUND,
                        "Shift type: " + shiftType + " not found.",
                        HttpStatus.NOT_FOUND
                ));

        Shift shiftToUpdate = ShiftMapper.toEntity(shiftRequest);
        shiftToUpdate.setId(existingShift.getId());
        shiftToUpdate.setTimesheet(existingShift.getTimesheet());

        Shift updatedShift = shiftRepository.save(shiftToUpdate);

        if (updatedShift != null) {
            return SUCCESS;
        }
        return ERROR;
    }

    @Override
    public int deleteShift(String shiftType) {
        if(!ShiftUtils.isShiftValid(shiftType.toUpperCase())){
            throw new ShiftServiceException(
                    CustomErrorCode.BAD_REQUEST,
                    "ShiftType cannot be null or empty.",
                    HttpStatus.BAD_REQUEST
            );
        }

        Shift shift = shiftRepository.findByShiftType(ShiftType.valueOf(shiftType.toUpperCase()))
                .orElseThrow(() -> new ShiftServiceException(CustomErrorCode.NOT_FOUND,
                        "Shift type: " + shiftType + " not found.", HttpStatus.NOT_FOUND));

        if (shift != null) {
            shiftRepository.delete(shift);
            return SUCCESS;
        }
        return ERROR;
    }

    @Override
    public ShiftDTO getInfo(String shiftType) {
        if(!ShiftUtils.isShiftValid(shiftType.toUpperCase())){
            throw new ShiftServiceException(
                    CustomErrorCode.BAD_REQUEST,
                    "The shift type cannot be null or empty.",
                    HttpStatus.BAD_REQUEST
            );
        }
        Shift shift = shiftRepository.findByShiftType(ShiftType.valueOf(shiftType.toUpperCase()))
                .orElseThrow(() -> new ShiftServiceException(
                        CustomErrorCode.NOT_FOUND,
                        "Shift type: " + shiftType + " not found.",
                        HttpStatus.NOT_FOUND
                ));

        return ShiftMapper.toDTO(shift);
    }

    @Override
    public List<ShiftDTO> getAllShift() {
        List<Shift> shifts = shiftRepository.findAll();
        return ShiftMapper.toDTOList(shifts);
    }
}
