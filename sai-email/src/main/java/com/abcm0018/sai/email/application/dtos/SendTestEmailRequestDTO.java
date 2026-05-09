package com.abcm0018.sai.email.application.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendTestEmailRequestDTO {

	@NotBlank(message = "El destinatario no puede estar vacío")
	@Email(message = "Formato de email inválido")
	private String recipient;

	@NotBlank(message = "El asunto no puede estar vacío")
	private String subject;

	@NotBlank(message = "El cuerpo del email no puede estar vacío")
	private String bodyHtml;
}
