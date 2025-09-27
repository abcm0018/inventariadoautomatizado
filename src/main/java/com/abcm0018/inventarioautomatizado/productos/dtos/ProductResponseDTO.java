package com.abcm0018.inventarioautomatizado.productos.dtos;

import lombok.*;

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
}
