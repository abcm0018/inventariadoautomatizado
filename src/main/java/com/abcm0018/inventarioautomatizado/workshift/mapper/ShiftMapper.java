package com.abcm0018.inventarioautomatizado.workshift.mapper;

import com.abcm0018.inventarioautomatizado.timesheet.domain.entity.Timesheet;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.ShiftDTO;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.ShiftRequest;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.TimesheetRequest;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.TimesheetResponseDTO;
import com.abcm0018.inventarioautomatizado.timesheet.mapper.TimesheetMapper;
import com.abcm0018.inventarioautomatizado.users.domain.entity.User;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.Shift;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.ShiftType;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ShiftMapper {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private ShiftMapper() {throw new IllegalStateException("Utility class");}

    public static Shift toEntity(ShiftRequest request){
        return Shift
                .builder()
                .shiftType(ShiftType.valueOf(request.getShiftType()))
                .startTime(LocalTime.parse(request.getStartTime(), TIME_FORMATTER))
                .endTime(LocalTime.parse(request.getEndTime(), TIME_FORMATTER))
                .build();
    }

    public static ShiftDTO toDTO(Shift shift){
        return ShiftDTO
                .builder()
                .shiftType(shift.getShiftType().toString())
                .startTime(shift.getStartTime().format(TIME_FORMATTER))
                .endTime(shift.getEndTime().format(TIME_FORMATTER))
                .build();
    }

    public static List<ShiftDTO> toDTOList(List<Shift> shiftList) {
        return shiftList.stream().map(ShiftMapper::toDTO).toList();
    }

}
