package com.abcm0018.sai.palets.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abcm0018.sai.palets.domain.entity.Palet;
import com.abcm0018.sai.palets.domain.entity.PalletScan;

public interface PalletScanRepository extends JpaRepository<PalletScan, Long> {

    List<PalletScan> findByPallet(Palet pallet);

    Optional<PalletScan> findFirstByPalletOrderByScannedAtDesc(Palet pallet);
}
