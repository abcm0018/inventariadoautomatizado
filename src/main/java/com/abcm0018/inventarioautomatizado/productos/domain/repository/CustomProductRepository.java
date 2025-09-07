package com.abcm0018.inventarioautomatizado.productos.domain.repository;

import com.abcm0018.inventarioautomatizado.productos.domain.entity.Product;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomProductRepository {

        List<Product> findProducts(@Param("ean") String ean,
                                   @Param("brand") String brand,
                                   @Param("initExpirationDate") String initExpirationDate,
                                   @Param("endExpirationDate") String endExpirationDate,
                                   @Param("expirationDate") String expirationDate,
                                   @Param("manufacturedIn") String manufacturedIn);
}
