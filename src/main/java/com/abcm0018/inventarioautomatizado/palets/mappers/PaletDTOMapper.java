package com.abcm0018.inventarioautomatizado.palets.mappers;

import com.abcm0018.inventarioautomatizado.palets.domain.entity.Palet;
import com.abcm0018.inventarioautomatizado.paletInfo.domain.entity.StaticPaletInfo;
import com.abcm0018.inventarioautomatizado.palets.dtos.PaletDTO;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaletDTOMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PaletDTOMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static PaletDTO toDTO(Palet palet, StaticPaletInfo staticInfo) {
        if (palet == null && staticInfo == null) {
            return null;
        }

        return PaletDTO.builder()
                // Datos de Palet
                .ean(palet != null ? palet.getEan() : null)
                .batchNumber(palet != null ? palet.getBatchNumber() : null)
                .packagingDate(palet != null && palet.getPackagingDate() != null
                        ? palet.getPackagingDate().format(DATE_FORMATTER) : null)
                .productUseByDate(palet != null && palet.getProductUseByDate() != null
                        ? palet.getProductUseByDate().format(DATE_FORMATTER) : null)
                .productionTime(palet != null ? palet.getProductionTime() : null)
                .sscc(palet != null ? palet.getSscc() : null)
                .shift(palet != null ? palet.getShift() : null)

                // Datos de StaticPaletInfo
                .weight(staticInfo != null && staticInfo.getWeight() != null
                        ? staticInfo.getWeight().intValue() : 0)
                .boxesPerPalet(staticInfo != null ? staticInfo.getBoxesPerPalet() : 0)
                .stackingLimit(staticInfo != null ? staticInfo.getStackingLimit() : null)
                .createdAt(staticInfo != null && staticInfo.getCreatedAt() != null
                        ? staticInfo.getCreatedAt().format(DATE_FORMATTER) : null)
                .updatedAt(staticInfo != null && staticInfo.getUpdatedAt() != null
                        ? staticInfo.getUpdatedAt().format(DATE_FORMATTER) : null)
                .deletedAt(staticInfo != null && staticInfo.getDeletedAt() != null
                        ? staticInfo.getDeletedAt().format(DATE_FORMATTER) : null)
                .build();
    }

    public static List<PaletDTO> toDTOList(List<Palet> palets, StaticPaletInfo staticInfo) {
        if(palets == null || palets.isEmpty()) {
            return List.of();
        }
        return palets.stream().map(palet -> toDTO(palet, staticInfo)).toList();
    }

}
