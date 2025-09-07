package com.abcm0018.inventarioautomatizado.paletInfo.mapper;

import com.abcm0018.inventarioautomatizado.paletInfo.domain.entity.StaticPaletInfo;
import com.abcm0018.inventarioautomatizado.paletInfo.dtos.StaticPaletInfoDTO;
import com.abcm0018.inventarioautomatizado.paletInfo.dtos.StaticPaletInfoRequest;

import java.util.List;


public class StaticPaletInfoMapper {

    private StaticPaletInfoMapper() {throw new IllegalStateException("Utility class");}

    public static StaticPaletInfo toEntity(StaticPaletInfoRequest request){

        return StaticPaletInfo
                .builder()
                .sscc(request.getSscc())
                .weight(request.getWeight())
                .boxesPerPalet(request.getBoxesPerPalet())
                .stackingLimit(request.getStackingLimit())
                .build();
    }

    public static StaticPaletInfoDTO toDTO(StaticPaletInfo info){
        return StaticPaletInfoDTO
                .builder()
                .sscc(info.getSscc())
                .weight(info.getWeight())
                .boxesPerPalet(info.getBoxesPerPalet())
                .stackingLimit(info.getStackingLimit())
                .build();
    }

    public static List<StaticPaletInfoDTO> toDTOList(List<StaticPaletInfo> staticPaletInfos) {
        return staticPaletInfos.stream().map(StaticPaletInfoMapper::toDTO).toList();
    }

}
