package com.abcm0018.inventarioautomatizado.productos.mappers;

import com.abcm0018.inventarioautomatizado.productos.domain.entity.Product;
import com.abcm0018.inventarioautomatizado.productos.dtos.ProductRequest;
import com.abcm0018.inventarioautomatizado.productos.dtos.ProductResponseDTO;

import java.util.List;

public class ProductMapper {

    private ProductMapper() {throw new IllegalStateException("Utility class");}

    public static Product toEntity(ProductRequest request){
        return Product
                .builder()
                .ean(request.getEan())
                .name(request.getName())
                .brand(request.getBrand())
                .description(request.getDescription())
                .manufacturedIn(request.getManufacturedIn())
                .stackingLimit(request.getStackingLimit())
                .boxesPerPalet(request.getBoxesPerPalet())
                .weight(request.getWeight())
                .formatCode(request.getFormatCode())
                .build();
    }

    public static ProductResponseDTO toDTO(Product product){
        return ProductResponseDTO
                .builder()
                .ean(product.getEan())
                .name(product.getName())
                .brand(product.getBrand())
                .description(product.getDescription())
                .manufacturedIn(product.getManufacturedIn())
                .boxesPerPalet(product.getBoxesPerPalet())
                .weight(product.getWeight())
                .formatCode(product.getFormatCode())
                .stackingLimit(product.getStackingLimit())
                .build();
    }

    public static List<ProductResponseDTO> toDTOList(List<Product> productList) {
        return productList.stream().map(ProductMapper::toDTO).toList();
    }

}
