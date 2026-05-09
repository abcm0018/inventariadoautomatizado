package com.abcm0018.sai.email.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailTemplateDTO {
	private Long id;
	private String templateName;
	private String subject;
	private String body;
}
