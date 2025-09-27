package com.abcm0018.inventarioautomatizado.paletInfo.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StaticPaletInfoRequest {
    @NotBlank(message = "El SSCC no puede estar vacío o nulo")
    @Size(message = "El SSCC debe tener exactamente 18 dígitos")
    @Pattern(regexp = "\\d{18}", message = "El SSCC debe contener solo números")
    private String sscc;
    @NotBlank(message = "El peso no puede ser nulo o vacío")
    @Pattern(regexp = "^(?:0|[1-9]\\d{0,2})(?:\\.\\d{1,3})?$", message = "Peso inválido (máx 999.999)")
    private BigDecimal weight;
    @NotBlank(message = "El número de cajas por palet no puede ser nulo")
    private Integer boxesPerPalet;
    @NotBlank(message = "El apilado no puede estar vacío")
    private String stackingLimit;
}
