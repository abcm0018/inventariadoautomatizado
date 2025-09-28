package com.abcm0018.inventarioautomatizado.workshift.service;

import com.abcm0018.inventarioautomatizado.workshift.domain.entity.WorkshiftChange;
import com.abcm0018.inventarioautomatizado.workshift.dtos.ShiftChangeRequest;

import java.time.LocalDate;

public interface WorkshiftService {
    void assignWeeklyShifts(LocalDate weekStart);

    void assignShiftForCurrentWeek();

    void assignShiftsForNextWeek();

    Integer requestWorkshiftChange(ShiftChangeRequest request);

    void approveShiftChange(Long requestId, boolean approved);
}
