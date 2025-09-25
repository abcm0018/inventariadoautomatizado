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
    private String sscc;
    private String ean;
    private String batchNumber; //lote
    private String productUseByDate; //Fecha de consumo preferente
    private String packagingDate;
    private String productionTime;
    private String shift;

    // Static info
    private int weight;
    private int boxesPerPalet;
    private String stackingLimit; // Apilado máximo
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
}
