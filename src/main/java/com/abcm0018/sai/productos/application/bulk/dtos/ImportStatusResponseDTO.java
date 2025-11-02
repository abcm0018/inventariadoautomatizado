package com.abcm0018.sai.productos.application.bulk.dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportStatusResponseDTO {
	@JsonProperty("job_id")
	private String jobId;

	private String status;

	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	@JsonProperty("finished_at")
	private LocalDateTime finishedAt;

	private BulkImportResponseDTO result;
}
