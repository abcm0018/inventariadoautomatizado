package com.abcm0018.inventarioautomatizado.timesheet.mapper;

import com.abcm0018.inventarioautomatizado.timesheet.domain.entity.Timesheet;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.TimesheetRequest;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.TimesheetResponseDTO;
import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.Shift;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TimesheetMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSSSSS");

    private TimesheetMapper() {throw new IllegalStateException("Utility class");}

    public static Timesheet toEntity(TimesheetRequest request, User user, Shift shift){
        return Timesheet
                .builder()
                .user(user)
                .checkInAt(LocalDateTime.parse(request.getCheckInAt(), FORMATTER))
                .checkOutAt(LocalDateTime.parse(request.getCheckOutAt(), FORMATTER))
                .shift(shift)
                .build();
    }

    public static TimesheetResponseDTO toDTO(Timesheet timesheet){
        return TimesheetResponseDTO
                .builder()
                .employeeNumber(timesheet.getUser().getEmployeeNumber())
                .checkInAt(timesheet.getCheckInAt().format(FORMATTER))
                .checkOutAt(timesheet.getCheckOutAt().format(FORMATTER))
                .shift(timesheet.getShift().getShiftType().toString())
                .build();
    }

    public static List<TimesheetResponseDTO> toDTOList(List<Timesheet> timesheetList) {
        return timesheetList.stream().map(TimesheetMapper::toDTO).toList();
    }

    private static String dateToString(LocalDate date){
        if(date == null){
            return "";
        }
        return date.format(FORMATTER);
    }
}
