package com.abcm0018.inventarioautomatizado.timesheet.dtos;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetRequest {
    @NotBlank(message = "Check in at cannot be null or empty")
    private String checkInAt;
    @NotBlank(message = "Check out at cannot be null or empty")
    private String checkOutAt;
}
