package com.abcm0018.inventarioautomatizado.workshift.service.impl;

import com.abcm0018.inventarioautomatizado.paletInfo.exceptions.StaticPaletInfoServiceException;
import com.abcm0018.inventarioautomatizado.paletInfo.mapper.StaticPaletInfoMapper;
import com.abcm0018.inventarioautomatizado.productos.constants.CustomErrorCode;
import com.abcm0018.inventarioautomatizado.shared.config.InventariadoCacheConfig;
import com.abcm0018.inventarioautomatizado.shared.utils.ShiftUtils;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.ShiftDTO;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.ShiftRequest;
import com.abcm0018.inventarioautomatizado.users.domain.repository.UserRepository;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.Shift;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.ShiftType;
import com.abcm0018.inventarioautomatizado.workshift.domain.repository.ShiftRepository;
import com.abcm0018.inventarioautomatizado.workshift.exceptions.ShiftServiceException;
import com.abcm0018.inventarioautomatizado.workshift.mapper.ShiftMapper;
import com.abcm0018.inventarioautomatizado.workshift.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = InventariadoCacheConfig.PRODUCT_INFO)
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

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
    public ShiftDTO updateShift(String shiftType, ShiftRequest shiftRequest) {
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
        shiftToUpdate.setUser(existingShift.getUser());
        shiftToUpdate.setTimesheet(existingShift.getTimesheet());

        Shift updatedShift = shiftRepository.save(shiftToUpdate);
        return ShiftMapper.toDTO(updatedShift);
    }

    @Override
    public void deleteShift(String shiftType) {
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

        shiftRepository.delete(shift);
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
                .orElseThrow(() -> new StaticPaletInfoServiceException(
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
