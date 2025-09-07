package com.abcm0018.inventarioautomatizado.paletInfo.controller;

import com.abcm0018.inventarioautomatizado.paletInfo.dtos.StaticPaletInfoDTO;
import com.abcm0018.inventarioautomatizado.paletInfo.dtos.StaticPaletInfoRequest;
import com.abcm0018.inventarioautomatizado.paletInfo.service.StaticPaletInfoService;
import com.abcm0018.inventarioautomatizado.productos.service.dto.ProductResponseDTO;
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
@RequestMapping(value = "/api/v1/static_palet_info")
@RequiredArgsConstructor
@Slf4j
public class StaticPaletInfoController {

    private final StaticPaletInfoService staticPaletInfoService;

    @CrossOrigin
    @Operation(summary = "This method is used to created a static palet info")
    @PostMapping(value = "")
    public StandardResponse<StaticPaletInfoDTO> addProduct(StaticPaletInfoRequest data){
        StaticPaletInfoDTO response = staticPaletInfoService.addInfo(data);
        log.info("Created info: {}", data.getSscc());
        return ResponseBuilder.withUpdatedElements(HttpStatus.CREATED, true, 1, "Static palet info created successfully", response);
    }

    @CrossOrigin
    @Operation(summary = "This method is used to updated a static palet info")
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
    public StandardResponse<StaticPaletInfoDTO> updateStaticPaletInfo(@PathVariable String sscc, @RequestBody StaticPaletInfoRequest data){
        StaticPaletInfoDTO response = staticPaletInfoService.updateInfo(sscc, data);
        log.info("Updated static palet info with sscc: {}", sscc);
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Static Palet Info successfully updated", response);
    }

    @CrossOrigin
    @Operation(summary = "This method is used to delete a static palet info")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "OK"),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))})
//    })
    @DeleteMapping(value = "/{ean}")
    public StandardResponse<Void> deleteStaticPaletInfo(@PathVariable String sscc) {
        staticPaletInfoService.deleteStaticPaletInfo(sscc);
        log.info("Deleted static palet info with sscc: {}", sscc);
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Static palet info successfully deleted");
    }

    @CrossOrigin
    @Operation(summary = "This method is used to display the static palet info ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponseDTO.class))}),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HttpErrorResponse.class))})
    })

    @GetMapping(value = "")
    public StandardResponse<List<StaticPaletInfoDTO>> getAllProducts() {
        List<StaticPaletInfoDTO> response = staticPaletInfoService.getAllInfo();
        log.info("List all static palet info: ");
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

    @CrossOrigin
    @Operation(summary = "This method is used to display a specific static palet info")
    @GetMapping("/{sscc}")
    public StandardResponse<StaticPaletInfoDTO> getStaticPaletInfo(@PathVariable String sscc) {
        StaticPaletInfoDTO response = staticPaletInfoService.getInfo(sscc);
        log.info("Get static palet info with SSCC: {}", sscc);
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }
}
