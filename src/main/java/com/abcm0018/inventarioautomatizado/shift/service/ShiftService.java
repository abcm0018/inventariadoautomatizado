package com.abcm0018.inventarioautomatizado.shift.service;

import com.abcm0018.inventarioautomatizado.shift.dtos.ShiftDTO;
import com.abcm0018.inventarioautomatizado.shift.dtos.ShiftRequest;

import java.util.List;

public interface ShiftService {
    ShiftDTO addShift(ShiftRequest shiftRequest);
    int deleteShift(String shiftType);
    int updateShift(String shiftType, ShiftRequest shiftRequest);
    ShiftDTO getInfo(String shiftType);
    List<ShiftDTO> getAllShift();
}
