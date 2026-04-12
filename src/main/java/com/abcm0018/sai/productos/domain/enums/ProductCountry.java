package com.abcm0018.sai.productos.domain.enums;

import lombok.Getter;

import java.util.List;

@Getter
public enum ProductCountry {
    ES("España"), //España
    PT("Portugal"), //Portugal
    FR("Francia"), //Francia
    IT("Italia"), //Italia
    DE("Dinamarca"), //Dinamarca
    UK("Reino Unido"); //Reino Unido

    private final String displayCountry;

    ProductCountry(String displayCountry) {
        this.displayCountry = displayCountry;
    }

    public static List<ProductCountry> getProductCountries(){
        return List.of(ES, PT, FR, IT, DE, UK);
    }
}