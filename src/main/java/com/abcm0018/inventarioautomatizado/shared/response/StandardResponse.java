package com.abcm0018.inventarioautomatizado.shared.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardResponse <T> {
    private StandardResult result;
    private T data;
}
