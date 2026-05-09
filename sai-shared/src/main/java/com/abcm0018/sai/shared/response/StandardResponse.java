package com.abcm0018.sai.shared.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardResponse <T> {
	@JsonUnwrapped
    private StandardResult result;

	@JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
}
