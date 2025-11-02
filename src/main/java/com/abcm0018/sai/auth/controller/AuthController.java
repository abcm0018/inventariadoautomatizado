package com.abcm0018.sai.auth.controller;

import com.abcm0018.sai.auth.application.dtos.AuthResponseDTO;
import com.abcm0018.sai.auth.application.dtos.LoginRequest;
import com.abcm0018.sai.auth.application.service.AuthService;
import com.abcm0018.sai.shared.response.ResponseBuilder;
import com.abcm0018.sai.shared.response.StandardResponse;
import com.abcm0018.sai.shared.utils.JWTUtils;

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
import org.springframework.web.context.request.WebRequest;

@RestController
@RequestMapping(value = "/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @CrossOrigin
    @Operation(summary = "This method is for logging in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content =
                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
                    @Schema(implementation = AuthResponseDTO.class))}),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))})
    })
    @PostMapping(value = "/login")
    public StandardResponse<AuthResponseDTO> login(@RequestBody LoginRequest request) {
        AuthResponseDTO response = authService.loginUser(request);
        log.info("User with role {} and employee number {} has successfully logged in", response.getRole(), response.getEmployeeNumber());
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Successfully response", response);
    }

    @CrossOrigin
    @GetMapping(value="/refreshToken")
    public StandardResponse<AuthResponseDTO> renewToken(WebRequest request) {
        String userToken = JWTUtils.extractTokenFromRequest(request);
        AuthResponseDTO response  = authService.renewUserToken(userToken);
        log.info("User with role {} and employee number {} has renew token", response.getRole(), response.getEmployeeNumber());
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Token renewed successfully", response);
    }

    @CrossOrigin
    @Operation(summary = "This method is for logging out")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content =
                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
                    @Schema(implementation = String.class))}),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))})
    })

    @GetMapping(value = "/logout")
    public StandardResponse<String> logout(WebRequest request) {
        String response = authService.logout(JWTUtils.extractTokenFromRequest(request));
        log.info("User logged out");
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Logout successful", response);
    }

}
