package com.abcm0018.inventarioautomatizado.productos.service.dto;
import jakarta.validation.constraints.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    @NotBlank(message="EAN cannot be null or empty")
    private String ean;
    @NotBlank(message="Name cannot be null or empty")
    private String name;
    @NotBlank(message="Brand cannot be null or empty")
    private String brand;
    @NotBlank(message="Description cannot be null or empty")
    private String description;
    @NotNull(message="Expiration Day cannot be null")
    private String expirationDay;
    @NotBlank(message="Manufactured In cannot be null or empty")
    private String manufacturedIn;
}
