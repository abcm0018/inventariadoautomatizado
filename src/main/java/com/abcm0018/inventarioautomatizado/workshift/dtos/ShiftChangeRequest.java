package com.abcm0018.inventarioautomatizado.workshift.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShiftChangeRequest {
    @NotBlank(message = "Employee number cannot be null or empty")
    private String employeeNumber;
    @NotBlank(message = "Workshift id cannot be null or empty")
    private Long workshiftId;
    @NotBlank(message = "New workshift cannot be null or empty")
    private String newWorkshiftDate;
    @NotBlank(message = "New shift cannot be null or empty")
    private String newShift;
    @NotBlank(message = "Reason cannot be null or empty")
    private String reason;
}

