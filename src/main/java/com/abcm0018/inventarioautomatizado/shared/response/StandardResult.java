package com.abcm0018.inventarioautomatizado.shared.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardResult {
    @JsonProperty("status")
    protected boolean status;
    @JsonProperty("http_code")
    protected Integer httpCode;
    @JsonProperty("errors")
    protected List<ResponseError> errors;
    @JsonProperty("trace_id")
    protected String traceId;
    @JsonProperty("info")
    protected String info;
    @JsonProperty("updated_elements")
    protected int updatedElements;
}
