package com.abcm0018.sai.shared.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ErrorResult extends StandardResult {
	@JsonProperty("errors")
	private List<ResponseError> errors;

	public ErrorResult(boolean status, Integer httpCode, String traceId, String info, List<ResponseError> errors) {
		super(status, httpCode, traceId, info);
		this.errors = errors;
	}
}
