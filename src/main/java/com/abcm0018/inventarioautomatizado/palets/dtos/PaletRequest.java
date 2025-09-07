package com.abcm0018.inventarioautomatizado.palets.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaletRequest {
    private String ean;
    private String batchNumber;   // lote
    private String productionDate;
    private String expirationDate;
    private String time;
    private String sscc;
    private String shift;
}
