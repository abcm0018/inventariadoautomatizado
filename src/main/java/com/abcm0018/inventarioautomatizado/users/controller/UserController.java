package com.abcm0018.inventarioautomatizado.users.controller;

import com.abcm0018.inventarioautomatizado.productos.dtos.ProductRequest;
import com.abcm0018.inventarioautomatizado.productos.dtos.ProductResponseDTO;
import com.abcm0018.inventarioautomatizado.shared.response.ResponseBuilder;
import com.abcm0018.inventarioautomatizado.shared.response.StandardResponse;
import com.abcm0018.inventarioautomatizado.users.dtos.UserRequest;
import com.abcm0018.inventarioautomatizado.users.dtos.UserResponseDTO;
import com.abcm0018.inventarioautomatizado.users.service.UserService;
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
@RequestMapping(value = "/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @CrossOrigin
    @Operation(summary = "This method is used to updated a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponseDTO.class))}),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))})
    })
    @PutMapping(value = "/{employeeNumber}")
    public StandardResponse<UserResponseDTO> updateUser(@PathVariable String employeeNumber, @RequestBody UserRequest data){
        UserResponseDTO response = userService.updateUser(employeeNumber, data);
        log.info("Updated product with employeeNumber: {}", employeeNumber);
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "User successfully updated", response);
    }

    @CrossOrigin
    @Operation(summary = "This method is used to delete a user")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "OK"),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))})
//    })
    @DeleteMapping(value = "/{employeeNumber}")
    public StandardResponse<Void> deleteUser(@PathVariable String employeeNumber) {
        userService.deleteUser(employeeNumber);
        log.info("Deleted product with employeeNumber: {}", employeeNumber);
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Product successfully deleted");
    }

    @CrossOrigin
    @Operation(summary = "This method is used to display the users ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductResponseDTO.class))}),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = HttpErrorResponse.class))})
    })

    @GetMapping(value = "")
    public StandardResponse<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> response = userService.getAllUsers();
        log.info("List all users: {} found", response.size());
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }

    @CrossOrigin
    @Operation(summary = "This methos is used to display a specific user")
    public StandardResponse<UserResponseDTO> getUser(@PathVariable String employeeNumber) {
        UserResponseDTO response = userService.getUser(employeeNumber);
        log.info("User with employeeNumber: {}", employeeNumber);
        return ResponseBuilder.with(HttpStatus.OK, true, "Successful response", response);
    }
}
