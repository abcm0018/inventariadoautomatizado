package com.abcm0018.inventarioautomatizado.productos.dtos;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDTO {
    private String ean;
    private String name;
    private String brand;
    private String description;
    private String manufacturedIn;
    private BigDecimal weight;
    private Integer boxesPerPalet;
    private String stackingLimit;
    private String formatCode;
}
