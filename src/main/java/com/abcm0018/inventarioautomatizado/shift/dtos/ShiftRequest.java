package com.abcm0018.inventarioautomatizado.shift.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShiftRequest {
    @NotBlank(message = "Shift cannot be null or empty")
    @Pattern(
        regexp = "^(MORNING|AFTERNOON|NIGHT)$",
        message = "Shift must be MORNING, AFTERNOON or NIGHT"
    )
    private String shiftType;
    @NotBlank(message = "Start time cannot be null or empty")
    @Pattern(
        regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
        message = "Start time must be in HH:mm format (00:00 - 23:59)"
    )
    private String startTime;
    @NotBlank(message = "End time cannot be null or empty")
    @Pattern(
        regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
        message = "Start time must be in HH:mm format (00:00 - 23:59)"
    )
    private String endTime;
}
