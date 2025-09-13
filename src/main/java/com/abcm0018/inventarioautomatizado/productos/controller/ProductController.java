package com.abcm0018.inventarioautomatizado.productos.controller;

import com.abcm0018.inventarioautomatizado.shared.response.ResponseBuilder;
import com.abcm0018.inventarioautomatizado.shared.response.StandardResponse;
import com.abcm0018.inventarioautomatizado.productos.service.ProductService;
import com.abcm0018.inventarioautomatizado.productos.dtos.ProductRequest;
import com.abcm0018.inventarioautomatizado.productos.dtos.ProductResponseDTO;
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
@RequestMapping(value = "/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final ProductService productService;

    @CrossOrigin
    @Operation(summary = "This method is used to created a product")
    @PostMapping(value = "")
    public StandardResponse<ProductResponseDTO> addProduct(ProductRequest data){
        ProductResponseDTO response = productService.addProduct(data);
        log.info("Created product: {}", data.getEan());
        return ResponseBuilder.withUpdatedElements(HttpStatus.CREATED, true, 1, "Product created successfully", response);
    }

    @CrossOrigin
    @Operation(summary = "This method is used to updated a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponseDTO.class))}),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))})
    })
    @PutMapping(value = "/{ean}")
    public StandardResponse<ProductResponseDTO> updateProduct(@PathVariable String ean, @RequestBody ProductRequest data){
        ProductResponseDTO response = productService.updateProduct(ean, data);
        log.info("Updated product with ean: {}", ean);
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Product successfully updated", response);
    }

    @CrossOrigin
    @Operation(summary = "This method is used to delete a product")
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
    public StandardResponse<Void> deleteProduct(@PathVariable String ean) {
        productService.deleteProduct(ean);
        log.info("Deleted product with ean: {}", ean);
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Product successfully deleted");
    }

    @CrossOrigin
    @Operation(summary = "This method is used to display the products ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponseDTO.class))}),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HttpErrorResponse.class))})
    })

    @GetMapping(value = "")
    public StandardResponse<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> response = productService.getAllProducts();
        log.info("List all pallets: {} found", response.size());
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

    @GetMapping(value = "/filter")
    public StandardResponse<List<ProductResponseDTO>> getProductByFilter(
            @RequestParam(required = false) String ean,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String initDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String expirationDate,
            @RequestParam(required = false) String manufacturedIn) {
        List<ProductResponseDTO> response = productService.findByFilters(ean, brand, initDate, endDate, expirationDate, manufacturedIn);
        log.info("List products with filters -> ean: {}, brand: {}, initExpirationDate: {}, endExpirationDate: {}, expirationDate:{}, manufacturedIn: {}", ean, brand, initDate, endDate, expirationDate, manufacturedIn);
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

}
