package com.abcm0018.inventarioautomatizado.palets.domain.repository;

import com.abcm0018.inventarioautomatizado.palets.domain.entity.Palet;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomPaletRepository {

    List<Palet> findPalets(@Param("ean") String ean,
                           @Param("batchNumber") String batchNumber,
                           @Param("productionDate") String packagingDate,
                           @Param("expirationDate") String productUseByDate,
                           @Param("time") String time,
                           @Param("shift") String shift);
}
