package com.abcm0018.inventarioautomatizado.palets.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaletDTO {
    private String ean;
    private String batchNumber; //lote
    private String expirationDate;
    private String productionDate;
    private String time;
    private String sscc;

    // Static info
    private int weight;
    private int boxesPerPalet;
    private String stackingLimit; // Apilado máximo
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
}
