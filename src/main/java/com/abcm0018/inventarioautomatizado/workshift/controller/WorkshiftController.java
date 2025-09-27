package com.abcm0018.inventarioautomatizado.workshift.controller;

import com.abcm0018.inventarioautomatizado.shared.response.ResponseBuilder;
import com.abcm0018.inventarioautomatizado.shared.response.StandardResponse;
import com.abcm0018.inventarioautomatizado.workshift.service.WorkshiftService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/workshifts")
@RequiredArgsConstructor
@Slf4j
public class WorkshiftController {

    private final WorkshiftService workshiftService;

    @CrossOrigin
    @Operation(summary = "This method is used to assign manually workshift")
    @PostMapping(value = "/assign")
    public StandardResponse<Void> assignManuallyWorkshift(@RequestParam String weekStart) {
        workshiftService.assignWeeklyShifts(weekStart);
        log.info("Workshift assigned!");
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "User successfully updated");
    }

    // TODO: Esto se tiene que borrar, solo es para pruebas

    @CrossOrigin
    @Operation(summary = "This method is used to assign dynamic workshift")
    @PostMapping(value = "/assign-current-week")
    public StandardResponse<Void> assignShiftsForCurrentWeek() {
        workshiftService.assignShiftForCurrentWeek();
        log.info("Workshift for this week assigned dynamic!");
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "User successfully updated");
    }

    @CrossOrigin
    @Operation(summary = "This method is used to assign dynamic workshift")
    @PostMapping(value = "/assign-next-week")
    public StandardResponse<Void> assignShiftsForNextWeek() {
        workshiftService.assignShiftsForNextWeek();
        log.info("Workshift for the next week assigned dynamic!");
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "User successfully updated");
    }



}
