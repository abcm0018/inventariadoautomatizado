package com.abcm0018.inventarioautomatizado.paletInfo.domain.repository;

import com.abcm0018.inventarioautomatizado.paletInfo.domain.entity.StaticPaletInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaticPaletInfoRepository extends JpaRepository<StaticPaletInfo, Long> {
    Optional<StaticPaletInfo> findBySscc(String sscc);

}
