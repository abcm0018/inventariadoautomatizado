package com.abcm0018.sai.productos.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO para obtener los estados (solo admin)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusResponse {
    Set<String> statusProduct;
}
