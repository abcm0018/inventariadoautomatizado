package com.abcm0018.inventarioautomatizado.palets.controller;

import com.abcm0018.inventarioautomatizado.palets.dtos.PaletDTO;
import com.abcm0018.inventarioautomatizado.palets.dtos.PaletRequest;
import com.abcm0018.inventarioautomatizado.palets.service.PaletService;
import com.abcm0018.inventarioautomatizado.productos.dtos.ProductResponseDTO;
import com.abcm0018.inventarioautomatizado.shared.response.ResponseBuilder;
import com.abcm0018.inventarioautomatizado.shared.response.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/palets")
@RequiredArgsConstructor
@Slf4j
public class PaletsController {

    private final PaletService paletService;

    @CrossOrigin
    @Operation(summary = "This method is used to updated a palet ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponseDTO.class))}),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))})
    })
    @PutMapping(value = "/{sscc}")
    public StandardResponse<PaletDTO> updatePalet(@PathVariable String sscc, @RequestBody PaletRequest request){
        PaletDTO response = paletService.updatePalet(sscc, request);
        log.info("Updated palet with SSCC: {}", sscc);
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Palet successfully updated", response);
    }

    @CrossOrigin
    @Operation(summary = "This method is used to delete a palet")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "OK"),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))})
//    })
    @DeleteMapping(value = "/{sscc}")
    public StandardResponse<Void> deletePalet(@PathVariable String sscc) {
        paletService.deletePalet(sscc);
        log.info("Deleted palet with SSCC: {}", sscc);
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Palet successfully deleted");
    }

    @CrossOrigin
    @Operation(summary = "Obtain all pallets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of pallets obtained", content = @Content(schema = @Schema(implementation = PaletDTO.class))),
            //@ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @GetMapping
    public StandardResponse<List<PaletDTO>> getPalets() {
        List<PaletDTO> response = paletService.getAllPalets();
        log.info("List all palet: {} pallets found", response.size());
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

    @CrossOrigin
    @Operation(summary = "Obtain pallets by EAN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Palets encontrados por EAN", content = @Content(schema = @Schema(implementation = PaletDTO.class))),
            //@ApiResponse(responseCode = "400", description = "Petición inválida"),
            //@ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/ean/{ean}")
    public StandardResponse<List<PaletDTO>> getPaletsByEAN(@PathVariable String ean) {
        List<PaletDTO> response = paletService.getPaletsByEAN(ean);
        log.info("List palet by EAN: {} -> {} pallets found", ean, response.size());
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

    @CrossOrigin
    @Operation(summary = "Obtain pallets by SSCC")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Palet encontrados por SSCC", content = @Content(schema = @Schema(implementation = PaletDTO.class))),
            //@ApiResponse(responseCode = "400", description = "Petición inválida"),
            //@ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/sscc/{sscc}")
    public StandardResponse<PaletDTO> getPaletsBySSCC(@PathVariable String sscc) {
        PaletDTO response = paletService.getPaletsBySSCC(sscc);
        log.info("Palet with SSCC : {} -> {}", sscc, response);
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

    @CrossOrigin
    @Operation(summary = "Obtain pallets by batch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Palets encontrados por lote", content = @Content(schema = @Schema(implementation = PaletDTO.class))),
            //@ApiResponse(responseCode = "400", description = "Petición inválida"),
            //@ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/batchNumber/{batchNumber}")
    public StandardResponse<List<PaletDTO>> getPaletsByBatchNumber(@PathVariable String batchNumber) {
        List<PaletDTO> response = paletService.getPaletsByBatchNumber(batchNumber);
        log.info("List palet by batch number: {} -> {} pallets found", batchNumber, response.size());
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

    @CrossOrigin
    @Operation(summary = "Obtain pallets by shift")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Palets encontrados por turno", content = @Content(schema = @Schema(implementation = PaletDTO.class))),
            //@ApiResponse(responseCode = "400", description = "Petición inválida"),
            //@ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/shift/{shift}")
    public StandardResponse<List<PaletDTO>> getPaletsByShift(@PathVariable String shift) {
        List<PaletDTO> response = paletService.getPaletsByShift(shift);
        log.info("List palet by shift: {} -> {} pallets found", shift, response.size());
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

    @GetMapping(value = "/filter")
    public StandardResponse<List<PaletDTO>> getProductByFilter(
            @RequestParam(required = false) String ean,
            @RequestParam(required = false) String batchNumber,
            @RequestParam(required = false) String productionDate,
            @RequestParam(required = false) String expirationDate,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String shift) {
        List<PaletDTO> response = paletService.findByFilters(ean, batchNumber, productionDate, expirationDate, time, shift);
        log.info("List palets with filters -> ean: {}, batchNumber: {}, productionDate: {}, expirationDate:{}, time: {}, shift: {}", ean, batchNumber, productionDate, expirationDate, time, shift);
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

}
