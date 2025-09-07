package com.abcm0018.inventarioautomatizado.shared.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseError {
    private int code;
    private String message;
}
