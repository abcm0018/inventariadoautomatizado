package com.abcm0018.inventarioautomatizado.productos.domain.repository;

import com.abcm0018.inventarioautomatizado.productos.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, CustomProductRepository {
    Optional<Product> findByEan(String ean);
}
