package com.abcm0018.inventarioautomatizado.productos.service.mapper;

import com.abcm0018.inventarioautomatizado.productos.domain.entity.Product;
import com.abcm0018.inventarioautomatizado.productos.service.dto.ProductRequest;
import com.abcm0018.inventarioautomatizado.productos.service.dto.ProductResponseDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProductMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ProductMapper() {throw new IllegalStateException("Utility class");}

    public static Product toEntity(ProductRequest request){
        return Product
                .builder()
                .ean(request.getEan())
                .name(request.getName())
                .brand(request.getBrand())
                .description(request.getDescription())
                .manufacturedIn(request.getManufacturedIn())
                .build();
    }

    public static ProductResponseDTO toDTO(Product product){
        return ProductResponseDTO
                .builder()
                .ean(product.getEan())
                .name(product.getName())
                .brand(product.getBrand())
                .description(product.getDescription())
                .expirationDay(dateToString(product.getExpirationDay()))
                .manufacturedIn(product.getManufacturedIn())
                .build();
    }

    public static List<ProductResponseDTO> toDTOList(List<Product> cookieStatisticsList) {
        return cookieStatisticsList.stream().map(ProductMapper::toDTO).toList();
    }

    private static String dateToString(LocalDate date){
        if(date == null){
            return "";
        }
            return date.format(FORMATTER);
    }
}
