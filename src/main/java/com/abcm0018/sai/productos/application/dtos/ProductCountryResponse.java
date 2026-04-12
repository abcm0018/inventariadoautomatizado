package com.abcm0018.sai.productos.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para obtener los estados (solo admin)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCountryResponse {
    List<String> product;
}
