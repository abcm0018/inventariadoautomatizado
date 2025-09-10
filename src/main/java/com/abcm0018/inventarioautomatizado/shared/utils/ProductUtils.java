package com.abcm0018.inventarioautomatizado.shared.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProductUtils {
    private static final String EAN_REGEX = "^[0-9]{14}$";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    private ProductUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isEANValid(String ean) {
        return ean != null && ean.matches(EAN_REGEX);
    }
}
