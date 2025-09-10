package com.abcm0018.inventarioautomatizado.productos.dtos;
import jakarta.validation.constraints.*;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    @NotBlank(message="EAN cannot be null or empty")
    @Pattern(regexp = "^[0-9]{14}$", message = "EAN must be a 14-digit numeric code")
    private String ean;
    @NotBlank(message="Name cannot be null or empty")
    private String name;
    @NotBlank(message="Brand cannot be null or empty")
    @Pattern(regexp = "^[A-Za-z]{1,50}$", message = "Brand must contain only letters (max 50 characters)")
    private String brand;
    @NotBlank(message="Description cannot be null or empty")
    private String description;
    @NotNull(message="Expiration Day cannot be null")
    @Pattern(regexp = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$",
            message = "Expiration Day must follow the format dd/MM/yyyy")
    private String expirationDay;
    @NotBlank(message="Manufactured In cannot be null or empty")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Manufactured In must be a valid ISO country code (2 uppercase letters)")
    private String manufacturedIn;
}
