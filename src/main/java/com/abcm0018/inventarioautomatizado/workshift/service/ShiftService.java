package com.abcm0018.inventarioautomatizado.workshift.service;

import com.abcm0018.inventarioautomatizado.timesheet.dtos.ShiftDTO;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.ShiftRequest;

import java.util.List;

public interface ShiftService {
    ShiftDTO addShift(ShiftRequest shiftRequest);
    void deleteShift(String shiftType);
    ShiftDTO updateShift(String shiftType, ShiftRequest shiftRequest);
    ShiftDTO getInfo(String shiftType);
    List<ShiftDTO> getAllShift();
}
