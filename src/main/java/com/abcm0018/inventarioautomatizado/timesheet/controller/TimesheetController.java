package com.abcm0018.inventarioautomatizado.timesheet.controller;

import com.abcm0018.inventarioautomatizado.shared.response.ResponseBuilder;
import com.abcm0018.inventarioautomatizado.shared.response.StandardResponse;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.TimesheetRequest;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.TimesheetResponseDTO;
import com.abcm0018.inventarioautomatizado.timesheet.service.TimesheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/timesheet")
@RequiredArgsConstructor
@Slf4j
public class TimesheetController {
    private final TimesheetService timesheetService;

    @CrossOrigin
    @Operation(summary = "This method is used to updated a timesheet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TimesheetResponseDTO.class))}),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))})
    })
    @PutMapping(value = "/{employeeNumber}/{shift}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public StandardResponse<TimesheetResponseDTO> updateTimesheet(@PathVariable String employeeNumber, @PathVariable String shift, @RequestBody TimesheetRequest request){
        TimesheetResponseDTO response = timesheetService.updateTimesheet(employeeNumber, shift, request);
        log.info("Updated timesheet with employee number: {}", employeeNumber);
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Timesheet successfully updated", response);
    }

    @CrossOrigin
    @Operation(summary = "This method is used to delete a timesheet")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "OK"),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))})
//    })
    @DeleteMapping(value = "/{employeeNumber}/{shift}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public StandardResponse<Void> deleteTimesheet(@PathVariable String employeeNumber, @PathVariable String shift) {
        timesheetService.deleteTimesheet(employeeNumber, shift);
        log.info("Deleted timesheet with employee number: {}", employeeNumber);
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Timesheet successfully deleted");
    }

    @CrossOrigin
    @Operation(summary = "This method is used to display the timesheet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TimesheetResponseDTO.class))}),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HttpErrorResponse.class))})
    })

    @GetMapping(value = "")
    public StandardResponse<List<TimesheetResponseDTO>> getAllTimesheets() {
        List<TimesheetResponseDTO> response = timesheetService.getAllTimesheets();
        log.info("List all shifts: {} pallets found", response.size());
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

    @CrossOrigin
    @Operation(summary = "This method is used to display a specific static timesheet")
    @GetMapping("/{employeeNumber}/{shift}")
    public StandardResponse<TimesheetResponseDTO> getShift(@PathVariable String employeeNumber, @PathVariable String shift) {
        TimesheetResponseDTO response = timesheetService.getTimesheet(employeeNumber, shift);
        log.info("Get timesheet with employee number: {}", employeeNumber);
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }
    
}
