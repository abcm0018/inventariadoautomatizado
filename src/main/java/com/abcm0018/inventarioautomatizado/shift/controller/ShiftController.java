package com.abcm0018.inventarioautomatizado.shift.controller;

import com.abcm0018.inventarioautomatizado.shared.response.ResponseBuilder;
import com.abcm0018.inventarioautomatizado.shared.response.StandardResponse;
import com.abcm0018.inventarioautomatizado.shift.dtos.ShiftDTO;
import com.abcm0018.inventarioautomatizado.shift.dtos.ShiftRequest;
import com.abcm0018.inventarioautomatizado.timesheet.dtos.TimesheetResponseDTO;
import com.abcm0018.inventarioautomatizado.shift.service.ShiftService;
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
@RequestMapping(value = "/api/v1/shift")
@RequiredArgsConstructor
@Slf4j
public class ShiftController {
    private final ShiftService shiftService;

    @CrossOrigin
    @Operation(summary = "This method is used to created a shift")
    @PostMapping(value = "")
    @PreAuthorize("hasAuthority('ADMIN')")
    public StandardResponse<ShiftDTO> addShift(ShiftRequest shift){
        ShiftDTO response = shiftService.addShift(shift);
        log.info("Created shift: {}", shift.getShiftType());
        return ResponseBuilder.withUpdatedElements(HttpStatus.CREATED, true, 1, "Shift created successfully", response);
    }

    @CrossOrigin
    @Operation(summary = "This method is used to updated a shift")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ShiftDTO.class))}),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))})
    })
    @PutMapping(value = "/{shiftType}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public StandardResponse<Integer> updateShift(@PathVariable String shiftType, @RequestBody ShiftRequest request){
        int totalUpdated = shiftService.updateShift(shiftType, request);
        log.info("Updated shift with shift type: {}", shiftType);
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, totalUpdated, "Shift successfully updated", totalUpdated);
    }

    @CrossOrigin
    @Operation(summary = "This method is used to delete a shift")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "OK"),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))})
//    })
    @DeleteMapping(value = "/{shiftType}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public StandardResponse<Integer> deleteShift(@PathVariable String shiftType) {
        int totalDeleted = shiftService.deleteShift(shiftType);
        log.info("Deleted shift with shift type: {}", shiftType);
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, totalDeleted, "Shift successfully deleted");
    }

    @CrossOrigin
    @Operation(summary = "This method is used to display the shift")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TimesheetResponseDTO.class))}),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HttpErrorResponse.class))})
    })

    @GetMapping(value = "")
    public StandardResponse<List<ShiftDTO>> getAllShifts() {
        List<ShiftDTO> response = shiftService.getAllShift();
        log.info("List all shifts: {} pallets found", response.size());
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

    @CrossOrigin
    @Operation(summary = "This method is used to display a specific static palet info")
    @GetMapping("/{shiftType}")
    public StandardResponse<ShiftDTO> getShift(@PathVariable String shiftType) {
        ShiftDTO response = shiftService.getInfo(shiftType);
        log.info("Get shift with shift type: {}", shiftType);
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }
}
