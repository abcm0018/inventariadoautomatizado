package com.abcm0018.inventarioautomatizado.users.dtos;

import com.abcm0018.inventarioautomatizado.timesheet.dtos.TimesheetResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {
    private String employeeNumber;
    private String name;
    private String surname;
    private String email;
    private String jobPosition;
    private String role;
}
