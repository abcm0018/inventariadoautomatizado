package com.abcm0018.inventarioautomatizado.productos.service;

import com.abcm0018.inventarioautomatizado.productos.dtos.ProductRequest;
import com.abcm0018.inventarioautomatizado.productos.dtos.ProductResponseDTO;

import java.util.List;

public interface ProductService {
    ProductResponseDTO addProduct(ProductRequest productRequest);
    void deleteProduct(String ean);
    ProductResponseDTO updateProduct(String ean, ProductRequest product);
    List<ProductResponseDTO> getAllProducts();
    List<ProductResponseDTO> findByFilters(String ean, String name, String brand, String manufacturedIn);
}
