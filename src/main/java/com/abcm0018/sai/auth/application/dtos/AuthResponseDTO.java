package com.abcm0018.sai.auth.application.dtos;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO implements Serializable {
    private String email;
	private String role;
	private String fullName;
    private String token;
    private String refreshToken;
	private String employeeNumber;
}
