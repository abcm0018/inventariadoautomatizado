package com.abcm0018.inventarioautomatizado.palets.domain.repository;

import com.abcm0018.inventarioautomatizado.palets.domain.entity.Palet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaletRepository extends JpaRepository<Palet, Long> {
    List<Palet> findAllByEan(String ean);
    Optional<Palet> findByEan(String ean);
    Optional<Palet> findBySscc(String sscc);
    Optional<List<Palet>> findByBatchNumber(String batchNumber); //Lote
    Optional<List<Palet>> findByShift(String shift);
}

