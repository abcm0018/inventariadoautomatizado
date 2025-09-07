package com.abcm0018.inventarioautomatizado.palets.controller;

import com.abcm0018.inventarioautomatizado.palets.dtos.PaletDTO;
import com.abcm0018.inventarioautomatizado.palets.service.PaletService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/palets")
@RequiredArgsConstructor
@Slf4j
public class PaletsController {

    private final PaletService paletService;

    @CrossOrigin
    @Operation(summary = "Obtener todas los palets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de palets obtenida", content = @Content(schema = @Schema(implementation = PaletDTO.class))),
            //@ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @GetMapping
    public StandardResponse<List<PaletDTO>> getPalets() {
        List<PaletDTO> response = paletService.getAllPalets();
        log.info("List all palet: ");
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

    @CrossOrigin
    @Operation(summary = "Obtener palets por EAN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Palets encontrados por EAN", content = @Content(schema = @Schema(implementation = PaletDTO.class))),
            //@ApiResponse(responseCode = "400", description = "Petición inválida"),
            //@ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/ean/{ean}")
    public StandardResponse<List<PaletDTO>> getPaletsByEAN(@PathVariable String ean) {
        List<PaletDTO> response = paletService.getPaletsByEAN(ean);
        log.info("List palet by EAN: ");
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

    @CrossOrigin
    @Operation(summary = "Obtener palet por SSCC")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Palet encontrados por SSCC", content = @Content(schema = @Schema(implementation = PaletDTO.class))),
            //@ApiResponse(responseCode = "400", description = "Petición inválida"),
            //@ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/sscc/{sscc}")
    public StandardResponse<PaletDTO> getPaletsBySSCC(@PathVariable String sscc) {
        PaletDTO response = paletService.getPaletsBySSCC(sscc);
        log.info("Palet with SSCC : ");
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

    @CrossOrigin
    @Operation(summary = "Obtener palets por lote")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Palets encontrados por lote", content = @Content(schema = @Schema(implementation = PaletDTO.class))),
            //@ApiResponse(responseCode = "400", description = "Petición inválida"),
            //@ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/batchNumber/{batchNumber}")
    public StandardResponse<List<PaletDTO>> getPaletsByBatchNumber(@PathVariable String batchNumber) {
        List<PaletDTO> response = paletService.getPaletsByBatchNumber(batchNumber);
        log.info("List palet by batch number: ");
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

}
