package com.abcm0018.inventarioautomatizado.timesheet.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetResponseDTO {
    private String employeeNumber;
    private String shift;
    private String checkInAt;
    private String checkOutAt;
}
