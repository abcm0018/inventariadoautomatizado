package com.abcm0018.sai.productos.application.bulk.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportJobResponseDTO {
	private String jobId;
	private String status;
	private String message;
}
