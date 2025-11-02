package com.abcm0018.sai.email.application.dtos;

import java.util.HashMap;
import java.util.Map;

import com.abcm0018.sai.email.domain.enums.EmailType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendEmailRequestDTO {

	@NotBlank(message = "El destinatario no puede estar vacío")
	@Email(message = "Formato de email inválido")
	private String recipient;

	@NotNull(message = "El tipo de email es obligatorio")
	private EmailType emailType;

	private Map<String, String> templateParameters = new HashMap<>();

	private String customSubject;
}
