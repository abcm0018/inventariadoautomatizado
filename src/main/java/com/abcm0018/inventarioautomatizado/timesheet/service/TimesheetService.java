package com.abcm0018.inventarioautomatizado.timesheet.service;


import com.abcm0018.inventarioautomatizado.timesheet.dtos.TimesheetRequest;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.TimesheetResponseDTO;

import java.util.List;

public interface TimesheetService {
    TimesheetResponseDTO updateTimesheet (String employeeNumber, String shift, TimesheetRequest request);
    void deleteTimesheet (String employeeNumber, String shift);
    TimesheetResponseDTO getTimesheet (String employeeNumber, String shift);
    List<TimesheetResponseDTO> getAllTimesheets ();
}
