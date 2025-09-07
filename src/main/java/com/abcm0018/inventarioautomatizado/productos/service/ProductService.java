package com.abcm0018.inventarioautomatizado.productos.service;

import com.abcm0018.inventarioautomatizado.productos.service.dto.ProductRequest;
import com.abcm0018.inventarioautomatizado.productos.service.dto.ProductResponseDTO;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProductService {
    ProductResponseDTO addProduct(ProductRequest productRequest);
    void deleteProduct(String ean);
    ProductResponseDTO updateProduct(String ean, ProductRequest product);
    List<ProductResponseDTO> getAllProducts();
    List<ProductResponseDTO> findByFilters(String ean, String brand,
                                           String initExpirationDate, String endExpirationDate, String expirationDate, String manufacturedIn);
}
