package com.abcm0018.sai.shared.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class SuccessResult extends StandardResult {
	@JsonProperty("updated_elements")
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int updatedElements;

	@JsonProperty("created_elements")
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int createdElements;

	@JsonProperty("deleted_elements")
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private int deletedElements;

	public SuccessResult(boolean status, Integer httpCode, String traceId, String info) {
		super(status, httpCode, traceId, info);
		this.updatedElements = 0;
		this.createdElements = 0;
		this.deletedElements = 0;
	}
}
