package com.abcm0018.inventarioautomatizado.productos.domain.repository;

import com.abcm0018.inventarioautomatizado.productos.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, CustomProductRepository {
    Optional<Product> findByEan(String ean);
}
