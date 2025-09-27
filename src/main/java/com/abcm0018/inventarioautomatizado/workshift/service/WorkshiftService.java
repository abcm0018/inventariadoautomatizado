package com.abcm0018.inventarioautomatizado.workshift.service;

import com.abcm0018.inventarioautomatizado.workshift.domain.entity.ShiftChange;

public interface WorkshiftService {
    void assignWeeklyShifts(String weekStart);

    void assignShiftForCurrentWeek();

    void assignShiftsForNextWeek();

    ShiftChange requestShiftChange(String employeeNumber, String currentDate, String currentShiftName, String requestedDate, String requestedShiftName, String reason);
}
