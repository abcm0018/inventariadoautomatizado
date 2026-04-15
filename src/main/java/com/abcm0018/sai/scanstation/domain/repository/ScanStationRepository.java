package com.abcm0018.sai.scanstation.domain.repository;

import com.abcm0018.sai.scanstation.domain.entity.ScanStation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanStationRepository extends JpaRepository<ScanStation, Long> {
}
