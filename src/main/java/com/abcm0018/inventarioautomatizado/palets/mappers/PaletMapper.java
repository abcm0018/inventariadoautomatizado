package com.abcm0018.inventarioautomatizado.palets.mappers;

import com.abcm0018.inventarioautomatizado.palets.domain.entity.Palet;
import com.abcm0018.inventarioautomatizado.palets.dtos.PaletRequest;

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
                .time(request.getTime())
                .shift(request.getShift())
                .build();
    }

}
