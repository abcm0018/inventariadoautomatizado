package com.abcm0018.sai.email.application.dtos;

import com.abcm0018.sai.email.domain.enums.EmailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailLogFilterDTO {
	private String recipient;
	private String emailType;
	private EmailStatus status;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
}
