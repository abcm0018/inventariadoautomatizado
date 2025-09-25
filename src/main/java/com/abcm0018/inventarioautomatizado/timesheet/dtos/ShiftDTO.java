package com.abcm0018.inventarioautomatizado.timesheet.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShiftDTO {
    private String shiftType;
    private String startTime;
    private String endTime;
}
