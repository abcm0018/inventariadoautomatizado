package com.abcm0018.inventarioautomatizado.productos.service.dto;

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
    private String expirationDay;
    private String manufacturedIn;
}
