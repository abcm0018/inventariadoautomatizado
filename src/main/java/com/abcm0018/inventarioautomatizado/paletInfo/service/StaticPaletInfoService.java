package com.abcm0018.inventarioautomatizado.paletInfo.service;

import com.abcm0018.inventarioautomatizado.paletInfo.dtos.StaticPaletInfoDTO;
import com.abcm0018.inventarioautomatizado.paletInfo.dtos.StaticPaletInfoRequest;

import java.util.List;

public interface StaticPaletInfoService {
    StaticPaletInfoDTO addInfo(StaticPaletInfoRequest infoRequest);
    void deleteStaticPaletInfo(String sscc);
    StaticPaletInfoDTO updateInfo(String sscc, StaticPaletInfoRequest infoRequest);
    StaticPaletInfoDTO getInfo(String sscc);
    List<StaticPaletInfoDTO> getAllInfo();
}
