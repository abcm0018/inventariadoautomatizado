package com.abcm0018.sai.scanstation.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abcm0018.sai.scanstation.domain.entity.ScanStation;

public interface ScanStationRepository extends JpaRepository<ScanStation, Long> {

    Optional<ScanStation> findByStationCode(String stationCode);
}
