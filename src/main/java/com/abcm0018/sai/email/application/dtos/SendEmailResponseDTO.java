package com.abcm0018.sai.email.application.dtos;

import java.time.LocalDateTime;

import com.abcm0018.sai.email.domain.enums.EmailStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendEmailResponseDTO {
	private Long id;
	private String recipient;
	private String subject;

	@JsonProperty(value = "email_type")
	private String emailType;
	private EmailStatus status;

	@JsonProperty(value = "sent_at")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime sentAt;

	@JsonProperty(value = "error_message")
	private String errorMessage;

}
