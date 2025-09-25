package com.abcm0018.inventarioautomatizado.auth.controller;

import com.abcm0018.inventarioautomatizado.auth.dtos.AuthResponseDTO;
import com.abcm0018.inventarioautomatizado.auth.dtos.LoginRequest;
import com.abcm0018.inventarioautomatizado.auth.dtos.PasswordResetRequest;
import com.abcm0018.inventarioautomatizado.auth.dtos.RegisterUserRequest;
import com.abcm0018.inventarioautomatizado.auth.service.AuthService;
import com.abcm0018.inventarioautomatizado.shared.response.ResponseBuilder;
import com.abcm0018.inventarioautomatizado.shared.response.StandardResponse;
import com.abcm0018.inventarioautomatizado.shared.utils.JWTUtils;
import com.abcm0018.inventarioautomatizado.users.dtos.UserResponseDTO;
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
import org.springframework.web.context.request.WebRequest;

@RestController
@RequestMapping(value = "/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @CrossOrigin
    @Operation(summary = "This method is used for the registration of a operator user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "409", description = "Conflict", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error", content =
//                    {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema =
//                    @Schema(implementation = HttpErrorResponse.class))})
    })

    @PostMapping(value = "/register")
    @PreAuthorize("hasAuthority('ADMIN')")
    public StandardResponse<Integer> registerUser(@RequestBody RegisterUserRequest data) {
        Integer result = authService.addUser(data);
        log.info("Created a user with employee number: {}", data.getEmployeeNumber());
        return ResponseBuilder.withUpdatedElements(HttpStatus.CREATED, true, result, "Successfully response");
    }

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
        log.info("User with role {} and employee number {} has successfully logged in",
                response.getUser().getRole(),
                response.getUser().getEmployeeNumber());
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Successfully response", response);
    }

    @CrossOrigin
    @GetMapping(value="/refreshToken")
    public StandardResponse<AuthResponseDTO> renewToken(WebRequest request) {
        String userToken = JWTUtils.extractTokenFromRequest(request);
        AuthResponseDTO response  = authService.renewUserToken(userToken);
        log.info("User with role {} and employee number {} has renew token",
                response.getUser().getRole(),
                response.getUser().getEmployeeNumber());
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Token renewed successfully", response);
    }

    @CrossOrigin
    @PutMapping(value = "/resetPassword")
    public StandardResponse<UserResponseDTO> changePassword(@RequestBody PasswordResetRequest passResetInfo){
        UserResponseDTO response = authService.updatePassword(passResetInfo);
        log.info("User with an employee number {} has updated password",
                response.getEmployeeNumber());
        return ResponseBuilder.withUpdatedElements(HttpStatus.OK, true, 1, "Password updated successfully", response);
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
