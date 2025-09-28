package com.abcm0018.inventarioautomatizado.palets.mappers;

import com.abcm0018.inventarioautomatizado.palets.domain.entity.Palet;
import com.abcm0018.inventarioautomatizado.palets.dtos.PaletDTO;
import com.abcm0018.inventarioautomatizado.palets.dtos.PaletRequest;
import com.abcm0018.inventarioautomatizado.productos.domain.entity.Product;
import com.abcm0018.inventarioautomatizado.productos.dtos.ProductResponseDTO;
import com.abcm0018.inventarioautomatizado.productos.mappers.ProductMapper;

import java.util.List;

public class PaletMapper {
    private PaletMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static Palet toEntity(PaletRequest request){

        return Palet
                .builder()
                .ean(request.getEan())
                .batchNumber(request.getBatchNumber())
                .sscc(request.getSscc())
                .productionTime(request.getProductionTime())
                .shift(request.getShift())
                .build();
    }

    public static PaletDTO toDTO(Palet palet){
        return PaletDTO
                .builder()
                .ean(palet.getEan())
                .batchNumber(palet.getBatchNumber())
                .sscc(palet.getSscc())
                .productionTime(palet.getProductionTime())
                .productUseByDate(String.valueOf(palet.getProductUseByDate()))
                .packagingDate(String.valueOf(palet.getPackagingDate()))
                .shift(palet.getShift())
                .build();
    }

    public static List<PaletDTO> toDTOList(List<Palet> paletList) {
        return paletList.stream().map(PaletMapper::toDTO).toList();
    }
}
