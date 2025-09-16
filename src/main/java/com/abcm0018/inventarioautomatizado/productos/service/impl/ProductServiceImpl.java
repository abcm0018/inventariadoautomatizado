package com.abcm0018.inventarioautomatizado.productos.service.impl;

import com.abcm0018.inventarioautomatizado.shared.config.InventariadoCacheConfig;
import com.abcm0018.inventarioautomatizado.productos.constants.CustomErrorCode;
import com.abcm0018.inventarioautomatizado.productos.domain.entity.Product;
import com.abcm0018.inventarioautomatizado.productos.domain.repository.ProductRepository;
import com.abcm0018.inventarioautomatizado.productos.exceptions.ProductServiceException;
import com.abcm0018.inventarioautomatizado.productos.service.ProductService;
import com.abcm0018.inventarioautomatizado.productos.dtos.ProductRequest;
import com.abcm0018.inventarioautomatizado.productos.dtos.ProductResponseDTO;
import com.abcm0018.inventarioautomatizado.productos.mappers.ProductMapper;
import com.abcm0018.inventarioautomatizado.shared.utils.ProductUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@CacheConfig(cacheNames = InventariadoCacheConfig.PRODUCT_INFO)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @CacheEvict(allEntries = true)
    public ProductResponseDTO addProduct(ProductRequest productRequest) {

        Optional<Product> existProduct = productRepository.findByEan(productRequest.getEan());

        if (existProduct.isPresent()) {
            throw new ProductServiceException(CustomErrorCode.BAD_REQUEST,
                    "There is already a product with the EAN: " + productRequest.getEan(),
                    HttpStatus.BAD_REQUEST);
        }

        Product product = ProductMapper.toEntity(productRequest);

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(null);

        Product savedProduct = productRepository.save(product);

        return ProductMapper.toDTO(savedProduct);
    }

    @Override
    @CacheEvict(allEntries = true)
    public ProductResponseDTO updateProduct(String ean, ProductRequest data) {
        if(!ProductUtils.isEANValid(ean)){
            throw new ProductServiceException(CustomErrorCode.BAD_REQUEST, "The EAN does not have a valid format.", HttpStatus.BAD_REQUEST);
        }

        Product product = productRepository.findByEan(ean)
                .orElseThrow(() -> new ProductServiceException(CustomErrorCode.NOT_FOUND,"Product not found: " + ean, HttpStatus.NOT_FOUND));

        Product productToUpdate = ProductMapper.toEntity(data);

        productToUpdate.setId(product.getId());
        productToUpdate.setCreatedAt(product.getCreatedAt());
        productToUpdate.setUpdatedAt(LocalDateTime.now());

        Product udpatedProduct = productRepository.save(productToUpdate);
        return ProductMapper.toDTO(udpatedProduct);
    }


    @Override
    @CacheEvict(allEntries = true)
    public void deleteProduct(String ean) {
        if(!ProductUtils.isEANValid(ean)){
            throw new ProductServiceException(CustomErrorCode.BAD_REQUEST, "The EAN does not have a valid format.", HttpStatus.BAD_REQUEST);
        }
        Product product = productRepository.findByEan(ean)
                .orElseThrow(() -> new ProductServiceException(CustomErrorCode.NOT_FOUND,
                        "Product not found with EAN: " + ean, HttpStatus.NOT_FOUND));
        productRepository.delete(product);
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        List<Product> productList = productRepository.findAll();
        return  ProductMapper.toDTOList(productList);
    }

    @Override
    @Cacheable(key = "{#root.methodName, #ean, #brand, #name, #manufacturedIn}")
    public List<ProductResponseDTO> findByFilters(String ean, String brand, String name, String manufacturedIn) {

        validateInputFilter(ean, brand, manufacturedIn);
        final List<Product> products = productRepository.findProducts(ean, brand, name, manufacturedIn);
        return ProductMapper.toDTOList(products);
    }

    private void validateInputFilter(String ean, String brand, String manufacturedIn) throws ProductServiceException {
        // Regex para EAN-14 (14 dígitos numéricos)
        String eanRegex = "^[0-9]{14}$";

        // Regex para brand (letras, números, espacios y guiones)
        String brandRegex = "^[A-Za-z]{1,50}$";

        // Regex para manufacturedIn (ejemplo: dos letras mayúsculas, código país ISO)
        String countryRegex = "^[A-Z]{2}$";

        // Validaciones
        if (ean != null && !ean.matches(eanRegex)) {
            throw new ProductServiceException(CustomErrorCode.BAD_REQUEST, "Invalid EAN. Must contain 14 digits.", HttpStatus.BAD_REQUEST);
        }

        if (brand != null && !brand.matches(brandRegex)) {
            throw new ProductServiceException(CustomErrorCode.BAD_REQUEST, "Invalid brand name. Letters only.", HttpStatus.BAD_REQUEST);
        }

        if (manufacturedIn != null && !manufacturedIn.matches(countryRegex)) {
            throw new ProductServiceException(CustomErrorCode.BAD_REQUEST, "Invalid country code. Must be ISO Alpha-2 (e.g. ES, FR, US).", HttpStatus.BAD_REQUEST);
        }
    }

}
