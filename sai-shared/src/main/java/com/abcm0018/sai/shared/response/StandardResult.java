package com.abcm0018.sai.shared.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class StandardResult {
	@JsonProperty("status")
	protected boolean status;

	@JsonProperty("http_code")
	protected Integer httpCode;

	@JsonProperty("info")
	protected String info;

	@JsonProperty("trace_id")
	protected String traceId;
}
