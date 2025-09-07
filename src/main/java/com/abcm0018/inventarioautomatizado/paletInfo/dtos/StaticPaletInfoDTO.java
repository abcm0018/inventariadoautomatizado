package com.abcm0018.inventarioautomatizado.paletInfo.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StaticPaletInfoDTO {
    private String sscc;
    private BigDecimal weight;
    private Integer boxesPerPalet;
    private String stackingLimit;
    private String status;
}
