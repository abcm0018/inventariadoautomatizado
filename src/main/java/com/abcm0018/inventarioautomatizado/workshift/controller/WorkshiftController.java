package com.abcm0018.inventarioautomatizado.workshift.controller;

import com.abcm0018.inventarioautomatizado.shared.response.ResponseBuilder;
import com.abcm0018.inventarioautomatizado.shared.response.StandardResponse;
import com.abcm0018.inventarioautomatizado.workshift.domain.entity.WorkshiftChange;
import com.abcm0018.inventarioautomatizado.workshift.dtos.ShiftChangeRequest;
import com.abcm0018.inventarioautomatizado.workshift.service.WorkshiftService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping(value = "/api/v1/workshifts")
@RequiredArgsConstructor
@Slf4j
public class WorkshiftController {

    private final WorkshiftService workshiftService;

    @CrossOrigin
    @Operation(summary = "This method is used to assign manually workshift")
    @PostMapping(value = "/assign")
    public StandardResponse<Void> assignManuallyWorkshift(@RequestParam LocalDate weekStart) {
        workshiftService.assignWeeklyShifts(weekStart);
        log.info("Workshift assigned!");
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "User successfully updated");
    }

    // TODO: Esto se tiene que borrar, solo es para pruebas

    @CrossOrigin
    @Operation(summary = "This method is used to assign dynamic workshift")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(value = "/assign-current-week")
    public StandardResponse<Void> assignShiftsForCurrentWeek() {
        workshiftService.assignShiftForCurrentWeek();
        log.info("Workshift for this week assigned dynamic!");
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "User successfully updated");
    }

    @CrossOrigin
    @Operation(summary = "This method is used to assign dynamic workshift")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(value = "/assign-next-week")
    public StandardResponse<Void> assignShiftsForNextWeek() {
        workshiftService.assignShiftsForNextWeek();
        log.info("Workshift for the next week assigned dynamic!");
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "User successfully updated");
    }

    @CrossOrigin
    @Operation(summary = "This method allows an employee to request a shift change")
    @PreAuthorize("hasAnyAuthority('OPERATOR', 'SUPERVISOR')")
    @PostMapping("/request-change")
    public StandardResponse<WorkshiftChange> requestShiftChange(@RequestBody ShiftChangeRequest request){
        WorkshiftChange shiftChange = workshiftService.requestWorkshiftChange(request);
        log.info("✅ Shift change requested by employee {}", request.getEmployeeNumber());
        return ResponseBuilder.withUpdatedElements(
                HttpStatus.OK,
                true,
                1,
                "Shift change request submitted successfully",
                shiftChange
        );
    }

    @CrossOrigin
    @Operation(summary = "This method allows an admin to accept a shift change")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/approve-change/{requestId}")
    public StandardResponse<Void> approveShiftChange(@PathVariable Long requestId, @RequestParam boolean approved) {
        workshiftService.approveShiftChange(requestId, approved);
        log.info("✅ Shift change requested: {}", requestId);
        return ResponseBuilder.withUpdatedElements(
                HttpStatus.OK,
                true,
                1,
                "Shift change request submitted successfully"
        );
    }
}
